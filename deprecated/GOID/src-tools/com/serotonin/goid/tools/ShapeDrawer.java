package com.serotonin.goid.tools;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import com.serotonin.goid.util.RenderUtils;
import com.serotonin.goid.util.Renderable;
import com.serotonin.goid.util.ViewPane;

public class ShapeDrawer extends JFrame {
    private static final long serialVersionUID = 1L;
    private final Object mutex = new Object();

    public static void main(String[] args) {
        new ShapeDrawer();
    }

    private final ViewPane viewPane;
    private final StatusPane statusPane;
    private final Obstacle obstacle;

    public ShapeDrawer() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("Shape drawer");

        viewPane = new ViewPane(mutex);
        statusPane = new StatusPane();

        getContentPane().add(viewPane, BorderLayout.CENTER);
        getContentPane().add(statusPane, BorderLayout.SOUTH);

        setSize(1000, 700);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension size = getSize();
        setLocation((screenSize.width - size.width) >> 1, (screenSize.height - size.height) >> 1);

        setVisible(true);

        viewPane.addMouseMotionListener(statusPane);
        MouseHandler mh = new MouseHandler();
        viewPane.addMouseListener(mh);
        viewPane.addMouseMotionListener(mh);
        viewPane.addKeyListener(new KeyHandler());

        obstacle = new Obstacle();
        viewPane.addRenderable(obstacle);

