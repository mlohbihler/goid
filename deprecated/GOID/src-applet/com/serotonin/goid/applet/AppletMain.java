package com.serotonin.goid.applet;

import javax.swing.ImageIcon;
import javax.swing.JApplet;

import com.serotonin.util.StringUtils;

public class AppletMain extends JApplet implements LocalContext {
    private static final long serialVersionUID = 1L;

    private ContextUtils contextUtils;
    private MainPanel viewerPanel;

    @Override
    public void init() {
        String userId = getParameter("userId");
        String taskId = getParameter("taskId");
        String taskClass = getParameter("taskClass");

        contextUtils = new ContextUtils(getCodeBase(), userId, taskId, taskClass);

        int splitLocation = StringUtils.parseInt(getParameter("splitLocation"), 750);
        int outputSplitLocation = StringUtils.parseInt(getParameter("outputSplitLocation"), 750);

        try {
            viewerPanel = new MainPanel(this, taskClass);
            add(viewerPanel);
            viewerPanel.init(splitLocation, outputSplitLocation);
            viewerPanel.setScript(contextUtils.getSavedScript());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void destroy() {
        MainPanel localViewerPanel = viewerPanel;
        if (localViewerPanel != null)
            contextUtils.saveSettings(localViewerPanel.getSplitLocation(), localViewerPanel.getOutputSplitLocation(),
                    null);
    }

    @Override
    public void start() {
        // no op
    }

    @Override
    public void stop() {
        // no op
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
