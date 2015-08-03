package com.serotonin.goid.applet;

public class UserData {
    private int splitLocation = 750;
    private int outputSplitLocation = 750;
    private String frameInfoStr = "";

    public int getSplitLocation() {
        return splitLocation;
    }

    public void setSplitLocation(int splitLocation) {
        this.splitLocation = splitLocation;
    }

    public int getOutputSplitLocation() {
        return outputSplitLocation;
    }

    public void setOutputSplitLocation(int outputSplitLocation) {
        this.outputSplitLocation = outputSplitLocation;
    }

    public String getFrameInfoStr() {
        return frameInfoStr;
    }

    public void setFrameInfoStr(String frameInfoStr) {
        this.frameInfoStr = frameInfoStr;
    }
}
