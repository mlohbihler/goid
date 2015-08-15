/*
 * Copyright (c) 2015, Serotonin Software Inc.
 *
 * This file is part of GoID.
 *
 * GoID is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of 
 * the License, or (at your option) any later version.
 *
 * GoID is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public 
 * License along with GoID. If not, see <http://www.gnu.org/licenses/>.
 */
package goid.simulation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author Matthew Lohbihler
 */
public class Main {
    public static void main(String[] args) {
        Environment env = new Environment();
        Viewer viewer = new Viewer(env);
        final Loop loop = new Loop(env, viewer);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                loop.terminate();
            }
        });

        // Create the toolbar
        JPanel toolbar = createToolbar(loop);

        // Create the control panel
        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.add(BorderLayout.CENTER, viewer);
        controlPanel.add(BorderLayout.NORTH, toolbar);

        // Create the frame
        JFrame f = new JFrame();
        f.setLocation(2000, 20);
        f.setSize(1000, 800);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setTitle("GoID");
        f.add(BorderLayout.CENTER, controlPanel);
        f.setBackground(Color.RED);
        f.setVisible(true);
    }

    static JPanel createToolbar(Loop loop) {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));

        final JButton auto = new JButton();
        final JButton step = new JButton();
        final JLabel framesLabel = new JLabel();
        final JSlider frameRate = new JSlider(1, 1000);

        auto.setAction(new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                if ("start".equals(e.getActionCommand())) {
                    auto.setActionCommand("stop");
                    auto.setText("Stop");
                    loop.envStart(toFrameRate(frameRate.getValue()));
                    step.setEnabled(false);
                }
                else {
                    auto.setActionCommand("start");
                    auto.setText("Start");
                    loop.envStop();
                    step.setEnabled(true);
                }
            }
        });
        auto.setActionCommand("start");
        auto.setText("Start");
        toolbar.add(auto);

        step.setAction(new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                loop.envStep();
            }
        });
        step.setText("Step");
        toolbar.add(step);

        frameRate.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int value = frameRate.getValue();
                framesLabel.setText("" + value + " i/s");
                loop.setEnvDelay(toFrameRate(value));
            }
        });
        frameRate.setValue(100);
        toolbar.add(frameRate);
        toolbar.add(framesLabel);

        return toolbar;
    }

    private static int toFrameRate(int ipers) {
        return (int) (1000D / ipers + 0.5);
    }
}
