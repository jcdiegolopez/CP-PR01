package com.yalex.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.yalex.automata.dfa.CombinedDfaBuilder;
import com.yalex.automata.dfa.DFA;

/**
 * Panel que renderiza un DFA combinado como grafo visual interactivo.
 * Soporta zoom (rueda del ratón) y pan (arrastrar).
 */
public class DfaGraphPanel extends javax.swing.JPanel {

    private static final Color BG = new Color(30, 30, 30);
    private static final Color EDGE_COLOR = new Color(160, 160, 160);
    private static final Color TEXT_COLOR = new Color(224, 224, 224);
    private static final Color INITIAL_ARROW_COLOR = new Color(79, 193, 255);
    private static final Color NODE_BORDER = new Color(200, 200, 200);
    private static final Color NODE_FILL_DEFAULT = new Color(50, 50, 55);
    private static final Font NODE_FONT = new Font("Consolas", Font.BOLD, 13);
    private static final Font LABEL_FONT = new Font("Consolas", Font.PLAIN, 11);
    private static final Font LEGEND_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 14);

    private static final int NODE_RADIUS = 22;
    private static final int ACCEPT_RING_GAP = 4;

    private static final Color[] RULE_COLORS = {
            new Color(46, 160, 67),    // green
            new Color(79, 193, 255),   // blue
            new Color(255, 123, 114),  // red
            new Color(255, 203, 107),  // yellow
            new Color(198, 120, 221),  // purple
            new Color(86, 182, 194),   // cyan
            new Color(255, 159, 67),   // orange
            new Color(152, 195, 121),  // light green
            new Color(224, 108, 117),  // salmon
            new Color(209, 154, 102),  // tan
    };

    private final DFA dfa;
    private final Map<Integer, Integer> stateRuleIndex;
    private final List<String> rulePatterns;

    // Layout: node positions in world coordinates
    private final double[] nodeX;
    private final double[] nodeY;

    // View transform
    private double zoom = 1.0;
    private double panX = 0;
    private double panY = 0;
    private Point lastDrag = null;

    // Grouped edges: (from,to) → list of chars
    private final Map<Long, List<Character>> groupedEdges;

    public DfaGraphPanel(CombinedDfaBuilder.Result result) {
        this.dfa = result.dfa();
        this.stateRuleIndex = result.stateRuleIndex();
        this.rulePatterns = result.rulePatterns();

        int n = dfa.getStateCount();
        nodeX = new double[n];
        nodeY = new double[n];

        computeLayout();

        groupedEdges = groupTransitions();

        setBackground(BG);
        setDoubleBuffered(true);

        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastDrag = e.getPoint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                lastDrag = null;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastDrag != null) {
                    panX += e.getX() - lastDrag.x;
                    panY += e.getY() - lastDrag.y;
                    lastDrag = e.getPoint();
                    repaint();
                }
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                double factor = e.getWheelRotation() < 0 ? 1.15 : 1.0 / 1.15;
                double mx = e.getX();
                double my = e.getY();
                panX = mx - factor * (mx - panX);
                panY = my - factor * (my - panY);
                zoom *= factor;
                zoom = Math.max(0.1, Math.min(zoom, 5.0));
                repaint();
            }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
        addMouseWheelListener(ma);
    }

    private void computeLayout() {
        int n = dfa.getStateCount();
        if (n == 0) {
            return;
        }
        if (n == 1) {
            nodeX[0] = 0;
            nodeY[0] = 0;
            return;
        }

        // Circular layout with initial state at left
        double radius = Math.max(100, 55.0 * n);
        for (int i = 0; i < n; i++) {
            double angle = 2 * Math.PI * i / n - Math.PI;
            nodeX[i] = radius * Math.cos(angle);
            nodeY[i] = radius * Math.sin(angle);
        }
    }

    private Map<Long, List<Character>> groupTransitions() {
        Map<Long, List<Character>> groups = new TreeMap<>();
        for (var fromEntry : dfa.getTransitionTable().entrySet()) {
            int from = fromEntry.getKey();
            for (var tr : fromEntry.getValue().entrySet()) {
                char sym = tr.getKey();
                int to = tr.getValue();
                long key = ((long) from << 32) | (to & 0xFFFFFFFFL);
                groups.computeIfAbsent(key, k -> new ArrayList<>()).add(sym);
            }
        }
        return groups;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        int w = getWidth();
        int h = getHeight();

        // Apply view transform: translate to center, then apply pan + zoom
        AffineTransform savedTx = g2.getTransform();
        g2.translate(w / 2.0 + panX, h / 2.0 + panY);
        g2.scale(zoom, zoom);

        drawEdges(g2);
        drawInitialArrow(g2);
        drawNodes(g2);

        g2.setTransform(savedTx);

        // Draw legend in screen-space (top-left corner)
        drawLegend(g2);
        drawTitle(g2);

        g2.dispose();
    }

    private void drawEdges(Graphics2D g2) {
        g2.setFont(LABEL_FONT);

        for (var entry : groupedEdges.entrySet()) {
            long key = entry.getKey();
            int from = (int) (key >> 32);
            int to = (int) (key & 0xFFFFFFFFL);
            List<Character> chars = entry.getValue();

            String label = formatCharGroup(chars);

            if (from == to) {
                drawSelfLoop(g2, from, label);
            } else {
                // Check if there's a reverse edge
                long reverseKey = ((long) to << 32) | (from & 0xFFFFFFFFL);
                boolean hasReverse = groupedEdges.containsKey(reverseKey);
                drawArrow(g2, from, to, label, hasReverse);
            }
        }
    }

    private void drawArrow(Graphics2D g2, int from, int to, String label, boolean curve) {
        double x1 = nodeX[from];
        double y1 = nodeY[from];
        double x2 = nodeX[to];
        double y2 = nodeY[to];

        double dx = x2 - x1;
        double dy = y2 - y1;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist < 1e-6) {
            return;
        }

        double ux = dx / dist;
        double uy = dy / dist;

        // Start and end at the edge of the nodes
        double sx = x1 + ux * NODE_RADIUS;
        double sy = y1 + uy * NODE_RADIUS;
        double ex = x2 - ux * NODE_RADIUS;
        double ey = y2 - uy * NODE_RADIUS;

        g2.setColor(EDGE_COLOR);
        g2.setStroke(new BasicStroke(1.3f));

        if (curve) {
            // Offset perpendicular for curved arrow
            double px = -uy;
            double py = ux;
            double offset = 12;

            double cx = (sx + ex) / 2 + px * offset;
            double cy = (sy + ey) / 2 + py * offset;

            Path2D path = new Path2D.Double();
            path.moveTo(sx, sy);
            path.quadTo(cx, cy, ex, ey);
            g2.draw(path);

            // Arrowhead at end
            double t = 0.95;
            double tx = (1 - t) * (1 - t) * sx + 2 * (1 - t) * t * cx + t * t * ex;
            double ty = (1 - t) * (1 - t) * sy + 2 * (1 - t) * t * cy + t * t * ey;
            double adx = ex - tx;
            double ady = ey - ty;
            double ad = Math.sqrt(adx * adx + ady * ady);
            if (ad > 0) {
                drawArrowHead(g2, ex, ey, adx / ad, ady / ad);
            }

            // Label at midpoint of curve
            double lx = cx;
            double ly = cy - 6;
            drawEdgeLabel(g2, label, lx, ly);
        } else {
            g2.draw(new Line2D.Double(sx, sy, ex, ey));
            drawArrowHead(g2, ex, ey, ux, uy);

            // Label at midpoint (offset perpendicular)
            double mx = (sx + ex) / 2;
            double my = (sy + ey) / 2;
            double px = -uy;
            double py = ux;
            drawEdgeLabel(g2, label, mx + px * 10, my + py * 10 - 2);
        }
    }

    private void drawArrowHead(Graphics2D g2, double ex, double ey, double ux, double uy) {
        double arrowLen = 8;
        double arrowAngle = Math.PI / 6;

        double lx = ex - arrowLen * (ux * Math.cos(arrowAngle) - uy * Math.sin(arrowAngle));
        double ly = ey - arrowLen * (uy * Math.cos(arrowAngle) + ux * Math.sin(arrowAngle));
        double rx = ex - arrowLen * (ux * Math.cos(arrowAngle) + uy * Math.sin(arrowAngle));
        double ry = ey - arrowLen * (uy * Math.cos(arrowAngle) - ux * Math.sin(arrowAngle));

        Path2D arrow = new Path2D.Double();
        arrow.moveTo(ex, ey);
        arrow.lineTo(lx, ly);
        arrow.moveTo(ex, ey);
        arrow.lineTo(rx, ry);
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(arrow);
    }

    private void drawSelfLoop(Graphics2D g2, int stateId, String label) {
        double x = nodeX[stateId];
        double y = nodeY[stateId];
        double loopR = 16;
        double loopY = y - NODE_RADIUS - loopR * 2 + 4;

        g2.setColor(EDGE_COLOR);
        g2.setStroke(new BasicStroke(1.3f));
        g2.draw(new Ellipse2D.Double(x - loopR, loopY, loopR * 2, loopR * 2));

        // Small arrowhead at reentry point
        drawArrowHead(g2, x + loopR * 0.7, y - NODE_RADIUS + 2, 0.5, 0.86);

        // Label above loop
        drawEdgeLabel(g2, label, x, loopY - 4);
    }

    private void drawEdgeLabel(Graphics2D g2, String label, double x, double y) {
        g2.setFont(LABEL_FONT);
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(label);

        // Background for readability
        g2.setColor(new Color(30, 30, 30, 200));
        g2.fillRoundRect((int) (x - tw / 2.0 - 3), (int) (y - fm.getAscent() - 1),
                tw + 6, fm.getHeight() + 2, 4, 4);

        g2.setColor(new Color(255, 203, 107));
        g2.drawString(label, (float) (x - tw / 2.0), (float) y);
    }

    private void drawInitialArrow(Graphics2D g2) {
        int init = dfa.getInitialStateId();
        double x = nodeX[init];
        double y = nodeY[init];

        double arrowLen = 40;
        double sx = x - NODE_RADIUS - arrowLen;
        double ex = x - NODE_RADIUS;

        g2.setColor(INITIAL_ARROW_COLOR);
        g2.setStroke(new BasicStroke(2.0f));
        g2.draw(new Line2D.Double(sx, y, ex, y));
        drawArrowHead(g2, ex, y, 1, 0);
    }

    private void drawNodes(Graphics2D g2) {
        int n = dfa.getStateCount();
        for (int i = 0; i < n; i++) {
            double x = nodeX[i];
            double y = nodeY[i];
            boolean isAccepting = dfa.getState(i).isAccepting();
            int ruleIdx = stateRuleIndex.getOrDefault(i, -1);

            // Fill color
            Color fill = NODE_FILL_DEFAULT;
            if (isAccepting && ruleIdx >= 0) {
                Color base = RULE_COLORS[ruleIdx % RULE_COLORS.length];
                fill = new Color(base.getRed() / 3, base.getGreen() / 3, base.getBlue() / 3, 200);
            }

            g2.setColor(fill);
            g2.fill(new Ellipse2D.Double(x - NODE_RADIUS, y - NODE_RADIUS,
                    NODE_RADIUS * 2, NODE_RADIUS * 2));

            // Border
            Color border = isAccepting && ruleIdx >= 0
                    ? RULE_COLORS[ruleIdx % RULE_COLORS.length]
                    : NODE_BORDER;
            g2.setColor(border);
            g2.setStroke(new BasicStroke(2.0f));
            g2.draw(new Ellipse2D.Double(x - NODE_RADIUS, y - NODE_RADIUS,
                    NODE_RADIUS * 2, NODE_RADIUS * 2));

            // Double circle for accepting states
            if (isAccepting) {
                int inner = NODE_RADIUS - ACCEPT_RING_GAP;
                g2.draw(new Ellipse2D.Double(x - inner, y - inner, inner * 2, inner * 2));
            }

            // State ID label
            String idStr = "q" + i;
            g2.setFont(NODE_FONT);
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(TEXT_COLOR);
            g2.drawString(idStr,
                    (float) (x - fm.stringWidth(idStr) / 2.0),
                    (float) (y + fm.getAscent() / 2.0 - 1));
        }
    }

    private void drawLegend(Graphics2D g2) {
        g2.setFont(LEGEND_FONT);
        FontMetrics fm = g2.getFontMetrics();

        int x = 15;
        int y = 45;
        int boxSize = 12;
        int lineHeight = 20;

        // Collect unique rule indices among accepting states
        Map<Integer, String> usedRules = new TreeMap<>();
        for (var entry : stateRuleIndex.entrySet()) {
            int ruleIdx = entry.getValue();
            if (ruleIdx >= 0 && ruleIdx < rulePatterns.size()) {
                usedRules.put(ruleIdx, rulePatterns.get(ruleIdx));
            }
        }

        if (usedRules.isEmpty()) {
            return;
        }

        // Background
        int legendW = 20;
        for (var entry : usedRules.entrySet()) {
            String text = "R" + entry.getKey() + ": " + truncate(entry.getValue(), 25);
            legendW = Math.max(legendW, fm.stringWidth(text) + boxSize + 20);
        }
        int legendH = usedRules.size() * lineHeight + 10;
        g2.setColor(new Color(37, 37, 38, 220));
        g2.fillRoundRect(x - 5, y - 5, legendW + 10, legendH + 10, 8, 8);
        g2.setColor(new Color(62, 62, 66));
        g2.drawRoundRect(x - 5, y - 5, legendW + 10, legendH + 10, 8, 8);

        for (var entry : usedRules.entrySet()) {
            int ruleIdx = entry.getKey();
            Color c = RULE_COLORS[ruleIdx % RULE_COLORS.length];

            g2.setColor(c);
            g2.fillRoundRect(x, y, boxSize, boxSize, 3, 3);

            g2.setColor(TEXT_COLOR);
            String text = "R" + ruleIdx + ": " + truncate(entry.getValue(), 25);
            g2.drawString(text, x + boxSize + 6, y + boxSize - 1);

            y += lineHeight;
        }
    }

    private void drawTitle(Graphics2D g2) {
        g2.setFont(TITLE_FONT);
        g2.setColor(TEXT_COLOR);
        String title = "Combined Lexer DFA (" + dfa.getStateCount() + " states)";
        g2.drawString(title, 15, 25);
    }

    // =========================================================================
    // Helpers — character grouping
    // =========================================================================

    static String formatCharGroup(List<Character> chars) {
        if (chars.isEmpty()) {
            return "";
        }
        if (chars.size() == 1) {
            return printableChar(chars.get(0));
        }

        // Sort chars and try to form ranges
        List<Character> sorted = new ArrayList<>(chars);
        Collections.sort(sorted);

        // If too many to display, use range notation
        if (sorted.size() > 6) {
            return buildRangeNotation(sorted);
        }

        // Otherwise comma-separated
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sorted.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(printableChar(sorted.get(i)));
        }
        return sb.toString();
    }

    private static String buildRangeNotation(List<Character> sorted) {
        StringBuilder sb = new StringBuilder("[");
        int i = 0;
        while (i < sorted.size()) {
            int start = i;
            while (i + 1 < sorted.size() && sorted.get(i + 1) == sorted.get(i) + 1) {
                i++;
            }
            if (i - start >= 2) {
                sb.append(printableChar(sorted.get(start)));
                sb.append("-");
                sb.append(printableChar(sorted.get(i)));
            } else {
                for (int j = start; j <= i; j++) {
                    sb.append(printableChar(sorted.get(j)));
                }
            }
            i++;
        }
        sb.append("]");
        return sb.toString();
    }

    private static String printableChar(char c) {
        return switch (c) {
            case '\n' -> "\\n";
            case '\t' -> "\\t";
            case '\r' -> "\\r";
            case ' ' -> "SP";
            default -> {
                if (c < 32 || c > 126) {
                    yield "x" + String.format("%02X", (int) c);
                }
                yield String.valueOf(c);
            }
        };
    }

    private static String truncate(String s, int max) {
        if (s.length() <= max) {
            return s;
        }
        return s.substring(0, max - 3) + "...";
    }
}
