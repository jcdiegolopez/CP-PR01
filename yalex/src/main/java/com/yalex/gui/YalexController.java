package com.yalex.gui;

import com.yalex.Main;
import com.yalex.automata.dfa.DFA;
import com.yalex.automata.dfa.DirectDfaBuilder;
import com.yalex.automata.minimization.DFAMinimizer;
import com.yalex.automata.syntax.SyntaxTree;
import com.yalex.automata.syntax.SyntaxTreeBuilder;
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
            private String diagramText;

            @Override
            protected Void doInBackground() {
                try {
                    Path inPath = currentFile.toPath();
                    String name = inPath.getFileName().toString();
                    String base = name.substring(0, name.lastIndexOf("."));
                    outPath = inPath.resolveSibling(base + ".py");

                    Main.runGeneration(inPath, outPath);
                    generatedSource = Files.readString(outPath);
                    diagramText = buildStateDiagramFromYal(Files.readString(inPath));
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
                    view.showGeneratedPythonTab(outPath.getFileName().toString(), generatedSource);
                }
                if (diagramText != null && outPath != null) {
                    String diagramTabName = outPath.getFileName().toString().replaceFirst("\\.py$", "") + ".dfa.txt";
                    view.showDiagramTab(diagramTabName, diagramText);
                }
                System.out.println("\n> Terminal process finished.");
                view.getRunBtn().setEnabled(true);
            }
        }.execute();
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

    private String buildStateDiagramFromYal(String yalSource) {
        YalFile yal = new YalParser().parse(yalSource);
        List<LetDefinition> lets = yal.getLetDefinitions();
        if (yal.getRuleSets().isEmpty()) {
            return "No hay bloques rule para construir diagrama.";
        }

        RuleSet rs = yal.getRuleSets().get(0);
        StringBuilder sb = new StringBuilder();
        sb.append("State Transition Diagram (textual)\n");
        sb.append("Rule set: ").append(rs.getName()).append("\n\n");

        List<Rule> rules = rs.getRules();
        for (int i = 0; i < rules.size(); i++) {
            Rule rule = rules.get(i);
            String expanded = LetExpander.apply(rule.getPattern().trim(), lets);
            if (expanded.equalsIgnoreCase("eof")) {
                continue;
            }

            RegexNode ast = RegexParser.parse(expanded);
            SyntaxTree tree = SyntaxTreeBuilder.build(ast);
            DFA dfa = DFAMinimizer.minimize(DirectDfaBuilder.build(tree));

            sb.append("Rule #").append(i).append(": ").append(rule.getPattern()).append("\n");
            sb.append("Initial state: q").append(dfa.getInitialStateId()).append("\n");
            sb.append("States:\n");
            for (int s = 0; s < dfa.getStateCount(); s++) {
                boolean isInitial = s == dfa.getInitialStateId();
                boolean isAccept = dfa.getState(s).isAccepting();
                sb.append("  q").append(s);
                if (isInitial) {
                    sb.append(" [start]");
                }
                if (isAccept) {
                    sb.append(" [accept]");
                }
                sb.append("\n");
            }

            sb.append("Transitions:\n");
            for (var fromEntry : dfa.getTransitionTable().entrySet()) {
                int from = fromEntry.getKey();
                for (var tr : fromEntry.getValue().entrySet()) {
                    char symbol = tr.getKey();
                    int to = tr.getValue();
                    sb.append("  q").append(from)
                            .append(" -- ").append(printableSymbol(symbol))
                            .append(" --> q").append(to)
                            .append("\n");
                }
            }
            sb.append("\n");
        }

        return sb.toString().trim();
    }

    private String printableSymbol(char c) {
        return switch (c) {
            case '\n' -> "\\n";
            case '\t' -> "\\t";
            case '\r' -> "\\r";
            case ' ' -> "<space>";
            default -> String.valueOf(c);
        };
    }
}
