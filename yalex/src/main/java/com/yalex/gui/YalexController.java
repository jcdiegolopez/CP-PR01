package com.yalex.gui;

import com.yalex.Main;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.prefs.Preferences;

/**
 * Lógica y manejo de eventos. Adaptado para simular VSCode UX.
 */
public class YalexController {

    private final YalexGui view;
    private final Preferences prefs;
    private static final String LAST_DIR_PREF = "LAST_DIR_PREF";
    private File currentFile = null;

    public YalexController(YalexGui view) {
        this.view = view;
        this.prefs = Preferences.userNodeForPackage(YalexController.class);
        initController();
    }

    private void initController() {
        // Redirigir consola al nuevo clon de terminal inferior
        System.setOut(new PrintStream(new ConsoleOutputStream(view.getTerminalArea(), "#CCCCCC"))); 
        System.setErr(new PrintStream(new ConsoleOutputStream(view.getTerminalArea(), "#FF6B6B")));

        // Registro de Eventos
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
        view.getTabLabel().setText(" " + file.getName() + "  \u2715");
        view.getStatusLabel().setText(" " + file.getName() + "       UTF-8    YALex");
        
        try {
            String content = Files.readString(file.toPath());
            view.getEditorArea().setText(content);
            view.getEditorArea().setCaretPosition(0);
            view.getTerminalArea().setText("> Archivo cargado correctamente en el editor.\n");
        } catch (Exception ex) {
            System.err.println("Error al leer el archivo: " + ex.getMessage());
        }
    }

    private void runBuildTask(ActionEvent e) {
        if (currentFile == null) {
            System.err.println("[Validación] Por favor, abre un archivo .yal desde el EXPLORER primero.");
            return;
        }

        // Simula el "Auto-Save" de VS Code: guarda lo que haya escrito en el JTextArea al archivo físico pre-compilación
        try {
            Files.writeString(currentFile.toPath(), view.getEditorArea().getText());
        } catch (Exception ex) {
            System.err.println("Error automático al guardar: " + ex.getMessage());
            return;
        }

        view.getRunBtn().setEnabled(false);
        view.getTerminalArea().setText(""); // Limpiar terminal 
        
        System.out.println("> Executing task: compile YALex \n");

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    Path inPath = currentFile.toPath();
                    String name = inPath.getFileName().toString();
                    String base = name.substring(0, name.lastIndexOf("."));
                    Path outPath = inPath.resolveSibling(base + ".py"); // El .py va al mismo directorio
                    
                    Main.runGeneration(inPath, outPath);
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
                System.out.println("\n> Terminal process finished.");
                view.getRunBtn().setEnabled(true);
            }
        }.execute();
    }
}