        init();
    }

    class KeyHandler implements KeyListener {
        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_CONTROL)
                viewPane.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            statusPane.repaint();
        }

        public void keyTyped(KeyEvent e) {
            if (e.getKeyChar() == KeyEvent.VK_DELETE) {
                synchronized (mutex) {
                    obstacle.removePoint();
                }
                viewPane.repaint();
            }
        }

        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_CONTROL)
                viewPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            else if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_D)
                obstacle.dump();
        }
    }

    class StatusPane extends JComponent implements MouseMotionListener {
        private static final long serialVersionUID = 1L;

        private int mx;
        private int my;
        private int y;

        @Override
        public void addNotify() {
            super.addNotify();
            FontMetrics fm = getFontMetrics(getFont());
            y = fm.getAscent() + fm.getLeading();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(0, getFontMetrics(getFont()).getHeight() + 2);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics;
            StringBuilder sb = new StringBuilder();
            sb.append("Mag: ").append(viewPane.getScale());
            sb.append("    Coord: ").append(mx).append(',').append(my);
            sb.append("    Points: ").append(obstacle.getPointCount());
            g.drawString(sb.toString(), 5, y);
        }

        public void mouseMoved(MouseEvent e) {
            Point tf = getTransformedPoint(e.getX(), e.getY());
            mx = tf.x;
            my = tf.y;
            repaint();
        }

        public void mouseDragged(MouseEvent e) {
        }
    }

    Point getTransformedPoint(int x, int y) {
        return new Point((int) ((x - viewPane.getTranslateX()) / viewPane.getScale()), (int) ((y - (int) viewPane
                .getTranslateY()) / viewPane.getScale()));
    }

    class MouseHandler extends MouseAdapter {
        private Point draggingPoint;

        @Override
        public void mouseDragged(MouseEvent e) {
            if (draggingPoint != null) {
                Point tp = getTransformedPoint(e.getX(), e.getY());
                synchronized (mutex) {
                    draggingPoint.x = tp.x;
                    draggingPoint.y = tp.y;
                    obstacle.updateShape();
                }
                viewPane.repaint();
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (e.isControlDown()) {
                Point tp = getTransformedPoint(e.getX(), e.getY());
                synchronized (mutex) {
                    draggingPoint = obstacle.getPointAt(tp);
                    if (draggingPoint == null) {
                        draggingPoint = tp;
                        obstacle.addPoint(tp);
                    }
                    else
                        obstacle.makeLastPoint(draggingPoint);
                }
                viewPane.repaint();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (draggingPoint != null) {
                draggingPoint = null;
                viewPane.repaint();
            }
        }
    }

    static class Obstacle implements Renderable {
        private static final Color OUTLINE_COLOR = new Color(0x40, 0x20, 0);
        private static final Color HIGHLIGHT_COLOR = new Color(0xFF, 0xFF, 0);
        private static final Color FILL_COLOR = new Color(0x80, 0x40, 0);

        private Path2D shape;
        private final List<Point> points = new ArrayList<Point>();

        public void addPoint(int x, int y) {
            addPoint(new Point(x, y));
        }

        public void addPoint(Point p) {
            points.add(p);
            updateShape();
        }

        public void removePoint() {
            if (!points.isEmpty()) {
                points.remove(points.size() - 1);
                updateShape();
            }
        }

        public Point getPointAt(Point p) {
            for (Point point : points) {
                if (point.x >= p.x - 1 && point.x <= p.x + 1 && point.y >= p.y - 1 && point.y <= p.y + 1)
                    return point;
            }
            return null;
        }

        public int getPointCount() {
            return points.size();
        }

        public void makeLastPoint(Point p) {
            if (!points.contains(p))
                return;
            int size = points.size();
            while (points.get(size - 1) != p)
                points.add(0, points.remove(size - 1));
        }

        public void updateShape() {
            if (points.size() > 2) {
                shape = new Path2D.Float();

                shape.moveTo(points.get(0).getX(), points.get(0).getY());

                for (int i = 1; i < points.size(); i++)
                    shape.lineTo(points.get(i).getX(), points.get(i).getY());

                shape.closePath();
            }
            else
                shape = null;
        }

        public void dump() {
            for (Point point : points)
                System.out.println("{" + point.x + "," + point.y + "},");
            System.out.println();
        }

        public void render(Graphics2D g) {
            if (points.size() > 2)
                RenderUtils.renderOutlined(g, shape, FILL_COLOR, OUTLINE_COLOR);
            else if (points.size() == 2) {
                g.setColor(OUTLINE_COLOR);
                g.setStroke(new BasicStroke(2));
                g.drawLine(points.get(0).x, points.get(0).y, points.get(1).x, points.get(1).y);
            }

            if (points.size() > 0) {
                g.setStroke(new BasicStroke(3));
                g.setColor(OUTLINE_COLOR);
                Point p;
                for (int i = 0; i < points.size() - 1; i++) {
                    p = points.get(i);
                    g.drawLine(p.x, p.y, p.x, p.y);
                }

                g.setColor(HIGHLIGHT_COLOR);
                p = points.get(points.size() - 1);
                g.drawLine(p.x, p.y, p.x, p.y);
            }
        }
    }

    private void init() {
        for (int[] point : data)
            obstacle.addPoint(point[0], point[1]);

        // obstacle.addPoint(new Point(452,673));
        // obstacle.addPoint(new Point(488,693));
        // obstacle.addPoint(new Point(508,686));
        // obstacle.addPoint(new Point(525,694));
        // obstacle.addPoint(new Point(587,740));
        // obstacle.addPoint(new Point(682,751));
        // obstacle.addPoint(new Point(766,754));
        // obstacle.addPoint(new Point(837,727));
        // obstacle.addPoint(new Point(873,697));
        // obstacle.addPoint(new Point(914,696));
        // obstacle.addPoint(new Point(914,712));
        // obstacle.addPoint(new Point(902,718));
        // obstacle.addPoint(new Point(907,734));
        // obstacle.addPoint(new Point(924,741));
        // obstacle.addPoint(new Point(948,740));
        // obstacle.addPoint(new Point(961,727));
        // obstacle.addPoint(new Point(975,682));
        // obstacle.addPoint(new Point(975,565));
        // obstacle.addPoint(new Point(949,508));
        // obstacle.addPoint(new Point(958,456));
        // obstacle.addPoint(new Point(978,410));
        // obstacle.addPoint(new Point(1013,257));
        // obstacle.addPoint(new Point(974,183));
        // obstacle.addPoint(new Point(986,149));
        // obstacle.addPoint(new Point(969,119));
        // obstacle.addPoint(new Point(939,108));
        // obstacle.addPoint(new Point(891,105));
        // obstacle.addPoint(new Point(770,108));
        // obstacle.addPoint(new Point(667,133));
        // obstacle.addPoint(new Point(595,125));
        // obstacle.addPoint(new Point(472,131));
        // obstacle.addPoint(new Point(374,152));
        // obstacle.addPoint(new Point(271,171));
        // obstacle.addPoint(new Point(204,173));
        // obstacle.addPoint(new Point(129,181));
        // obstacle.addPoint(new Point(124,217));
        // obstacle.addPoint(new Point(104,256));
        // obstacle.addPoint(new Point(93,285));
        // obstacle.addPoint(new Point(88,327));
        // obstacle.addPoint(new Point(89,347));
        // obstacle.addPoint(new Point(102,360));
        // obstacle.addPoint(new Point(90,356));
        // obstacle.addPoint(new Point(57,359));
        // obstacle.addPoint(new Point(38,347));
        // obstacle.addPoint(new Point(3,349));
        // obstacle.addPoint(new Point(3,1));
        // obstacle.addPoint(new Point(1915,1));
        // obstacle.addPoint(new Point(1915,1119));
        // obstacle.addPoint(new Point(5,1119));
        // obstacle.addPoint(new Point(2,363));
        // obstacle.addPoint(new Point(43,356));
        // obstacle.addPoint(new Point(57,362));
        // obstacle.addPoint(new Point(81,359));
        // obstacle.addPoint(new Point(110,361));
        // obstacle.addPoint(new Point(115,366));
        // obstacle.addPoint(new Point(132,448));
        // obstacle.addPoint(new Point(130,553));
        // obstacle.addPoint(new Point(145,651));
        // obstacle.addPoint(new Point(210,723));
        // obstacle.addPoint(new Point(286,731));
        // obstacle.addPoint(new Point(437,737));
        // obstacle.addPoint(new Point(457,714));
        // obstacle.addPoint(new Point(451,693));
        // obstacle.addPoint(new Point(440,684));
        // obstacle.addPoint(new Point(431,691));
        // obstacle.addPoint(new Point(425,680));
        // viewPane.repaint();
    }

    static int[][] data = { { -1188, 63 }, { -1131, 28 }, { -1052, 18 }, { -988, 1 }, { -946, -21 }, { -876, -55 },
            { -824, -100 }, { -724, -121 }, { -655, -123 }, { -588, -104 }, { -534, -81 }, { -514, -63 },
            { -437, -43 }, { -386, -48 }, { -343, -75 }, { -321, -95 }, { -262, -131 }, { -190, -146 }, { -51, -159 },
            { 141, -153 }, { 197, -133 }, { 268, -108 }, { 389, -91 }, { 421, -115 }, { 461, -132 }, { 501, -133 },
            { 552, -116 }, { 603, -123 }, { 621, -139 }, { 655, -144 }, { 669, -144 }, { 682, -130 }, { 682, -106 },
            { 691, -83 }, { 696, 11 }, { 716, 57 }, { 777, 97 }, { 803, 123 }, { 813, 126 }, { 817, 132 },
            { 816, 139 }, { 804, 200 }, { 791, 288 }, { 788, 367 }, { 791, 403 }, { 813, 433 }, { 840, 457 },
            { 850, 503 }, { 845, 542 }, { 834, 547 }, { 824, 542 }, { 814, 530 }, { 789, 511 }, { 744, 522 },
            { 723, 528 }, { 690, 522 }, { 666, 517 }, { 647, 522 }, { 630, 538 }, { 625, 550 }, { 611, 554 },
            { 604, 547 }, { 603, 532 }, { 597, 510 }, { 597, 483 }, { 612, 443 }, { 646, 397 }, { 657, 349 },
            { 647, 266 }, { 617, 192 }, { 507, 152 }, { 456, 128 }, { 413, 99 }, { 374, 90 }, { 340, 91 },
            { 327, 107 }, { 305, 118 }, { 302, 112 }, { 282, 97 }, { 260, 88 }, { 238, 74 }, { 205, 64 }, { 172, 70 },
            { 143, 59 }, { 120, 42 }, { 82, 38 }, { 53, 28 }, { 44, 33 }, { 34, 44 }, { 33, 61 }, { 31, 96 },
            { 33, 150 }, { 45, 189 }, { 46, 219 }, { 15, 244 }, { -17, 243 }, { -52, 226 }, { -53, 181 }, { -71, 147 },
            { -92, 118 }, { -101, 93 }, { -93, 56 }, { -93, 22 }, { -143, 9 }, { -176, 2 }, { -230, 0 }, { -271, 13 },
            { -310, 41 }, { -341, 82 }, { -370, 108 }, { -416, 128 }, { -458, 142 }, { -484, 157 }, { -515, 167 },
            { -546, 151 }, { -585, 135 }, { -598, 115 }, { -612, 93 }, { -626, 77 }, { -648, 77 }, { -667, 86 },
            { -682, 112 }, { -684, 134 }, { -681, 158 }, { -642, 182 }, { -603, 182 }, { -591, 173 }, { -581, 161 },
            { -567, 166 }, { -562, 176 }, { -562, 186 }, { -572, 194 }, { -580, 217 }, { -599, 245 }, { -647, 263 },
            { -672, 284 }, { -709, 339 }, { -710, 379 }, { -707, 430 }, { -709, 473 }, { -725, 521 }, { -748, 583 },
            { -761, 637 }, { -759, 699 }, { -753, 776 }, { -709, 914 }, { -633, 977 }, { -582, 994 }, { -499, 1015 },
            { -391, 1007 }, { -342, 1001 }, { -330, 994 }, { -329, 973 }, { -341, 957 }, { -360, 943 }, { -382, 932 },
            { -392, 915 }, { -393, 896 }, { -378, 877 }, { -342, 868 }, { -299, 872 }, { -283, 896 }, { -271, 936 },
            { -253, 957 }, { -201, 977 }, { -128, 992 }, { -56, 986 }, { 23, 981 }, { 73, 985 }, { 124, 997 },
            { 200, 997 }, { 273, 1008 }, { 288, 1024 }, { 293, 1039 }, { 297, 1061 }, { 319, 1076 }, { 344, 1069 },
            { 367, 1053 }, { 396, 1025 }, { 414, 991 }, { 446, 948 }, { 486, 911 }, { 500, 863 }, { 525, 809 },
            { 563, 805 }, { 615, 797 }, { 653, 771 }, { 653, 743 }, { 664, 695 }, { 641, 653 }, { 623, 612 },
            { 636, 593 }, { 645, 578 }, { 654, 556 }, { 668, 550 }, { 689, 552 }, { 702, 558 }, { 712, 567 },
            { 736, 570 }, { 754, 569 }, { 779, 565 }, { 793, 560 }, { 810, 558 }, { 823, 556 }, { 837, 562 },
            { 847, 572 }, { 849, 583 }, { 849, 598 }, { 846, 609 }, { 837, 620 }, { 833, 632 }, { 830, 644 },
            { 830, 658 }, { 841, 688 }, { 855, 719 }, { 858, 755 }, { 843, 796 }, { 828, 825 }, { 821, 866 },
            { 809, 903 }, { 796, 944 }, { 776, 986 }, { 749, 1020 }, { 740, 1035 }, { 752, 1061 }, { 750, 1098 },
            { 748, 1174 }, { 725, 1292 }, { 702, 1423 }, { 600, 1479 }, { 531, 1510 }, { 405, 1533 }, { 299, 1534 },
            { 197, 1533 }, { -37, 1521 }, { -149, 1499 }, { -317, 1449 }, { -442, 1400 }, { -602, 1357 },
            { -759, 1307 }, { -913, 1241 }, { -931, 1229 }, { -948, 1189 }, { -953, 1160 }, { -971, 1133 },
            { -985, 1124 }, { -1006, 1100 }, { -1024, 1094 }, { -1037, 1094 }, { -1059, 1099 }, { -1074, 1109 },
            { -1085, 1119 }, { -1108, 1137 }, { -1122, 1159 }, { -1132, 1155 }, { -1139, 1140 }, { -1130, 1129 },
            { -1129, 1083 }, { -1146, 938 }, { -1176, 786 }, { -1180, 625 }, { -1156, 353 }, { -1179, 283 },
            { -1205, 251 }, { -1206, 179 }, { -1203, 131 }, };
}
