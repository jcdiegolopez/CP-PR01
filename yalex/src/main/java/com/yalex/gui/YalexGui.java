package com.yalex.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.basic.BasicButtonUI;

public class YalexGui extends JFrame {

    private static final Color BG_ACTIVITY = new Color(51, 51, 51);
    private static final Color BG_SIDEBAR = new Color(37, 37, 38);
    private static final Color BG_EDITOR = new Color(30, 30, 30);
    private static final Color BG_TABS = new Color(45, 45, 45);
    private static final Color FG_TEXT = new Color(224, 224, 224);
    private static final Color FG_TEXT_ACTIVE = new Color(255, 255, 255);
    private static final Color FG_BLUE = new Color(79, 193, 255);
    private static final Color BORDER_COLOR = new Color(62, 62, 66);
    private static final Color RUN_GREEN = new Color(46, 160, 67);

    private final JButton openFileBtn;
    private final JButton runBtn;
    private final JTextPane terminalArea;
    private final JLabel statusLabel;
    private final JTabbedPane editorTabs;

    private JTextArea yalEditorArea;
    private String yalTabTitle = "untitled.yal";

    private JTextArea lexerTestInputArea;
    private JTextArea lexerTestOutputArea;
    private JButton lexerTestRunBtn;

    public YalexGui() {
        super("YALex - Visual Studio Code");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 760);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_EDITOR);

        JPanel leftContainer = new JPanel(new BorderLayout());

        JPanel activityBar = new JPanel();
        activityBar.setBackground(BG_ACTIVITY);
        activityBar.setPreferredSize(new Dimension(50, 0));
        activityBar.setBorder(new MatteBorder(0, 0, 0, 1, BORDER_COLOR));

        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(BG_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(new MatteBorder(0, 0, 0, 1, BORDER_COLOR));

        JLabel explorerTitle = new JLabel(" EXPLORER");
        explorerTitle.setForeground(FG_TEXT);
        explorerTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        explorerTitle.setBorder(new EmptyBorder(10, 5, 10, 5));

        JPanel fileListPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        fileListPanel.setBackground(BG_SIDEBAR);

        openFileBtn = new JButton("+ Open Folder / .yal");
        openFileBtn.setUI(new BasicButtonUI());
        openFileBtn.setBackground(new Color(14, 99, 156));
        openFileBtn.setForeground(Color.WHITE);
        openFileBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        openFileBtn.setFocusPainted(false);
        openFileBtn.setBorder(new EmptyBorder(10, 18, 10, 18));
        openFileBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        fileListPanel.add(openFileBtn);

        sidebar.add(explorerTitle, BorderLayout.NORTH);
        sidebar.add(fileListPanel, BorderLayout.CENTER);

        leftContainer.add(activityBar, BorderLayout.WEST);
        leftContainer.add(sidebar, BorderLayout.CENTER);

        JPanel editorPanel = new JPanel(new BorderLayout());
        editorPanel.setBackground(BG_EDITOR);

        JPanel tabsBar = new JPanel(new BorderLayout());
        tabsBar.setBackground(BG_TABS);
        tabsBar.setPreferredSize(new Dimension(0, 35));

        JLabel tabsTitle = new JLabel(" EDITOR");
        tabsTitle.setForeground(FG_TEXT);
        tabsTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabsTitle.setBorder(new EmptyBorder(0, 10, 0, 0));

        JPanel runPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        runPanel.setBackground(BG_TABS);

        runBtn = new JButton("Run  \u25B6");
        runBtn.setToolTipText("Run Compile Task...");
        runBtn.setUI(new BasicButtonUI());
        runBtn.setBackground(RUN_GREEN);
        runBtn.setForeground(Color.WHITE);
        runBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        runBtn.setOpaque(true);
        runBtn.setBorder(new EmptyBorder(10, 16, 10, 16));
        runBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        runPanel.add(runBtn);

        tabsBar.add(tabsTitle, BorderLayout.WEST);
        tabsBar.add(runPanel, BorderLayout.EAST);

        editorTabs = new JTabbedPane();
        editorTabs.setBackground(BG_TABS);
        editorTabs.setForeground(FG_TEXT);
        editorTabs.setFont(new Font("Segoe UI", Font.BOLD, 13));
        UIManager.put("TabbedPane.selected", BG_EDITOR);
        UIManager.put("TabbedPane.contentAreaColor", BG_EDITOR);
        UIManager.put("TabbedPane.foreground", FG_TEXT);
        UIManager.put("TabbedPane.selectedForeground", FG_TEXT_ACTIVE);
        editorTabs.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_COLOR));
        editorTabs.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                refreshTabContrast();
            }
        });

        editorTabs.addTab("welcome", wrappedArea(createReadonlyArea(
                "\n  // Haz clic en 'Open Folder / .yal' para cargar un archivo en el editor...\n")));
        setStyledTabTitle(editorTabs.getTabCount() - 1, "welcome");
        addLexerTestTab();
        refreshTabContrast();

        editorPanel.add(tabsBar, BorderLayout.NORTH);
        editorPanel.add(editorTabs, BorderLayout.CENTER);

        JPanel bottomContainer = new JPanel(new BorderLayout());
        bottomContainer.setPreferredSize(new Dimension(0, 220));
        bottomContainer.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_COLOR));

        JPanel terminalTabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 8));
        terminalTabs.setBackground(BG_EDITOR);

        JLabel problemsLabel = new JLabel("PROBLEMS");
        problemsLabel.setForeground(new Color(120, 120, 120));
        problemsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        JLabel termLabel = new JLabel("TERMINAL");
        termLabel.setForeground(FG_TEXT);
        termLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        termLabel.setBorder(new MatteBorder(0, 0, 1, 0, FG_TEXT));

        terminalTabs.add(problemsLabel);
        terminalTabs.add(termLabel);

        terminalArea = new JTextPane();
        terminalArea.setBackground(BG_EDITOR);
        terminalArea.setForeground(FG_TEXT);
        terminalArea.setCaretColor(Color.WHITE);
        terminalArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        terminalArea.setMargin(new Insets(5, 15, 5, 15));
        terminalArea.setEditable(false);

        JScrollPane terminalScroll = new JScrollPane(terminalArea);
        terminalScroll.setBorder(null);

        bottomContainer.add(terminalTabs, BorderLayout.NORTH);
        bottomContainer.add(terminalScroll, BorderLayout.CENTER);

        JSplitPane splitH = new JSplitPane(JSplitPane.VERTICAL_SPLIT, editorPanel, bottomContainer);
        splitH.setDividerSize(3);
        splitH.setResizeWeight(0.65);
        splitH.setBorder(null);
        splitH.setBackground(BG_EDITOR);

        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(new Color(0, 122, 204));
        statusBar.setPreferredSize(new Dimension(0, 22));
        statusLabel = new JLabel(" Ready         UTF-8    YALex");
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setBorder(new EmptyBorder(0, 10, 0, 10));
        statusBar.add(statusLabel, BorderLayout.WEST);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(leftContainer, BorderLayout.WEST);
        getContentPane().add(splitH, BorderLayout.CENTER);
        getContentPane().add(statusBar, BorderLayout.SOUTH);
    }

    public void closeGeneratedTabs() {
        for (int i = editorTabs.getTabCount() - 1; i >= 0; i--) {
            String title = editorTabs.getTitleAt(i);
            if (title != null && (title.startsWith("py:") || title.startsWith("dfagraph:") || title.startsWith("dfa:"))) {
                editorTabs.removeTabAt(i);
            }
        }
        refreshTabContrast();
    }

    public void openYalTab(String fileName, String content) {
        closeGeneratedTabs();
        if (yalEditorArea == null) {
            yalEditorArea = createEditableArea();
            removeWelcomeTabIfPresent();
            editorTabs.addTab(fileName, wrappedArea(yalEditorArea));
            setStyledTabTitle(editorTabs.getTabCount() - 1, fileName);
        } else {
            int idx = findTabIndexByComponent(yalEditorArea);
            if (idx >= 0) {
                setStyledTabTitle(idx, fileName);
            }
        }
        yalTabTitle = fileName;
        yalEditorArea.setText(content);
        yalEditorArea.setCaretPosition(0);
        focusYalTab();
    }

    public void setYalTabDirty(boolean dirty) {
        if (yalEditorArea == null) {
            return;
        }
        int idx = findTabIndexByComponent(yalEditorArea);
        if (idx < 0) {
            return;
        }
        String title = dirty ? yalTabTitle + " *" : yalTabTitle;
        setStyledTabTitle(idx, title);
        refreshTabContrast();
    }

    public void showGeneratedPythonTab(String fileName, String content) {
        showOrUpdateReadOnlyTab("py:" + fileName, fileName, content);
    }

    public void showDiagramTab(String tabName, String content) {
        showOrUpdateReadOnlyTab("dfa:" + tabName, tabName, content);
    }

    /**
     * Muestra un tab con un panel gráfico interactivo del DFA combinado.
     * Si ya existe un tab DFA-graph, lo reemplaza.
     */
    public void showDfaGraphTab(String tabName, DfaGraphPanel panel) {
        String prefix = "dfagraph:";
        for (int i = 0; i < editorTabs.getTabCount(); i++) {
            if (editorTabs.getTitleAt(i).startsWith(prefix)) {
                editorTabs.setComponentAt(i, panel);
                setStyledTabTitle(i, prefix + tabName);
                editorTabs.setSelectedIndex(i);
                refreshTabContrast();
                return;
            }
        }
        editorTabs.addTab(prefix + tabName, panel);
        setStyledTabTitle(editorTabs.getTabCount() - 1, prefix + tabName);
        editorTabs.setSelectedIndex(editorTabs.getTabCount() - 1);
        refreshTabContrast();
    }

    private void showOrUpdateReadOnlyTab(String namePrefix, String fileName, String content) {
        for (int i = 0; i < editorTabs.getTabCount(); i++) {
            if (editorTabs.getTitleAt(i).startsWith(namePrefix)) {
                JScrollPane pane = (JScrollPane) editorTabs.getComponentAt(i);
                JTextArea area = (JTextArea) pane.getViewport().getView();
                area.setText(content);
                area.setCaretPosition(0);
                editorTabs.setSelectedIndex(i);
                refreshTabContrast();
                return;
            }
        }

        JTextArea area = createReadonlyArea(content);
        editorTabs.addTab(namePrefix + " " + fileName, wrappedArea(area));
        setStyledTabTitle(editorTabs.getTabCount() - 1, namePrefix + " " + fileName);
        editorTabs.setSelectedIndex(editorTabs.getTabCount() - 1);
        refreshTabContrast();
    }

    private void removeWelcomeTabIfPresent() {
        for (int i = 0; i < editorTabs.getTabCount(); i++) {
            if ("welcome".equals(editorTabs.getTitleAt(i))) {
                editorTabs.removeTabAt(i);
                refreshTabContrast();
                return;
            }
        }
    }

    private void focusYalTab() {
        if (yalEditorArea == null) {
            return;
        }
        int idx = findTabIndexByComponent(yalEditorArea);
        if (idx >= 0) {
            editorTabs.setSelectedIndex(idx);
            refreshTabContrast();
        }
    }

    private void refreshTabContrast() {
        int selected = editorTabs.getSelectedIndex();
        for (int i = 0; i < editorTabs.getTabCount(); i++) {
            editorTabs.setBackgroundAt(i, i == selected ? BG_EDITOR : BG_TABS);
            editorTabs.setForegroundAt(i, i == selected ? FG_TEXT_ACTIVE : FG_TEXT);

            var tabComp = editorTabs.getTabComponentAt(i);
            if (tabComp instanceof JPanel panel && panel.getComponentCount() > 0
                    && panel.getComponent(0) instanceof JLabel label) {
                panel.setBackground(i == selected ? BG_EDITOR : BG_TABS);
                label.setForeground(i == selected ? FG_TEXT_ACTIVE : FG_TEXT);
            }
        }
    }

    private void setStyledTabTitle(int index, String title) {
        editorTabs.setTitleAt(index, title);

        JPanel tab = new JPanel(new BorderLayout());
        tab.setOpaque(true);
        tab.setBorder(new EmptyBorder(4, 8, 4, 8));

        JLabel label = new JLabel(title, SwingConstants.LEFT);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));

        tab.add(label, BorderLayout.CENTER);
        editorTabs.setTabComponentAt(index, tab);
    }

    private int findTabIndexByComponent(JTextArea area) {
        for (int i = 0; i < editorTabs.getTabCount(); i++) {
            Component c = editorTabs.getComponentAt(i);
            if (!(c instanceof JScrollPane pane)) {
                continue;
            }
            if (pane.getViewport().getView() == area) {
                return i;
            }
        }
        return -1;
    }

    private void addLexerTestTab() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_EDITOR);
        root.putClientProperty("yalex.tabId", "lexerTest");

        JPanel north = new JPanel(new BorderLayout());
        north.setBackground(BG_TABS);
        north.setBorder(new EmptyBorder(8, 10, 8, 10));

        JLabel hint = new JLabel("Probar lexer — escribe el texto de entrada (stdin)");
        hint.setForeground(FG_TEXT);
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        lexerTestRunBtn = new JButton("Ejecutar lexer  \u25B6");
        lexerTestRunBtn.setToolTipText("Ejecuta tokenize_all() sobre el .py generado con Run (última compilación exitosa)");
        lexerTestRunBtn.setUI(new BasicButtonUI());
        lexerTestRunBtn.setBackground(RUN_GREEN);
        lexerTestRunBtn.setForeground(Color.WHITE);
        lexerTestRunBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lexerTestRunBtn.setOpaque(true);
        lexerTestRunBtn.setBorder(new EmptyBorder(8, 14, 8, 14));
        lexerTestRunBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRow.setOpaque(false);
        btnRow.add(lexerTestRunBtn);

        north.add(hint, BorderLayout.WEST);
        north.add(btnRow, BorderLayout.EAST);

        lexerTestInputArea = createEditableArea();
        lexerTestInputArea.setText("");
        lexerTestOutputArea = createReadonlyArea("");
        lexerTestOutputArea.setEditable(false);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setBackground(BG_EDITOR);
        JLabel inLabel = new JLabel("Entrada");
        inLabel.setForeground(FG_TEXT);
        inLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        inLabel.setBorder(new EmptyBorder(0, 2, 4, 0));
        top.add(inLabel, BorderLayout.NORTH);
        top.add(wrappedArea(lexerTestInputArea), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBackground(BG_EDITOR);
        JLabel outLabel = new JLabel("Salida (stdout / stderr)");
        outLabel.setForeground(FG_TEXT);
        outLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        outLabel.setBorder(new EmptyBorder(0, 2, 4, 0));
        bottom.add(outLabel, BorderLayout.NORTH);
        bottom.add(wrappedArea(lexerTestOutputArea), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, top, bottom);
        split.setResizeWeight(0.45);
        split.setDividerSize(4);
        split.setBorder(null);
        split.setBackground(BG_EDITOR);

        root.add(north, BorderLayout.NORTH);
        root.add(split, BorderLayout.CENTER);

        editorTabs.addTab("Probar lexer", root);
        setStyledTabTitle(editorTabs.getTabCount() - 1, "Probar lexer");
    }

    /**
     * Selecciona la pestaña donde se prueba el lexer generado.
     */
    public void selectLexerTestTab() {
        for (int i = 0; i < editorTabs.getTabCount(); i++) {
            Component c = editorTabs.getComponentAt(i);
            if (c instanceof JPanel p && "lexerTest".equals(p.getClientProperty("yalex.tabId"))) {
                editorTabs.setSelectedIndex(i);
                refreshTabContrast();
                return;
            }
        }
    }

    private JTextArea createEditableArea() {
        JTextArea area = new JTextArea();
        area.setEditable(true);
        area.setBackground(BG_EDITOR);
        area.setForeground(new Color(212, 212, 212));
        area.setCaretColor(Color.WHITE);
        area.setFont(new Font("Consolas", Font.PLAIN, 14));
        area.setMargin(new Insets(10, 15, 10, 10));
        return area;
    }

    private JTextArea createReadonlyArea(String content) {
        JTextArea area = createEditableArea();
        area.setEditable(false);
        area.setText(content);
        area.setCaretPosition(0);
        return area;
    }

    private JScrollPane wrappedArea(JTextArea area) {
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(null);
        return scroll;
    }

    public JButton getOpenFileBtn() {
        return openFileBtn;
    }

    public JButton getRunBtn() {
        return runBtn;
    }

    public JTextArea getYalEditorArea() {
        return yalEditorArea;
    }

    public JTextPane getTerminalArea() {
        return terminalArea;
    }

    public JLabel getStatusLabel() {
        return statusLabel;
    }

    public JButton getLexerTestRunBtn() {
        return lexerTestRunBtn;
    }

    public JTextArea getLexerTestInputArea() {
        return lexerTestInputArea;
    }

    public JTextArea getLexerTestOutputArea() {
        return lexerTestOutputArea;
    }
}
