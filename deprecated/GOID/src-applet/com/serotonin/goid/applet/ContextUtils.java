package com.serotonin.goid.applet;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;

import com.serotonin.io.StreamUtils;
import com.serotonin.util.StringUtils;
import com.serotonin.web.util.HttpUtils;

public class ContextUtils {
    private final HttpClient httpClient = HttpUtils.getHttpClient(5000);
    private final URL codebase;
    private final String userId;
    private final String taskId;
    private final String taskClass;

    public ContextUtils(URL codebase, String userId, String taskId, String taskClass) {
        this.codebase = codebase;
        if (StringUtils.isEmpty(userId))
            this.userId = null;
        else
            this.userId = userId;
        this.taskId = taskId;
        this.taskClass = taskClass;
    }

    public ImageIcon createImageIcon(String filename) {
        ClassLoader cl = getClass().getClassLoader();
        URL url = cl.getResource(filename);
        return new ImageIcon(url);
    }

    public boolean isValidUser() {
        return userId != null;
    }

    public void saveSettings(int splitLocation, int outputSplitLocation, String frameInfoStr) {
        if (isValidUser()) {
            PostMethod post = getPostMethod("/appletRes/settingsSave.php");
            post.addParameter("userId", userId);
            post.addParameter("splitLocation", Integer.toString(splitLocation));
            post.addParameter("outputSplitLocation", Integer.toString(outputSplitLocation));
            if (frameInfoStr != null)
                post.addParameter("frameInfo", frameInfoStr);
            executePost(post, true);
        }
    }

    public void saveScript(String script) {
        if (isValidUser()) {
            PostMethod post = getPostMethod("/appletRes/scriptSave.php");
            post.addParameter("userId", userId);
            post.addParameter("taskId", taskId);
            post.addParameter("script", script);
            executePost(post, true);
        }
    }

    public String saveScore(int score, String resultDetails) {
        if (isValidUser()) {
            PostMethod post = getPostMethod("/appletRes/taskCompletedSave.php");
            post.addParameter("userId", userId);
            post.addParameter("taskId", taskId);
            post.addParameter("score", Integer.toString(score));
            post.addParameter("resultDetails", resultDetails);

            String response = executePost(post, false);

            // Get the rank.
            int rank = StringUtils.parseInt(StringUtils.findGroup(Pattern.compile("rank=(\\d*)"), response), -1);
            int ties = StringUtils.parseInt(StringUtils.findGroup(Pattern.compile("ties=(\\d*)"), response), -1);
            String changedStr = StringUtils.findGroup(Pattern.compile("changed=(\\w*)"), response);
            boolean changed = "1".equals(changedStr) || "true".equals(changedStr);

            String rankDesc = "Your rank is " + rank;
            if (ties == 1)
                rankDesc += " (tied with 1 other)";
            else if (ties > 1)
                rankDesc += " (tied with " + ties + " others)";

            if (changed)
                return "Your new score has been saved. " + rankDesc;
            return "You did not beat your previous score. " + rankDesc;
        }
        return null;
    }

    public String getSampleScript() {
        // Replace the class name with the script name.
        String filename = taskClass.replaceAll("\\.", "/");
        filename = filename.substring(0, filename.lastIndexOf("/") + 1) + "sample.js";
        InputStream is = getClass().getClassLoader().getResourceAsStream(filename);
        StringWriter sw = new StringWriter();
        try {
            StreamUtils.transfer(new InputStreamReader(is), sw);
            is.close();
        }
        catch (IOException e) {
            e.printStackTrace();
            return "// Read op failed: " + e.getMessage();
        }
        return sw.toString();
    }

    public String getSavedScript() {
        if (isValidUser()) {
            PostMethod post = getPostMethod("/appletRes/getScript.php");
            post.addParameter("userId", userId);
            post.addParameter("taskId", taskId);
            return executePost(post, false);
        }
        return "";
    }

    public UserData getUserData() {
        UserData userData = new UserData();
        if (isValidUser()) {
            try {
                PostMethod post = getPostMethod("/appletRes/getUserData.php");
                post.addParameter("userId", userId);
                String response = executePost(post, false);
                String[] parts = response.split("\\|");
                if (parts.length == 3) {
                    userData.setSplitLocation(Integer.parseInt(parts[0]));
                    userData.setOutputSplitLocation(Integer.parseInt(parts[1]));
                    userData.setFrameInfoStr(parts[2]);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return userData;
    }

    public PostMethod getPostMethod(String path) {
        int port = codebase.getPort();
        String uri = codebase.getProtocol() + "://" + codebase.getHost();
        if (port != -1)
            uri += ":" + port;
        uri += path;

        return new PostMethod(uri);
    }

    private String executePost(PostMethod post, boolean dumpResponse) {
        try {
            int responseCode = httpClient.executeMethod(post);
            if (responseCode != HttpStatus.SC_OK)
                System.out.println("Invalid response code: " + responseCode);
            else {
                String response = HttpUtils.readFullResponseBody(post);
                if (dumpResponse) {
                    if (!StringUtils.isEmpty(response))
                        System.out.println("Save response: " + response);
                }
                else
                    return response;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            post.releaseConnection();
        }

        return null;
    }
}
