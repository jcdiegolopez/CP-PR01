package com.yalex.gui;

import java.awt.Color;
import java.io.OutputStream;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * Redirige un flujo de salida (como System.out o System.err) 
 * hacia un JTextPane de Swing. Se asegura de insertar el texto 
 * desde el Event Dispatch Thread para no congelar la UI.
 */
public class ConsoleOutputStream extends OutputStream {
    
    private final JTextPane textPane;
    private final Color color;

    /**
     * @param textPane Componente donde se pintará el texto
     * @param colorHex Color en formato hexadecimal (ej. "#cc0000")
     */
    public ConsoleOutputStream(JTextPane textPane, String colorHex) {
        this.textPane = textPane;
        this.color = Color.decode(colorHex);
    }

    @Override
    public void write(byte[] b, int off, int len) {
        String text = new String(b, off, len);
        appendToPane(text);
    }

    @Override
    public void write(int b) {
        // Fallback: escribe caracter a caracter si alguien hace write() directo
        appendToPane(String.valueOf((char) b));
    }

    private void appendToPane(String text) {
        // Toda actualización UI debe ocurrir en el EDT
        SwingUtilities.invokeLater(() -> {
            StyledDocument doc = textPane.getStyledDocument();
            Style style = textPane.addStyle("ColorStyle", null);
            StyleConstants.setForeground(style, color);
            
            try {
                doc.insertString(doc.getLength(), text, style);
                textPane.setCaretPosition(doc.getLength());
            } catch (Exception e) {
                // Ignorar de forma segura si el documento se invalida
                e.printStackTrace();
            }
        });
    }
}
