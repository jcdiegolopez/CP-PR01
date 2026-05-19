package com.yapar.gui;

import com.yapar.analysis.FirstFollowCalculator;
import com.yapar.automaton.LR0Automaton;
import com.yapar.automaton.LR0AutomatonBuilder;
import com.yapar.codegen.ParserCodeGen;
import com.yapar.model.Grammar;
import com.yapar.tables.Conflict;
import com.yapar.tables.ParseResult;
import com.yapar.tables.SLRAction;
import com.yapar.tables.SLRTableBuilder;
import com.yapar.yalp.YalpParser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Panel completo de YAPar para la GUI.
 *
 * Estructura:
 *   NORTH  — barra de herramientas: "Abrir .yalp" + nombre + "Run YAPar"
 *   CENTER — JSplitPane vertical
 *               TOP: editor .yalp
 *               BOTTOM: sub-pestañas (Tabla SLR, Conflictos, Parser .py)
 *   SOUTH  — barra de estado
 */
public final class YaparPanel extends JPanel {

    // ── Paleta (misma que YalexGui) ───────────────────────────────────────────
    private static final Color BG_EDITOR  = new Color(30, 30, 30);
    private static final Color BG_TABS    = new Color(45, 45, 45);
    private static final Color FG_TEXT    = new Color(224, 224, 224);
    private static final Color FG_ACTIVE  = new Color(255, 255, 255);
    private static final Color FG_BLUE    = new Color(79, 193, 255);
    private static final Color FG_RED     = new Color(255, 107, 107);
    private static final Color FG_GREEN   = new Color(87, 201, 126);
    private static final Color BORDER_CLR = new Color(62, 62, 66);
    private static final Color RUN_GREEN  = new Color(46, 160, 67);
    private static final Font  MONO_FONT  = new Font("Consolas", Font.PLAIN, 13);
    private static final Font  UI_FONT    = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font  UI_BOLD    = new Font("Segoe UI", Font.BOLD, 13);

    private static final String PREF_LAST_DIR = "YAPAR_LAST_DIR";

    // ── Componentes ───────────────────────────────────────────────────────────
    private final JTextArea   yalpEditor;
    private final JTextArea   conflictsArea;
    private final JTextArea   codeArea;
    private final JTextField  testInputField;
    private final JTextArea   testOutputArea;
    private final JButton     testRunBtn;
    private final JLabel      fileLabel;
    private final JLabel      statusLabel;
    private final JButton     runBtn;
    private final JTabbedPane resultTabs;

    private File        currentYalpFile  = null;
    private ParseResult lastParseResult  = null;
    private LR0Automaton lastAutomaton   = null;

    // ── Constructor ───────────────────────────────────────────────────────────

