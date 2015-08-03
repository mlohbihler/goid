package com.serotonin.goid.applet;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import sun.org.mozilla.javascript.internal.RhinoException;

import com.serotonin.goid.util.TurnDispatcher;
import com.serotonin.goid.util.ViewPane;

public class MainPanel extends JPanel implements TaskListener {
    private static final long serialVersionUID = 1L;
    public static final Random RANDOM = new Random();

    private final LocalContext localContext;
    private final Task task;
    private boolean isRunMode;
    private boolean taskCompleted;

    private final Object mutex = new Object();
    private JButton nextButton;
    private JButton startButton;
    private JButton pauseButton;
    private JButton resetButton;
    private JButton fasterButton;
    private JButton slowerButton;
    private JButton saveButton;
    private JButton clearContextButton;
    private JToggleButton displayStateButton;
    private JButton runButton;
    private JButton cancelButton;
    private final ViewPane viewPane;
    final JTextPane scriptPane;
    final JSplitPane splitPane;
    final JSplitPane outputSplitPane;
    private final JTextPane outputPane;
    private final ScriptOutputHandler outputHandler = new ScriptOutputHandler();
    private EnvironmentRunner environmentRunner;

    private final UndoAction undoAction;
    private final RedoAction redoAction;
    private final Compilable engine;

    private TurnDispatcher dispatcher;

