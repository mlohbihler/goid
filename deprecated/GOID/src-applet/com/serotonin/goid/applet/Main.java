package com.serotonin.goid.applet;

import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import com.serotonin.io.StreamUtils;

public class Main implements LocalContext {
    public static void main(String[] args) throws Exception {
        new Main();
    }

    private final String taskClass;

    public Main() throws Exception {
        // taskClass = "com.serotonin.goid.task.test.TestTask";
        taskClass = "com.serotonin.goid.task.donut.DonutTask";
        // taskClass = "com.serotonin.goid.task.collector.CollectorTask";
        //        taskClass = "com.serotonin.goid.task.arm.ArmTask";
        MainPanel mainPanel = new MainPanel(this, taskClass);

        JFrame frame = new JFrame("GoiD");
        frame.getContentPane().add(mainPanel);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocation(2000, 180);
        frame.setSize(1150, 1000);
        frame.setVisible(true);

        mainPanel.init(750, 750);
    }

    @Override
    public ImageIcon createImageIcon(String filename) {
        return new ImageIcon(filename);
    }

    @Override
    public void saveScript(String script) {
        System.out.println("Script save not implemented");
    }

    @Override
    public String saveScore(int score, String resultDetails) {
        System.out.println("Score save not implemented");
        return null;
    }

    @Override
    public String getSampleScript() {
        // Replace the class name with the script name.
        String filename = taskClass.replaceAll("\\.", "/");
        filename = "src/" + filename.substring(0, filename.lastIndexOf("/") + 1) + "sample.js";
        StringWriter sw = new StringWriter();
        try {
            FileReader fr = new FileReader(filename);
            StreamUtils.transfer(fr, sw);
            fr.close();
        }
        catch (IOException e) {
            e.printStackTrace();
            return "// Read op failed: " + e.getMessage();
        }
        return sw.toString();
    }

    @Override
    public boolean isValidUser() {
        return true;
    }
}
