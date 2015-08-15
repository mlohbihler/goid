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

/**
 * The thread that runs the environment updates and maintains the frame rate. Separated from the environment
 * so that the environment doesn't have to deal with this thread timing stuff.
 * 
 * The viewer runs in a separate thread from the environment, so that visual effects controlled by the viewer
 * can still run at a consistent frame rate regardless of the environment iteration rate.
 * 
 * @author Matthew Lohbihler
 */
public class Loop {
    private final Environment environment;

    private final AutoStepper viewerAuto;
    private AutoStepper envAuto;

    public Loop(Environment environment, Viewer viewer) {
        this.environment = environment;

        viewerAuto = new AutoStepper(17) {
            @Override
            void step() {
                viewer.repaint();
            }
        };
    }

    public synchronized void envStart(int delay) {
        if (envAuto == null) {
            envAuto = new AutoStepper(delay) {
                @Override
                void step() {
                    environment.update();
                }
            };
        }
    }

    public synchronized void envStop() {
        if (envAuto != null) {
            envAuto.terminate();
            envAuto = null;
        }
    }

    public void setEnvDelay(int delay) {
        if (envAuto != null)
            envAuto.delay = delay;
    }

    public void envStep() {
        environment.update();
    }

    public synchronized void terminate() {
        viewerAuto.terminate();
        if (envAuto != null)
            envAuto.terminate();
    }

    static abstract class AutoStepper implements Runnable {
        private volatile boolean running;
        private volatile int delay;
        private final Thread thread;

        AutoStepper(int delay) {
            this.delay = delay;
            running = true;
            thread = new Thread(this);
            thread.start();
        }

        void terminate() {
            running = false;
            synchronized (this) {
                notify();
            }
            try {
                thread.join();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            long lastRun = System.currentTimeMillis();

            while (running) {
                step();

                // Rest
                long nextRun = lastRun + delay;
                long now = System.currentTimeMillis();
                long wait = nextRun - now;
                if (wait > 0) {
                    synchronized (this) {
                        try {
                            wait(wait);
                        }
                        catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    lastRun = nextRun;
                }
                else if (wait == 0)
                    lastRun = nextRun;
                else {
                    lastRun = System.currentTimeMillis();
                }
            }
        }

        abstract void step();
    }
}