    public MainPanel(LocalContext localContext, String taskClass) throws Exception {
        super(new BorderLayout());

        this.localContext = localContext;

        Task task = null;
        try {
            task = (Task) Class.forName(taskClass).newInstance();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        this.task = task;
        task.setTaskListener(this);

        try {
            UIManager.setLookAndFeel((LookAndFeel) Class.forName("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel")
                    .newInstance());
        }
        catch (Exception e1) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
            catch (Exception e2) {
                e1.printStackTrace();
                e2.printStackTrace();
            }
        }

        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        engine = (Compilable) scriptEngineManager.getEngineByName("js");
        outputHandler.setListener(this);

        viewPane = new ViewPane(mutex);

        // Script side
        scriptPane = new JTextPane() {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean getScrollableTracksViewportWidth() {
                return false;
            }

            @Override
            public void setSize(Dimension d) {
                Dimension psize = getParent().getSize();
                if (d.width < psize.width)
                    super.setSize(psize.width, d.height);
                else
                    super.setSize(d);
            }

            @Override
            public void addNotify() {
                super.addNotify();

                // Set the tab stop.
                int charWidth = scriptPane.getFontMetrics(getFont()).charWidth('w');
                // A fakey way to do this, but there doesn't appear to be an obvious way to specify an infinite tab
                // expansion besides overriding TabSet. Another day...
                TabStop[] tabs = new TabStop[50];
                for (int i = 0; i < tabs.length; i++)
                    tabs[i] = new TabStop(4 * charWidth * (i + 1));
                TabSet tabSet = new TabSet(tabs);
                SimpleAttributeSet attributes = new SimpleAttributeSet();
                StyleConstants.setTabSet(attributes, tabSet);
                getStyledDocument().setParagraphAttributes(0, getDocument().getLength(), attributes, false);
            }
        };

        scriptPane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        scriptPane.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.isControlDown()) {
                    if (e.getKeyCode() == KeyEvent.VK_S)
                        saveScript();
                }
            }
        });

        //
        // Undo
        UndoManager manager = new UndoManager();
        manager.setLimit(1000);
        scriptPane.getDocument().addUndoableEditListener(manager);

        undoAction = new UndoAction(manager);
        redoAction = new RedoAction(manager);

        scriptPane.registerKeyboardAction(undoAction, KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK),
                JComponent.WHEN_FOCUSED);
        scriptPane.registerKeyboardAction(redoAction, KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK),
                JComponent.WHEN_FOCUSED);

        scriptPane.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                saveButton.setEnabled(true);
            }

            public void insertUpdate(DocumentEvent e) {
                saveButton.setEnabled(true);
            }

            public void removeUpdate(DocumentEvent e) {
                saveButton.setEnabled(true);
            }
        });

        //
        // Other stuff
        createToolbar();

        outputPane = new JTextPane();
        outputPane.setEditable(false);
        JScrollPane outputScroll = new JScrollPane(outputPane);
        outputScroll.setPreferredSize(new Dimension(0, 300));

        // Put it all together
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, viewPane, new JScrollPane(scriptPane));
        outputSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitPane, new JScrollPane(outputPane));
        add(outputSplitPane, BorderLayout.CENTER);

        scriptPane.requestFocus();

        resetDispatcher();
    }

    public void init(int splitLocation, int outputSplitLocation) {
        splitPane.setDividerLocation(splitLocation);
        outputSplitPane.setDividerLocation(outputSplitLocation);
    }

    public int getSplitLocation() {
        return splitPane.getDividerLocation();
    }

    public int getOutputSplitLocation() {
        return outputSplitPane.getDividerLocation();
    }

    public void resetDispatcher() {
        viewPane.removeRenderable(task.getEnvironment());

        task.reset();
        taskCompleted = false;
        task.clearScriptContext();
        task.setDisplayAgentInfo(displayStateButton.isSelected());

        dispatcher = new TurnDispatcher();
        dispatcher.addListener(task.getEnvironment());

        viewPane.addRenderable(task.getEnvironment());
        viewPane.setTranslation(task.getInitialTranslationX(), task.getInitialTranslationY());
        viewPane.setTime(dispatcher.getTime());
        viewPane.paintImmediately(0, 0, viewPane.getSize().width, viewPane.getSize().height);
    }

    public void setScript(String script) {
        scriptPane.setText(script);
        saveScript();
    }

    public void saveScript() {
        new Thread() {
            @Override
            public void run() {
                try {
                    CompiledScript script = engine.compile(scriptPane.getText());
                    synchronized (mutex) {
                        task.clearScriptContext();
                        task.setScript(script);
                    }
                    saveButton.setEnabled(false);

                    localContext.saveScript(scriptPane.getText());
                }
                catch (ScriptException e) {
                    showScriptException(e, "Compilation error");
                }
            }
        }.start();
    }

    public void scriptException(final ScriptException e) {
        // Run the exception display in a new thread because this thread is
        // probably holding the mutex.
        new Thread() {
            @Override
            public void run() {
                showScriptException(e, "Script error");
            }
        }.start();
    }

    public void scriptOutput(String message) {
        if (!isRunMode) {
            try {
                message += "\r\n";
                outputPane.getDocument().insertString(outputPane.getDocument().getEndPosition().getOffset(), message,
                        null);
            }
            catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
    }

    public void clearScriptOutput() {
        outputPane.setText("");
    }

    public void taskCompleted(final int score, final String resultDetails, final String message) {
        if (!taskCompleted) {
            // Mark the task as being completed so that the handling code below only gets called once. This method
            // can get called by the dispatching thread, which is a problem when we're trying to stop the dispatching
            // thread.
            taskCompleted = true;

            // Thread to display the result.
            new Thread() {
                @Override
                public void run() {
                    stopEnvironment();

                    String display = message;
                    if (isRunMode) {
                        display = localContext.saveScore(score, resultDetails);
                        isRunMode = false;
                        scriptPane.setEnabled(true);
                    }
                    else
                        display += " (Score not saved. Use 'run' mode.)";

                    nextButton.setEnabled(false);
                    startButton.setEnabled(false);
                    JOptionPane.showMessageDialog(viewPane, display, "Task completed", JOptionPane.INFORMATION_MESSAGE);
                }
            }.start();
        }
    }

    public void taskMessage(String message) {
        viewPane.displayMessage(message);
    }

    public ScriptOutputHandler getScriptOutputHandler() {
        return outputHandler;
    }

    public String getDispatcherTime() {
        return dispatcher.getTime();
    }

    private void showScriptException(ScriptException e, String title) {
        String message;
        if (e.getCause() instanceof RhinoException) {
            RhinoException re = (RhinoException) e.getCause();
            message = re.details() + ", line " + re.lineNumber() + ", column " + re.columnNumber();
        }
        else {
            if (e.getCause() != null)
                message = e.getCause().getMessage();
            else
                message = e.getMessage();

            if (e.getLineNumber() != -1)
                message += ", line: " + e.getLineNumber();
            if (e.getColumnNumber() != -1)
                message += ", col=" + e.getColumnNumber();
        }

        JOptionPane.showMessageDialog(viewPane, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private void startEnvironment() {
        if (environmentRunner == null) {
            environmentRunner = new EnvironmentRunner();
            environmentRunner.setDaemon(true);
            environmentRunner.start();
        }

        nextButton.setEnabled(false);
        startButton.setEnabled(false);
        if (isRunMode) {
            pauseButton.setEnabled(false);
            resetButton.setEnabled(false);

            saveButton.setEnabled(false);
            clearContextButton.setEnabled(false);

            runButton.setEnabled(false);
            cancelButton.setEnabled(true);
        }
        else {
            pauseButton.setEnabled(true);
            resetButton.setEnabled(true);
        }

        updateSpeedButtons();
    }

    public void updateSpeedButtons() {
        fasterButton.setEnabled(!isFastest());
        slowerButton.setEnabled(!isSlowest());
    }

    private void stopEnvironment() {
        EnvironmentRunner r = environmentRunner;
        if (r != null) {
            r.terminate();
            environmentRunner = null;
        }

        nextButton.setEnabled(true);
        startButton.setEnabled(true);
        pauseButton.setEnabled(false);

        if (isRunMode) {
            resetButton.setEnabled(true);

            saveButton.setEnabled(true);
            clearContextButton.setEnabled(true);

            runButton.setEnabled(true);
            cancelButton.setEnabled(false);
        }
    }

    private void next() {
        synchronized (mutex) {
            int loops = 1;
            if (delay == -1)
                loops = 3;
            else if (delay == -2)
                loops = 10;
            else if (delay == -3)
                loops = 20;
            else if (delay == -4)
                loops = 50;

            while (loops-- > 0)
                dispatcher.next();

            viewPane.setTime(dispatcher.getTime());
        }
        // viewPane.repaint();
        viewPane.paintImmediately(0, 0, viewPane.getSize().width, viewPane.getSize().height);
        Thread.yield();
    }

    //
    // 
    // Environment runner
    // 
    //
    int delay = 0;

    public void speedUp() {
        if (!isFastest())
            delay--;
        speedMessage();
    }

    public void slowDown() {
        if (!isSlowest())
            delay++;
        speedMessage();
    }

    private void speedMessage() {
        String message = "Normal speed";
        if (delay == -4)
            message = "Hyper fast";
        else if (delay == -3)
            message = "Very fast";
        else if (delay == -2)
            message = "Quite fast";
        else if (delay == -1)
            message = "Fast";
        else if (delay == 1)
            message = "Slow";
        else if (delay == 2)
            message = "Quite slow";
        else if (delay == 3)
            message = "Very slow";
        else if (delay == 4)
            message = "Glacial";

        boolean antialias = true;
        if (delay < 0)
            antialias = false;
        viewPane.setAntialias(antialias);

        viewPane.displayMessage(message);
    }

    public boolean isFastest() {
        return delay == -4;
    }

    public boolean isSlowest() {
        return delay == 4;
    }

    class EnvironmentRunner extends Thread {
        private volatile boolean running = true;

        public void terminate() {
            running = false;
            try {
                join();
            }
            catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void run() {
            while (running) {
                next();

                if (delay > 0) {
                    try {
                        if (delay == 1)
                            Thread.sleep(1);
                        else if (delay == 2)
                            Thread.sleep(10);
                        else if (delay == 3)
                            Thread.sleep(100);
                        else if (delay == 4)
                            Thread.sleep(1000);
                    }
                    catch (InterruptedException e) {
                    }
                }
            }
        }
    }

    //
    //
    // Menu
    //
    //
    private void createToolbar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        // Zoom in
        JButton zoomInButton = new JButton(createImageIcon("images/magnifier_zoom_in.png"));
        zoomInButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                viewPane.zoomIn();
            }
        });
        zoomInButton.setToolTipText("Zoom in (Ctrl++)");
        toolBar.add(zoomInButton);

        // Zoom out
        JButton zoomOutButton = new JButton(createImageIcon("images/magnifier_zoom_out.png"));
        zoomOutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                viewPane.zoomOut();
            }
        });
        zoomOutButton.setToolTipText("Zoom out (Ctrl+-)");
        toolBar.add(zoomOutButton);

        toolBar.addSeparator();

        // Next
        nextButton = new JButton(createImageIcon("images/control_end_blue.png"));
        nextButton.setDisabledIcon(createImageIcon("images/control_end.png"));
        nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                stopEnvironment();
                next();
                resetButton.setEnabled(true);
            }
        });
        nextButton.setToolTipText("Execute one turn");
        toolBar.add(nextButton);

        // Start
        startButton = new JButton(createImageIcon("images/control_play_blue.png"));
        startButton.setDisabledIcon(createImageIcon("images/control_play.png"));
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                startEnvironment();
                viewPane.displayMessage("test mode");
            }
        });
        startButton.setToolTipText("Execute in loop (test mode)");
        toolBar.add(startButton);

        // Pause
        pauseButton = new JButton(createImageIcon("images/control_pause_blue.png"));
        pauseButton.setDisabledIcon(createImageIcon("images/control_pause.png"));
        pauseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                stopEnvironment();
            }
        });
        pauseButton.setToolTipText("Pause loop");
        pauseButton.setEnabled(false);
        toolBar.add(pauseButton);

        // Reset
        resetButton = new JButton(createImageIcon("images/control_start_blue.png"));
        resetButton.setDisabledIcon(createImageIcon("images/control_start.png"));
        resetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                stopEnvironment();
                resetDispatcher();
                viewPane.repaint();
                resetButton.setEnabled(false);
            }
        });
        resetButton.setToolTipText("Stop and reset");
        resetButton.setEnabled(false);
        toolBar.add(resetButton);

        // Faster
        fasterButton = new JButton(createImageIcon("images/lightning_add.png"));
        fasterButton.setDisabledIcon(createImageIcon("images/lightning_add_grey.png"));
        fasterButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                speedUp();
                updateSpeedButtons();
            }
        });
        fasterButton.setToolTipText("Speed up loop execution");
        toolBar.add(fasterButton);

        // Slower
        slowerButton = new JButton(createImageIcon("images/lightning_delete.png"));
        slowerButton.setDisabledIcon(createImageIcon("images/lightning_delete_grey.png"));
        slowerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                slowDown();
                updateSpeedButtons();
            }
        });
        slowerButton.setToolTipText("Slow down loop execution");
        toolBar.add(slowerButton);

        toolBar.addSeparator();

        // Load sample script
        JButton loadScriptButton = new JButton(createImageIcon("images/script_lightning.png"));
        loadScriptButton.setDisabledIcon(createImageIcon("images/script_lightning_grey.png"));
        loadScriptButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                setScript(localContext.getSampleScript());
            }
        });
        loadScriptButton.setToolTipText("Load sample script");
        toolBar.add(loadScriptButton);

        // Save script
        saveButton = new JButton(createImageIcon("images/script_save.png"));
        saveButton.setDisabledIcon(createImageIcon("images/script_save_grey.png"));
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                saveScript();
            }
        });
        saveButton.setToolTipText("Save script to agent (Ctrl+S)");
        saveButton.setEnabled(false);
        toolBar.add(saveButton);

        // Clear script context
        clearContextButton = new JButton(createImageIcon("images/script_delete.png"));
        clearContextButton.setDisabledIcon(createImageIcon("images/script_delete_grey.png"));
        clearContextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                new Thread() {
                    @Override
                    public void run() {
                        synchronized (mutex) {
                            task.clearScriptContext();
                        }
                    }
                }.start();
            }
        });
        clearContextButton.setToolTipText("Clear script context");
        toolBar.add(clearContextButton);

        // Undo
        JButton undoButton = new JButton(createImageIcon("images/arrow_undo.png"));
        undoButton.setDisabledIcon(createImageIcon("images/arrow_undo_grey.png"));
        undoButton.addActionListener(undoAction);
        undoButton.setToolTipText("Undo");
        toolBar.add(undoButton);

        // Redo
        JButton redoButton = new JButton(createImageIcon("images/arrow_redo.png"));
        redoButton.setDisabledIcon(createImageIcon("images/arrow_redo_grey.png"));
        redoButton.addActionListener(redoAction);
        redoButton.setToolTipText("Redo");
        toolBar.add(redoButton);

        toolBar.addSeparator();

        // Clear the output
        displayStateButton = new JToggleButton(createImageIcon("images/information.png"));
        displayStateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                task.setDisplayAgentInfo(displayStateButton.isSelected());
                repaint();
            }
        });
        displayStateButton.setToolTipText("Display agent state(s)");
        toolBar.add(displayStateButton);

        // Clear the output
        JButton clearOutputButton = new JButton(createImageIcon("images/monitor_delete.png"));
        clearOutputButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                clearScriptOutput();
            }
        });
        clearOutputButton.setToolTipText("Clear output");
        toolBar.add(clearOutputButton);

        toolBar.addSeparator();

        // Run
        runButton = new JButton(createImageIcon("images/bullet_go.png"));
        runButton.setDisabledIcon(createImageIcon("images/bullet_go_grey.png"));
        runButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                stopEnvironment();
                resetDispatcher();
                clearScriptOutput();

                scriptPane.setEnabled(false);
                saveScript();

                isRunMode = true;
                startEnvironment();

                viewPane.displayMessage("running");
            }
        });
        if (localContext.isValidUser())
            runButton.setToolTipText("Run");
        else {
            runButton.setToolTipText("Run (disabled - please login or register)");
            runButton.setEnabled(false);
        }
        toolBar.add(runButton);

        // Cancel
        cancelButton = new JButton(createImageIcon("images/stop.png"));
        cancelButton.setDisabledIcon(createImageIcon("images/stop_grey.png"));
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                stopEnvironment();

                isRunMode = false;

                scriptPane.setEnabled(true);

                viewPane.displayMessage("stopped");
            }
        });
        cancelButton.setToolTipText("Cancel run and reset");
        cancelButton.setEnabled(false);
        toolBar.add(cancelButton);

        add(BorderLayout.NORTH, toolBar);
    }

    private ImageIcon createImageIcon(String filename) {
        return localContext.createImageIcon(filename);
    }

    class UndoAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        private final UndoManager manager;

        public UndoAction(UndoManager manager) {
            this.manager = manager;
        }

        public void actionPerformed(ActionEvent evt) {
            try {
                manager.undo();
            }
            catch (CannotUndoException e) {
            }
        }
    }

    class RedoAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        private final UndoManager manager;

        public RedoAction(UndoManager manager) {
            this.manager = manager;
        }

        public void actionPerformed(ActionEvent evt) {
            try {
                manager.redo();
            }
            catch (CannotRedoException e) {
            }
        }
    }
}
