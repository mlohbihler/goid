package com.serotonin.goid.applet;

import javax.swing.ImageIcon;

public interface LocalContext {
    public ImageIcon createImageIcon(String filename);

    public void saveScript(String script);

    public String getSampleScript();

    public String saveScore(int score, String resultDetails);

    public boolean isValidUser();
}
