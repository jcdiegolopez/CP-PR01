package com.yalex.gui;

import com.yalex.Main;
import com.yalex.automata.dfa.CombinedDfaBuilder;
import com.yalex.codegen.LetExpander;
import com.yalex.model.LetDefinition;
import com.yalex.model.Rule;
import com.yalex.model.RuleSet;
import com.yalex.regex.RegexParser;
import com.yalex.regex.node.RegexNode;
import com.yalex.yal.YalFile;
import com.yalex.yal.YalParser;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Lógica y manejo de eventos. Adaptado para simular VSCode UX.
 */
public class YalexController {

    private final YalexGui view;
    private final Preferences prefs;
    private static final String LAST_DIR_PREF = "LAST_DIR_PREF";
    private File currentFile = null;
    /** Ruta del último {@code .py} generado con éxito (Run); se usa en "Probar lexer". */
    private Path lastGeneratedLexerPath = null;
    private boolean suppressDirtyEvents = false;

    public YalexController(YalexGui view) {
        this.view = view;
        this.prefs = Preferences.userNodeForPackage(YalexController.class);
        initController();
    }

    private void initController() {
        System.setOut(new PrintStream(new ConsoleOutputStream(view.getTerminalArea(), "#CCCCCC")));
        System.setErr(new PrintStream(new ConsoleOutputStream(view.getTerminalArea(), "#FF6B6B")));

        view.getOpenFileBtn().addActionListener(this::openFile);
        view.getRunBtn().addActionListener(this::runBuildTask);
        view.getLexerTestRunBtn().addActionListener(this::runLexerTest);
    }

    private void openFile(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        String lastDir = prefs.get(LAST_DIR_PREF, null);
        if (lastDir != null) {
            chooser.setCurrentDirectory(new File(lastDir));
        }
        chooser.setFileFilter(new FileNameExtensionFilter("YALex File (*.yal)", "yal"));
        
        if (chooser.showOpenDialog(view) == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();
            prefs.put(LAST_DIR_PREF, selected.getParent());
            loadFile(selected);
        }
    }

    private void loadFile(File file) {
        this.currentFile = file;
        this.lastGeneratedLexerPath = null;
        view.getStatusLabel().setText(" " + file.getName() + "       UTF-8    YALex");

        try {
            String content = Files.readString(file.toPath());
            suppressDirtyEvents = true;
            view.openYalTab(file.getName(), content);
            installDirtyTrackingIfNeeded();
            view.setYalTabDirty(false);
            view.getTerminalArea().setText("> Archivo cargado correctamente en el editor.\n");
            suppressDirtyEvents = false;
        } catch (Exception ex) {
            suppressDirtyEvents = false;
            System.err.println("Error al leer el archivo: " + ex.getMessage());
        }
    }

    private void runBuildTask(ActionEvent e) {
        if (currentFile == null) {
            System.err.println("[Validación] Por favor, abre un archivo .yal desde el EXPLORER primero.");
            return;
        }
        if (view.getYalEditorArea() == null) {
            System.err.println("[Validación] No hay editor .yal activo.");
            return;
        }

        try {
            Files.writeString(currentFile.toPath(), view.getYalEditorArea().getText());
            view.setYalTabDirty(false);
        } catch (Exception ex) {
            System.err.println("Error automático al guardar: " + ex.getMessage());
            return;
        }

        view.getRunBtn().setEnabled(false);
        view.getTerminalArea().setText("");

        System.out.println("> Executing task: compile YALex \n");

        new SwingWorker<Void, Void>() {
            private Path outPath;
            private String generatedSource;
            private DfaGraphPanel graphPanel;

            @Override
            protected Void doInBackground() {
                try {
                    Path inPath = currentFile.toPath();
                    String name = inPath.getFileName().toString();
                    String base = name.substring(0, name.lastIndexOf("."));
                    outPath = inPath.resolveSibling(base + ".py");

                    Main.runGeneration(inPath, outPath);
                    generatedSource = Files.readString(outPath);
                    graphPanel = buildCombinedDfaGraph(Files.readString(inPath));
                } catch (IllegalArgumentException ex) {
                    System.err.println("\n[ERROR DE COMPILACIÓN O SINTAXIS]");
                    System.err.println(ex.getMessage());
                } catch (Exception ex) {
                    System.err.println("\n[ERROR CRÍTICO DESCONOCIDO]");
                    ex.printStackTrace(System.err);
                }
                return null;
            }

            @Override
            protected void done() {
                if (generatedSource != null && outPath != null) {
                    lastGeneratedLexerPath = outPath;
                    view.showGeneratedPythonTab(outPath.getFileName().toString(), generatedSource);
                }
                if (graphPanel != null) {
                    view.showDfaGraphTab("Lexer DFA", graphPanel);
                }
                System.out.println("\n> Terminal process finished.");
                view.getRunBtn().setEnabled(true);
            }
        }.execute();
    }

