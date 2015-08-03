package com.serotonin.goid.util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.WindowConstants;

public class ViewFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    
    private final TurnDispatcher dispatcher;
    private final Object mutex = new Object();
    private final JMenuItem startMenu;
    private final JMenuItem stopMenu;
    private final JMenuItem stepMenu;
    private final JMenuItem exitMenu;
    private final ViewPane viewPane;
    private EnvironmentRunner environmentRunner;
    
    private int sleepCountdown;
    
    public ViewFrame(final TurnDispatcher dispatcher) {
        this.dispatcher = dispatcher;
        
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu();
        startMenu = new JMenuItem();
        stopMenu = new JMenuItem();
        stepMenu = new JMenuItem();
        JSeparator separator = new JSeparator();
        exitMenu = new JMenuItem();
        viewPane = new ViewPane(mutex);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("Environment Viewer");
        
        menu.setText("Environment");
        startMenu.setText("Start");
        startMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (environmentRunner == null) {
                    environmentRunner = new EnvironmentRunner();
                    environmentRunner.start();
                }
                startMenu.setEnabled(false);
                stopMenu.setEnabled(true);
                stepMenu.setEnabled(false);
            }
        });
        menu.add(startMenu);

        stopMenu.setText("Stop");
        stopMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                EnvironmentRunner r = environmentRunner;
                if (r != null) {
                    r.terminate();
                    environmentRunner = null;
                }
                startMenu.setEnabled(true);
                stopMenu.setEnabled(false);
                stepMenu.setEnabled(true);
            }
        });
        stopMenu.setEnabled(false);
        menu.add(stopMenu);

        stepMenu.setText("Step");
        stepMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                next();
            }
        });
        menu.add(stepMenu);

        menu.add(separator);
        
        exitMenu.setText("Exit");
        exitMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                System.exit(0);
            }
        });
        menu.add(exitMenu);

        menuBar.add(menu);
        setJMenuBar(menuBar);
        
        getContentPane().add(viewPane, BorderLayout.CENTER);
        
        pack();
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension size = getSize();
        setLocation((screenSize.width - size.width) >> 1, (screenSize.height - size.height) >> 1);
        
        setVisible(true);
        
        viewPane.requestFocus();
        viewPane.repaint();
    }
    
    public void addRenderable(Renderable r) {
        viewPane.addRenderable(r);
    }
    
    public void removeRenderable(Renderable r) {
        viewPane.removeRenderable(r);
    }
    
    public void startEnv() {
        startMenu.doClick();
    }
    
    private void next() {
        synchronized (mutex) {
            dispatcher.next();
        }
        viewPane.repaint();
        
        if (sleepCountdown <= 0) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {}
            sleepCountdown = 10000;
        }
        sleepCountdown--;
    }
    
    class EnvironmentRunner extends Thread {
        private volatile boolean running = true;
        
        public void terminate() {
            running = false;
            try {
                join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        
        @Override
        public void run() {
            while (running)
                next();
        }
    }
    
//    public void addViewMouseListener(MouseListener l) {
//        viewPane.addMouseListener(viewPane.new TranslatingMouseListener(l));
//    }
//
//    public void addViewMouseMotionListener(MouseMotionListener l) {
//        viewPane.addMouseMotionListener(viewPane.new TranslatingMouseMotionListener(l));
//    }
//
//    public void init() {
//        java.awt.EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//                getContentPane().add(new ControlPane(), BorderLayout.NORTH);
//                getContentPane().add(viewPane, BorderLayout.CENTER);
//                
//                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//                Dimension size = getSize();
//                setLocation((screenSize.width - size.width) >> 1, (screenSize.height - size.height) >> 1);
//                
//                setVisible(true);
//                
//                if (autoStart)
//                    start();
//            }
//        });
//    }
//    
//    class ViewPane extends JComponent {
//        private static final long serialVersionUID = -1;
//        
//        @Override
//        public void paintComponent(Graphics g) {
//            Graphics2D g2 = (Graphics2D)g;
//            
//            Rectangle viewBounds = translateViewBounds();
//            g2.translate(-viewBounds.x, -viewBounds.y);
//            
//            // Clear the draw area.
//            g2.setColor(Color.BLACK);
//            g2.fillRect(viewBounds.x, viewBounds.y, viewBounds.width-1, viewBounds.height-1);
//            
//            renderableProcess.render(viewBounds, g2);
//        }
//        
//        private Rectangle translateViewBounds() {
//            Dimension size = getSize();
//            return new Rectangle(-size.width >> 1, -size.height >> 1, size.width, size.height);
//        }
//        
//        private MouseEvent translateMouseEvent(MouseEvent e) {
//            Rectangle viewBounds = translateViewBounds();
//            e.translatePoint(viewBounds.x, viewBounds.y);
//            return e;
//        }
//        
//        class TranslatingMouseListener extends MouseAdapter {
//            private final MouseListener innerListener;
//            
//            TranslatingMouseListener(MouseListener l) {
//                innerListener = l;
//            }
//            
//            @Override
//            public void mouseClicked(MouseEvent e) {
//                innerListener.mouseClicked(translateMouseEvent(e));
//            }
//
//            @Override
//            public void mouseEntered(MouseEvent e) {
//                innerListener.mouseEntered(translateMouseEvent(e));
//            }
//
//            @Override
//            public void mouseExited(MouseEvent e) {
//                innerListener.mouseExited(translateMouseEvent(e));
//            }
//
//            @Override
//            public void mousePressed(MouseEvent e) {
//                innerListener.mousePressed(translateMouseEvent(e));
//            }
//
//            @Override
//            public void mouseReleased(MouseEvent e) {
//                innerListener.mouseReleased(translateMouseEvent(e));
//            }
//        }
//        
//        class TranslatingMouseMotionListener extends MouseAdapter {
//            private final MouseMotionListener innerListener;
//            
//            TranslatingMouseMotionListener(MouseMotionListener l) {
//                innerListener = l;
//            }
//            
//            @Override
//            public void mouseDragged(MouseEvent e) {
//                innerListener.mouseDragged(translateMouseEvent(e));
//            }
//
//            @Override
//            public void mouseMoved(MouseEvent e) {
//                innerListener.mouseMoved(translateMouseEvent(e));
//            }
//        }
//    }
    
}