    public YaparPanel() {
        super(new BorderLayout());
        setBackground(BG_EDITOR);
        putClientProperty("yalex.tabId", "yapar");

        // ── Toolbar NORTH ─────────────────────────────────────────────────────
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(BG_TABS);
        toolbar.setBorder(new EmptyBorder(8, 10, 8, 10));

        JButton openBtn = makeBtn("+ Abrir .yalp", new Color(14, 99, 156));
        fileLabel = new JLabel("  sin archivo");
        fileLabel.setForeground(FG_TEXT);
        fileLabel.setFont(UI_FONT);

        runBtn = makeBtn("Run YAPar  ▶", RUN_GREEN);
        runBtn.setEnabled(false);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        left.add(openBtn);
        left.add(fileLabel);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(runBtn);

        toolbar.add(left,  BorderLayout.WEST);
        toolbar.add(right, BorderLayout.EAST);

        // ── Editor .yalp ──────────────────────────────────────────────────────
        yalpEditor = makeTextArea(true);
        yalpEditor.setText("(* Abre o pega aquí tu archivo .yalp *)\n");
        JScrollPane editorScroll = scrollOf(yalpEditor);

        // ── Sub-pestañas de resultado ─────────────────────────────────────────
        resultTabs = new JTabbedPane();
        resultTabs.setBackground(BG_TABS);
        resultTabs.setForeground(FG_TEXT);
        resultTabs.setFont(UI_BOLD);
        resultTabs.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_CLR));
        // Quitar el foco punteado y forzar colores oscuros
        UIManager.put("TabbedPane.selected",             BG_EDITOR);
        UIManager.put("TabbedPane.contentAreaColor",     BG_EDITOR);
        UIManager.put("TabbedPane.foreground",           FG_TEXT);
        UIManager.put("TabbedPane.selectedForeground",   FG_ACTIVE);
        UIManager.put("TabbedPane.focus",                BG_TABS);
        UIManager.put("TabbedPane.tabAreaBackground",    BG_TABS);
        UIManager.put("TabbedPane.unselectedBackground", BG_TABS);
        SwingUtilities.updateComponentTreeUI(resultTabs);

        // Pestaña 1: Tabla SLR(1) — se rellena al correr
        JLabel placeholderTable = new JLabel("Ejecuta Run YAPar para ver la tabla SLR(1).",
                                              SwingConstants.CENTER);
        placeholderTable.setForeground(FG_TEXT);
        placeholderTable.setFont(UI_FONT);
        placeholderTable.setOpaque(true);
        placeholderTable.setBackground(BG_EDITOR);
        resultTabs.addTab("Tabla SLR(1)", placeholderTable);

        // Pestaña 2: Conflictos
        conflictsArea = makeTextArea(false);
        resultTabs.addTab("Conflictos", scrollOf(conflictsArea));

        // Pestaña 3: Parser .py generado
        codeArea = makeTextArea(false);
        resultTabs.addTab("Parser .py", scrollOf(codeArea));

        // Pestaña 4: Probar gramática
        testInputField  = new JTextField();
        testOutputArea  = makeTextArea(false);
        testRunBtn      = makeBtn("Probar  ▶", new Color(14, 99, 156));
        testRunBtn.setEnabled(false);
        resultTabs.addTab("Probar gramática", buildTestTab());

        // Aplicar tab components estilizados y escuchar cambios de selección
        for (int i = 0; i < resultTabs.getTabCount(); i++) {
            styledTab(resultTabs, i);
        }
        resultTabs.addChangeListener(e -> refreshTabContrast());

        // ── Split editor / resultados ─────────────────────────────────────────
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, editorScroll, resultTabs);
        split.setResizeWeight(0.40);
        split.setDividerSize(4);
        split.setBorder(null);
        split.setBackground(BG_EDITOR);

        // ── Status SOUTH ──────────────────────────────────────────────────────
        statusLabel = new JLabel(" Listo");
        statusLabel.setForeground(FG_TEXT);
        statusLabel.setFont(UI_FONT);
        statusLabel.setBorder(new EmptyBorder(4, 10, 4, 10));
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(37, 37, 38));

        // ── Layout ────────────────────────────────────────────────────────────
        add(toolbar,     BorderLayout.NORTH);
        add(split,       BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        // ── Listeners ─────────────────────────────────────────────────────────
        openBtn.addActionListener(e -> openYalpFile());
        runBtn .addActionListener(e -> runPipeline());
        testRunBtn    .addActionListener(e -> runGrammarTest());
        testInputField.addActionListener(e -> runGrammarTest()); // Enter también corre
    }

    // ── Abrir archivo ─────────────────────────────────────────────────────────

    private void openYalpFile() {
        Preferences prefs = Preferences.userNodeForPackage(YaparPanel.class);
        JFileChooser chooser = new JFileChooser();
        String lastDir = prefs.get(PREF_LAST_DIR, null);
        if (lastDir != null) chooser.setCurrentDirectory(new File(lastDir));
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "YAPar Grammar (*.yalp)", "yalp"));

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        currentYalpFile = chooser.getSelectedFile();
        prefs.put(PREF_LAST_DIR, currentYalpFile.getParent());

        try {
            String content = Files.readString(currentYalpFile.toPath());
            yalpEditor.setText(content);
            yalpEditor.setCaretPosition(0);
            fileLabel.setText("  " + currentYalpFile.getName());
            runBtn.setEnabled(true);
            status("Archivo cargado: " + currentYalpFile.getName(), FG_TEXT);
        } catch (Exception ex) {
            status("Error al leer archivo: " + ex.getMessage(), FG_RED);
        }
    }

    // ── Pipeline principal ────────────────────────────────────────────────────

    private void runPipeline() {
        runBtn.setEnabled(false);
        status("Ejecutando pipeline YAPar...", FG_BLUE);
        conflictsArea.setText("");
        codeArea.setText("");

        SwingWorker<PipelineOutput, Void> worker = new SwingWorker<>() {
            @Override
            protected PipelineOutput doInBackground() throws Exception {
                String source = yalpEditor.getText();

                var yalpFile  = new YalpParser().parse(source);
                var grammar   = Grammar.build(yalpFile);
                var automaton = new LR0AutomatonBuilder(grammar).build();
                var followMap = new FirstFollowCalculator(grammar).getFollowSets();
                var result    = new SLRTableBuilder(automaton, followMap).build();

                // Generar .py junto al .yalp (o en directorio temp si no hay archivo)
                Path outPath = resolveOutputPath();
                ParserCodeGen.generate(result, grammar, automaton, outPath);
                String pyCode = Files.readString(outPath);

                return new PipelineOutput(grammar, automaton, result, pyCode);
            }

            @Override
            protected void done() {
                runBtn.setEnabled(true);
                try {
                    PipelineOutput out = get();
                    lastParseResult = out.result();
                    lastAutomaton   = out.automaton();
                    testRunBtn.setEnabled(true);

                    displayTable(out.grammar(), out.automaton(), out.result());
                    displayConflicts(out.result().conflicts());
                    codeArea.setText(out.pyCode());
                    codeArea.setCaretPosition(0);

                    int nConflicts = out.result().conflicts().size();
                    String msg = String.format(
                        "OK — %d estado(s), %d conflicto(s). Parser generado.",
                        out.automaton().stateCount(), nConflicts);
                    status(msg, nConflicts == 0 ? FG_GREEN : FG_RED);

                    // Mostrar tabla si no hay conflictos, conflictos si los hay
                    resultTabs.setSelectedIndex(nConflicts > 0 ? 1 : 0);

                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    status("Error: " + cause.getMessage(), FG_RED);
                    conflictsArea.setText(cause.getClass().getSimpleName()
                                          + ": " + cause.getMessage());
                    resultTabs.setSelectedIndex(1);
                }
            }
        };
        worker.execute();
    }

    // ── Mostrar tabla SLR(1) ──────────────────────────────────────────────────

    private void displayTable(Grammar grammar, LR0Automaton automaton, ParseResult result) {
        // Columnas: "Estado" | terminales (ACTION) | no-terminales (GOTO)
        List<String> terminals    = new ArrayList<>(grammar.terminals());
        List<String> nonTerminals = new ArrayList<>(grammar.nonTerminals());
        Collections.sort(terminals);
        Collections.sort(nonTerminals);

        List<String> allCols = new ArrayList<>();
        allCols.add("Estado");
        allCols.addAll(terminals);
        allCols.addAll(nonTerminals);

        int numStates = automaton.stateCount();
        Object[][] data = new Object[numStates][allCols.size()];

        for (int s = 0; s < numStates; s++) {
            data[s][0] = s;
            // ACTION columns
            for (int c = 0; c < terminals.size(); c++) {
                SLRAction action = result.table().action(s, terminals.get(c));
                data[s][1 + c] = actionCell(action);
            }
            // GOTO columns
            for (int c = 0; c < nonTerminals.size(); c++) {
                Optional<Integer> target = result.table().gotoState(s, nonTerminals.get(c));
                data[s][1 + terminals.size() + c] = target.map(Object::toString).orElse("");
            }
        }

        DefaultTableModel model = new DefaultTableModel(data, allCols.toArray()) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(model);
        table.setBackground(BG_EDITOR);
        table.setForeground(FG_TEXT);
        table.setGridColor(BORDER_CLR);
        table.setFont(MONO_FONT);
        table.setRowHeight(22);
        table.getTableHeader().setBackground(BG_TABS);
        table.getTableHeader().setForeground(FG_ACTIVE);
        table.getTableHeader().setFont(UI_BOLD);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Colorear celdas: shift=azul, reduce=amarillo, accept=verde, error=vacío
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                String txt = value == null ? "" : value.toString();
                if (sel) {
                    c.setBackground(new Color(60, 80, 100));
                    c.setForeground(FG_ACTIVE);
                } else if (txt.startsWith("s") && !txt.isEmpty() && Character.isDigit(txt.charAt(1))) {
                    c.setBackground(new Color(20, 50, 80));
                    c.setForeground(FG_BLUE);
                } else if (txt.startsWith("r")) {
                    c.setBackground(new Color(60, 50, 10));
                    c.setForeground(new Color(255, 200, 50));
                } else if ("acc".equals(txt)) {
                    c.setBackground(new Color(20, 60, 30));
                    c.setForeground(FG_GREEN);
                } else {
                    c.setBackground(BG_EDITOR);
                    c.setForeground(FG_TEXT);
                }
                setHorizontalAlignment(col == 0 ? CENTER : CENTER);
                return c;
            }
        };

        for (int c = 0; c < table.getColumnCount(); c++) {
            table.getColumnModel().getColumn(c).setCellRenderer(renderer);
            table.getColumnModel().getColumn(c).setPreferredWidth(c == 0 ? 60 : 90);
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG_EDITOR);

        // Separador visual entre ACTION y GOTO (línea en el header)
        addSectionLabel(scroll, terminals.size(), nonTerminals.size(), allCols);

        resultTabs.setComponentAt(0, scroll);
    }

    private String actionCell(SLRAction action) {
        return switch (action) {
            case SLRAction.Shift s  -> "s" + s.state();
            case SLRAction.Reduce r -> "r" + r.production().id();
            case SLRAction.Accept a -> "acc";
            case SLRAction.Error e  -> "";
        };
    }

    private void addSectionLabel(JScrollPane scroll, int nTerminals, int nNonTerminals,
                                  List<String> allCols) {
        // Solo añade etiquetas ACTION / GOTO encima del header (opcional, decorativo)
        // Se omite por simplicidad — la separación es visible por los nombres de columna
    }

    // ── Mostrar conflictos ────────────────────────────────────────────────────

    private void displayConflicts(List<Conflict> conflicts) {
        if (conflicts.isEmpty()) {
            conflictsArea.setText("No se detectaron conflictos.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(conflicts.size()).append(" conflicto(s) detectado(s):\n\n");
        for (Conflict c : conflicts) {
            sb.append(c).append("\n");
        }
        sb.append("\nRegla de desempate aplicada: Shift > Reduce; entre Reduce/Reduce gana menor id.");
        conflictsArea.setText(sb.toString());
        conflictsArea.setCaretPosition(0);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Path resolveOutputPath() {
        if (currentYalpFile != null) {
            String name = currentYalpFile.getName();
            String base = name.contains(".") ? name.substring(0, name.lastIndexOf('.')) : name;
            return currentYalpFile.toPath().resolveSibling("theparser_" + base + ".py");
        }
        return Path.of(System.getProperty("java.io.tmpdir"), "theparser.py");
    }

    private void status(String msg, Color color) {
        statusLabel.setText(" " + msg);
        statusLabel.setForeground(color);
    }

    private JTextArea makeTextArea(boolean editable) {
        JTextArea area = new JTextArea();
        area.setEditable(editable);
        area.setBackground(BG_EDITOR);
        area.setForeground(new Color(212, 212, 212));
        area.setCaretColor(Color.WHITE);
        area.setFont(MONO_FONT);
        area.setMargin(new Insets(10, 15, 10, 10));
        return area;
    }

    private JScrollPane scrollOf(JTextArea area) {
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(null);
        return scroll;
    }

    private JButton makeBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setUI(new BasicButtonUI());
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(UI_BOLD);
        btn.setOpaque(true);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ── Probar gramática ──────────────────────────────────────────────────────

    private JPanel buildTestTab() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_EDITOR);

        // Barra superior: campo de texto + botón
        JPanel bar = new JPanel(new BorderLayout(8, 0));
        bar.setBackground(BG_TABS);
        bar.setBorder(new EmptyBorder(8, 10, 8, 10));

        JLabel hint = new JLabel("Tokens separados por espacio  (ej: NUM PLUS NUM)");
        hint.setForeground(FG_TEXT);
        hint.setFont(UI_FONT);

        testInputField.setBackground(new Color(50, 50, 50));
        testInputField.setForeground(FG_ACTIVE);
        testInputField.setCaretColor(Color.WHITE);
        testInputField.setFont(MONO_FONT);
        testInputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_CLR),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));

        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnWrap.setOpaque(false);
        btnWrap.add(testRunBtn);

        bar.add(hint,         BorderLayout.NORTH);
        bar.add(testInputField, BorderLayout.CENTER);
        bar.add(btnWrap,      BorderLayout.EAST);

        // Área de salida
        testOutputArea.setText("Escribe tokens arriba y presiona Probar (o Enter).");
        testOutputArea.setFont(new Font("Consolas", Font.BOLD, 14));

        root.add(bar,                  BorderLayout.NORTH);
        root.add(scrollOf(testOutputArea), BorderLayout.CENTER);
        return root;
    }

    private void runGrammarTest() {
        if (lastParseResult == null) return;

        String raw = testInputField.getText().trim();
        if (raw.isEmpty()) {
            showTestResult(false, "Escribe al menos un token.");
            return;
        }

        String[] tokenTypes = raw.split("\\s+");
        String result = runLRParser(tokenTypes, lastParseResult);
        boolean accepted = result.startsWith("OK");
        showTestResult(accepted, result);

        // Cambiar a esta pestaña para ver el resultado
        resultTabs.setSelectedIndex(3);
        refreshTabContrast();
    }

    private String runLRParser(String[] tokenTypes, ParseResult parseResult) {
        // Verificar antes de parsear si algún token no está en el vocabulario
        java.util.Set<String> knownTerminals = new java.util.HashSet<>();
        parseResult.table().actionTable().values()
                .forEach(row -> knownTerminals.addAll(row.keySet()));

        for (int k = 0; k < tokenTypes.length; k++) {
            if (!knownTerminals.contains(tokenTypes[k])) {
                return "Error léxico — el token '" + tokenTypes[k] + "' (posición " + (k + 1) + ")"
                     + " no está declarado en la gramática (%token).\n"
                     + "Terminales válidos: " + sortedList(knownTerminals);
            }
        }

        java.util.Deque<Integer> stateStack = new java.util.ArrayDeque<>();
        stateStack.push(0);
        int i = 0;

        while (true) {
            int state = stateStack.peek();
            String token = (i < tokenTypes.length) ? tokenTypes[i] : "$";
            SLRAction action = parseResult.table().action(state, token);

            if (action instanceof SLRAction.Shift s) {
                stateStack.push(s.state());
                i++;

            } else if (action instanceof SLRAction.Reduce r) {
                int rhsLen = r.production().rhs().size();
                for (int j = 0; j < rhsLen; j++) stateStack.pop();
                int top = stateStack.peek();
                java.util.Optional<Integer> next =
                        parseResult.table().gotoState(top, r.production().lhs());
                if (next.isEmpty()) {
                    // El parser acepta la estructura pero la acción semántica no tiene destino
                    return "Error semántico — la reducción " + r.production()
                         + " no tiene entrada GOTO desde el estado " + top + ".\n"
                         + "La gramática puede tener conflictos no resueltos.";
                }
                stateStack.push(next.get());

            } else if (action instanceof SLRAction.Accept) {
                return "OK — cadena aceptada por la gramática.";

            } else {
                // ACTION = Error: el token no encaja en esta posición
                String posInfo = (i < tokenTypes.length)
                        ? "'" + tokenTypes[i] + "' en posición " + (i + 1)
                        : "fin de entrada ('$')";
                String leido = i == 0
                        ? "(ninguno)"
                        : String.join(" ", java.util.Arrays.copyOf(tokenTypes, i));
                return "Error sintáctico — token inesperado: " + posInfo + ".\n"
                     + "Tokens consumidos correctamente: " + leido + "\n"
                     + "Estado actual del parser: " + state;
            }
        }
    }

    private String sortedList(java.util.Set<String> set) {
        java.util.List<String> list = new java.util.ArrayList<>(set);
        list.remove("$");
        java.util.Collections.sort(list);
        return String.join(", ", list);
    }

    private void showTestResult(boolean ok, String msg) {
        testOutputArea.setForeground(ok ? FG_GREEN : FG_RED);
        testOutputArea.setText(msg);
    }

    // ── Estilo de pestañas (misma técnica que YalexGui) ──────────────────────

    private void styledTab(JTabbedPane tabs, int index) {
        String title = tabs.getTitleAt(index);
        JPanel tab = new JPanel(new BorderLayout());
        tab.setOpaque(true);
        tab.setBackground(BG_TABS);
        tab.setBorder(new EmptyBorder(4, 10, 4, 10));
        JLabel label = new JLabel(title, SwingConstants.LEFT);
        label.setFont(UI_BOLD);
        label.setForeground(FG_TEXT);
        tab.add(label, BorderLayout.CENTER);
        tabs.setTabComponentAt(index, tab);
    }

    private void refreshTabContrast() {
        int selected = resultTabs.getSelectedIndex();
        for (int i = 0; i < resultTabs.getTabCount(); i++) {
            resultTabs.setBackgroundAt(i, i == selected ? BG_EDITOR : BG_TABS);
            Component tabComp = resultTabs.getTabComponentAt(i);
            if (tabComp instanceof JPanel panel && panel.getComponentCount() > 0
                    && panel.getComponent(0) instanceof JLabel label) {
                panel.setBackground(i == selected ? BG_EDITOR : BG_TABS);
                label.setForeground(i == selected ? FG_ACTIVE : FG_TEXT);
            }
        }
    }

    // ── Record interno para pasar datos entre hilos ───────────────────────────

    private record PipelineOutput(
            Grammar grammar,
            LR0Automaton automaton,
            ParseResult result,
            String pyCode) {}
}
