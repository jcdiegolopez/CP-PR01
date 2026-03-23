package com.yalex.gui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

/**
 * Visual Studio Code literal clone for YALex
 */
public class YalexGui extends JFrame {

    // Paleta literal de VS Code Dark+
    private static final Color BG_ACTIVITY = new Color(51, 51, 51);    // #333333
    private static final Color BG_SIDEBAR = new Color(37, 37, 38);     // #252526
    private static final Color BG_EDITOR = new Color(30, 30, 30);      // #1E1E1E
    private static final Color BG_TABS = new Color(45, 45, 45);        // #2D2D2D
    private static final Color FG_TEXT = new Color(204, 204, 204);     // #CCCCCC
    private static final Color FG_BLUE = new Color(79, 193, 255);      // #4FC1FF
    private static final Color BORDER_COLOR = new Color(62, 62, 66);   // #3E3E42

    private final JButton openFileBtn;
    private final JButton runBtn;
    private final JTextArea editorArea;
    private final JTextPane terminalArea;
    private final JLabel tabLabel;
    private final JLabel statusLabel;

    public YalexGui() {
        super("YALex - Visual Studio Code");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_EDITOR);

        // --- LEFT = Activity Bar + Sidebar ---
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
        explorerTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        explorerTitle.setBorder(new EmptyBorder(10, 5, 10, 5));
        
        JPanel fileListPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        fileListPanel.setBackground(BG_SIDEBAR);
        
        openFileBtn = new JButton("+ Open Folder / .yal");
        openFileBtn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        openFileBtn.setBackground(new Color(14, 99, 156));
        openFileBtn.setForeground(Color.WHITE);
        openFileBtn.setFocusPainted(false);
        openFileBtn.setBorder(new EmptyBorder(6, 15, 6, 15));
        openFileBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        fileListPanel.add(openFileBtn);

        sidebar.add(explorerTitle, BorderLayout.NORTH);
        sidebar.add(fileListPanel, BorderLayout.CENTER);

        leftContainer.add(activityBar, BorderLayout.WEST);
        leftContainer.add(sidebar, BorderLayout.CENTER);

        // --- CENTER = Editor ---
        JPanel editorPanel = new JPanel(new BorderLayout());
        editorPanel.setBackground(BG_EDITOR);

        JPanel tabsBar = new JPanel(new BorderLayout());
        tabsBar.setBackground(BG_TABS);
        tabsBar.setPreferredSize(new Dimension(0, 35));

        JPanel activeTab = new JPanel(new BorderLayout());
        activeTab.setBackground(BG_EDITOR); // active tab is darker like editor
        activeTab.setBorder(new MatteBorder(0, 0, 1, 0, FG_BLUE));
        tabLabel = new JLabel(" untitled.yal  \u2715");
        tabLabel.setForeground(FG_TEXT);
        tabLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabLabel.setBorder(new EmptyBorder(0, 15, 0, 15));
        activeTab.add(tabLabel, BorderLayout.CENTER);
        
        JPanel tabWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabWrapper.setBackground(BG_TABS);
        tabWrapper.add(activeTab);

        JPanel runPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        runPanel.setBackground(BG_TABS);
        
        runBtn = new JButton("\u25B6");
        runBtn.setToolTipText("Run Compile Task...");
        runBtn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        runBtn.setBackground(BG_TABS);
        runBtn.setForeground(FG_TEXT);
        runBtn.setBorder(new EmptyBorder(8, 10, 8, 10));
        runBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        runPanel.add(runBtn);

        tabsBar.add(tabWrapper, BorderLayout.WEST);
        tabsBar.add(runPanel, BorderLayout.EAST);

        editorArea = new JTextArea("\n  // Haz clic en 'Open Folder / .yal' para cargar un archivo en el editor...\n");
        editorArea.setBackground(BG_EDITOR);
        editorArea.setForeground(new Color(212, 212, 212));
        editorArea.setCaretColor(Color.WHITE);
        editorArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        editorArea.setMargin(new Insets(10, 15, 10, 10));
        
        JScrollPane editorScroll = new JScrollPane(editorArea);
        editorScroll.setBorder(null);

        editorPanel.add(tabsBar, BorderLayout.NORTH);
        editorPanel.add(editorScroll, BorderLayout.CENTER);

        // --- BOTTOM = Terminal Panel ---
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
        termLabel.setBorder(new MatteBorder(0, 0, 1, 0, FG_TEXT)); // Active tab line
        
        terminalTabs.add(problemsLabel);
        terminalTabs.add(termLabel);

        terminalArea = new JTextPane();
        terminalArea.setBackground(BG_EDITOR);
        terminalArea.setForeground(FG_TEXT);
        terminalArea.setCaretColor(Color.WHITE);
        terminalArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        terminalArea.setMargin(new Insets(5, 15, 5, 15));
        
        JScrollPane terminalScroll = new JScrollPane(terminalArea);
        terminalScroll.setBorder(null);

        bottomContainer.add(terminalTabs, BorderLayout.NORTH);
        bottomContainer.add(terminalScroll, BorderLayout.CENTER);

        // --- SPLIT PANE ---
        JSplitPane splitH = new JSplitPane(JSplitPane.VERTICAL_SPLIT, editorPanel, bottomContainer);
        splitH.setDividerSize(3);
        splitH.setResizeWeight(0.65); // 65% editor, 35% terminal
        splitH.setBorder(null);
        splitH.setBackground(BG_EDITOR);

        // --- Status Bar ---
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(new Color(0, 122, 204)); // VSCode blue status bar
        statusBar.setPreferredSize(new Dimension(0, 22));
        statusLabel = new JLabel(" Ready         UTF-8    YALex");
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setBorder(new EmptyBorder(0, 10, 0, 10));
        statusBar.add(statusLabel, BorderLayout.WEST);

        // Root Layout
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(leftContainer, BorderLayout.WEST);
        getContentPane().add(splitH, BorderLayout.CENTER);
        getContentPane().add(statusBar, BorderLayout.SOUTH);
    }

    public JButton getOpenFileBtn() { return openFileBtn; }
    public JButton getRunBtn() { return runBtn; }
    public JTextArea getEditorArea() { return editorArea; }
    public JTextPane getTerminalArea() { return terminalArea; }
    public JLabel getTabLabel() { return tabLabel; }
    public JLabel getStatusLabel() { return statusLabel; }
}