    private void runLexerTest(ActionEvent e) {
        if (lastGeneratedLexerPath == null || !Files.isRegularFile(lastGeneratedLexerPath)) {
            view.getLexerTestOutputArea().setText(
                    "No hay un lexer generado para el archivo actual.\n"
                            + "Abre un .yal, pulsa Run y espera a que la compilación termine sin errores.\n");
            view.selectLexerTestTab();
            return;
        }

        final String input = view.getLexerTestInputArea().getText();
        view.getLexerTestRunBtn().setEnabled(false);

        new SwingWorker<LexerTestRunner.Result, Void>() {
            @Override
            protected LexerTestRunner.Result doInBackground() throws Exception {
                return LexerTestRunner.run(lastGeneratedLexerPath, input);
            }

            @Override
            protected void done() {
                view.getLexerTestRunBtn().setEnabled(true);
                try {
                    LexerTestRunner.Result r = get();
                    String text = formatLexerRunOutput(r);
                    view.getLexerTestOutputArea().setText(text);
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    view.getLexerTestOutputArea().setText(
                            "Error al ejecutar el lexer:\n"
                                    + cause.getClass().getSimpleName() + ": " + cause.getMessage());
                }
                view.selectLexerTestTab();
            }
        }.execute();
    }

    private static String formatLexerRunOutput(LexerTestRunner.Result r) {
        if (r.isPythonMissing()) {
            return r.stderr();
        }
        StringBuilder sb = new StringBuilder();
        String out = r.stdout();
        String err = r.stderr();
        if (out != null && !out.isEmpty()) {
            sb.append(out);
        }
        if (err != null && !err.isEmpty()) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append("--- stderr ---\n").append(err);
        }
        if (sb.length() == 0) {
            sb.append("(sin salida en stdout/stderr; código de salida ").append(r.exitCode()).append(")");
        } else if (r.exitCode() != 0) {
            sb.append("\n--- código de salida: ").append(r.exitCode()).append(" ---");
        }
        return sb.toString();
    }

    private void installDirtyTrackingIfNeeded() {
        if (view.getYalEditorArea() == null) {
            return;
        }
        if (Boolean.TRUE.equals(view.getYalEditorArea().getClientProperty("dirty-listener-installed"))) {
            return;
        }
        view.getYalEditorArea().putClientProperty("dirty-listener-installed", true);
        view.getYalEditorArea().getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                markDirty();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                markDirty();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                markDirty();
            }

            private void markDirty() {
                if (!suppressDirtyEvents) {
                    view.setYalTabDirty(true);
                }
            }
        });
    }

    /**
     * Construye el DFA combinado del lexer y crea el panel gráfico interactivo.
     */
    private DfaGraphPanel buildCombinedDfaGraph(String yalSource) {
        YalFile yal = new YalParser().parse(yalSource);
        List<LetDefinition> lets = yal.getLetDefinitions();
        if (yal.getRuleSets().isEmpty()) {
            throw new IllegalArgumentException("No hay bloques rule para construir DFA.");
        }

        RuleSet rs = yal.getRuleSets().get(0);
        List<Rule> rules = rs.getRules();

        List<RegexNode> asts = new ArrayList<>();
        List<String> patterns = new ArrayList<>();

        for (Rule rule : rules) {
            String expanded = LetExpander.apply(rule.getPattern().trim(), lets);
            if (expanded.equalsIgnoreCase("eof")) {
                continue;
            }
            asts.add(RegexParser.parse(expanded));
            patterns.add(rule.getPattern().trim());
        }

        if (asts.isEmpty()) {
            throw new IllegalArgumentException("No hay reglas no-EOF para construir DFA.");
        }

        CombinedDfaBuilder.Result result = CombinedDfaBuilder.build(asts, patterns);

        System.out.println("Combined DFA: " + result.dfa().getStateCount() + " estados, "
                + result.dfa().getAlphabet().size() + " símbolos en el alfabeto");

        return new DfaGraphPanel(result);
    }
}

