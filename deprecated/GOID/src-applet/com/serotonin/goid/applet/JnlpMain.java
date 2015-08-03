package com.serotonin.goid.applet;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.jnlp.BasicService;
import javax.jnlp.ServiceManager;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.serotonin.json.JsonReader;
import com.serotonin.json.JsonWriter;
import com.serotonin.util.StringUtils;

public class JnlpMain implements LocalContext {
    public static void main(String[] args) throws Exception {
        // Arguments are all preceeded with an 'x' to make sure none of them are empty... something javaws has a big
        // problem with.
        String userId = args[0].substring(1);
        String taskId = args[1].substring(1);
        String taskClass = args[2].substring(1);

        new JnlpMain(userId, taskId, taskClass);
    }

    private final BasicService basicService;
    private final ContextUtils contextUtils;
    private final MainPanel viewerPanel;

    public JnlpMain(String userId, String taskId, String taskClass) throws Exception {
        basicService = (BasicService) ServiceManager.lookup("javax.jnlp.BasicService");
        contextUtils = new ContextUtils(basicService.getCodeBase(), userId, taskId, taskClass);
        viewerPanel = new MainPanel(this, taskClass);

        UserData utd = contextUtils.getUserData();

        FrameInfo frameInfo;
        if (StringUtils.isEmpty(utd.getFrameInfoStr()))
            frameInfo = new FrameInfo();
        else
            frameInfo = new JsonReader(utd.getFrameInfoStr()).read(FrameInfo.class);

        final JFrame frame = new JFrame("GoiD");

        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                new Thread() {
                    @Override
                    public void run() {
                        JComponent message = new JComponent() {
                            private static final long serialVersionUID = 1L;

                            @Override
                            protected void paintComponent(Graphics g) {
                                Dimension size = getSize();
                                g.setColor(Color.WHITE);
                                g.fillRect(0, 0, size.width, size.height);

                                g.setColor(getForeground());
                                g.drawRect(0, 0, size.width - 1, size.height - 1);

                                FontMetrics fm = getFontMetrics(getFont());
                                String message = "Saving settings...";
                                g.drawString(message, (size.width - fm.stringWidth(message)) >> 1, (size.height + fm
                                        .getAscent()) >> 1);
                            }
                        };

                        JPanel glass = (JPanel) frame.getGlassPane();
                        glass.setVisible(true);
                        glass.setLayout(null);
                        glass.add(message);

                        Dimension glassSize = glass.getSize();
                        message.setBounds((glassSize.width - 200) >> 1, (glassSize.height - 50) / 3, 200, 40);

                        FrameInfo frameInfo = new FrameInfo(frame);
                        JsonWriter writer = new JsonWriter();
                        String frameInfoStr;
                        try {
                            frameInfoStr = writer.write(frameInfo);
                            contextUtils.saveSettings(viewerPanel.getSplitLocation(), viewerPanel
                                    .getOutputSplitLocation(), frameInfoStr);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }

                        System.exit(0);
                    }
                }.start();
            }
        });

        frame.getContentPane().add(viewerPanel);
        frame.setLocation(frameInfo.getX(), frameInfo.getY());
        frame.setSize(frameInfo.getW(), frameInfo.getH());
        if (frameInfo.isMaximized())
            frame.setExtendedState(Frame.MAXIMIZED_BOTH);

        frame.setVisible(true);

        viewerPanel.init(utd.getSplitLocation(), utd.getOutputSplitLocation());
        viewerPanel.setScript(contextUtils.getSavedScript());
    }

    public static class FrameInfo {
        private int x = 100;
        private int y = 180;
        private int w = 1150;
        private int h = 1000;
        private boolean maximized = false;

        public FrameInfo() {
            // no op
        }

        public FrameInfo(JFrame frame) {
            if ((frame.getExtendedState() & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH)
                maximized = true;

            Point location = frame.getLocation();
            x = location.x;
            y = location.y;

            Dimension size = frame.getSize();
            w = size.width;
            h = size.height;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getW() {
            return w;
        }

        public void setW(int w) {
            this.w = w;
        }

        public int getH() {
            return h;
        }

        public void setH(int h) {
            this.h = h;
        }

        public boolean isMaximized() {
            return maximized;
        }

        public void setMaximized(boolean maximized) {
            this.maximized = maximized;
        }
    }

    public ImageIcon createImageIcon(String filename) {
        return contextUtils.createImageIcon(filename);
    }

    public String getSampleScript() {
        return contextUtils.getSampleScript();
    }

    public void saveScript(String script) {
        contextUtils.saveScript(script);
    }

    public String saveScore(int score, String resultDetails) {
        return contextUtils.saveScore(score, resultDetails);
    }

    public boolean isValidUser() {
        return contextUtils.isValidUser();
    }
}
