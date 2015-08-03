package com.serotonin.goid.task.donut;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.List;

import javax.script.CompiledScript;

import com.serotonin.goid.applet.Task;
import com.serotonin.goid.applet.TaskListener;
import com.serotonin.goid.util.BasicEnvironment;
import com.serotonin.goid.util.ControlScript;
import com.serotonin.goid.util.Environment;
import com.serotonin.goid.util2d.Circle;
import com.serotonin.goid.util2d.Obstacle;

public class DonutTask implements Task {
    private BugBody bugBody;
    private final ControlScript bugScript = new ControlScript();
    private BasicEnvironment environment;
    private CompletionMonitor bugTaskMonitor;
    private TaskListener taskListener;

    public DonutTask() {
        reset();
    }

    public double getInitialTranslationX() {
        return 150;
    }

    public double getInitialTranslationY() {
        return 300;
    }

    public void setScript(CompiledScript script) {
        bugScript.setScript(script);
    }

    public void clearScriptContext() {
        bugBody.resetContext();
        bugScript.resetContext();
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setTaskListener(TaskListener taskListener) {
        this.taskListener = taskListener;
        bugScript.setTaskListener(taskListener);
        if (bugTaskMonitor != null)
            bugTaskMonitor.setTaskListener(taskListener);
    }

    public void reset() {
        environment = new BasicEnvironment();
        bugBody = new BugBody(environment, bugScript, new Circle(0, 0, BugBody.RADIUS));
        bugBody.setOrientation(Math.PI * 3 / 2);

        bugTaskMonitor = new CompletionMonitor(bugBody, getMovementRegions());
        bugTaskMonitor.setTaskListener(taskListener);

        environment.add(bugTaskMonitor);
        environment.add(new Obstacle(getBoundary(), -200, -400));
        environment.add(new Obstacle(getIsland(), -50, -350));
        environment.add(bugBody);
    }

    public void setDisplayAgentInfo(boolean display) {
        bugBody.setDisplayAgentStates(display);
    }

    /*
    if (typeof(touchedAWall) == "undefined")
    touchedAWall = false;

    actuators.forwardMovement = 1;
    if (senses.obstacles) {
    touchedAWall = true;
    actuators.turn = 0.01;
    }
    else {
    if (touchedAWall)
        actuators.turn = -0.02;
    }




    if (typeof(touchedAWall) == "undefined") {
    touchedAWall = false;
    initTurn = true;
    }

    actuators.forwardMovement = 1;
    if (initTurn) {
    if (senses.orientation > 0.1)
        actuators.turn = -0.07;
    else
        initTurn = false;
    }
    else {
    if (senses.obstacles) {
        touchedAWall = true;
        actuators.turn = -0.1;
    }
    else {
        if (touchedAWall)
            actuators.turn = 0.04;
    }
    }

    */

    private List<Point> getBoundary() {
        List<Point> points = new ArrayList<Point>();
        points.add(new Point(682, 751));
        points.add(new Point(766, 754));
        points.add(new Point(837, 727));
        points.add(new Point(873, 697));
        points.add(new Point(914, 696));
        points.add(new Point(914, 712));
        points.add(new Point(902, 718));
        points.add(new Point(907, 734));
        points.add(new Point(924, 741));
        points.add(new Point(948, 740));
        points.add(new Point(961, 727));
        points.add(new Point(975, 682));
        points.add(new Point(975, 565));
        points.add(new Point(949, 508));
        points.add(new Point(958, 456));
        points.add(new Point(978, 410));
        points.add(new Point(1013, 257));
        points.add(new Point(974, 183));
        points.add(new Point(986, 149));
        points.add(new Point(969, 119));
        points.add(new Point(939, 108));
        points.add(new Point(891, 105));
        points.add(new Point(770, 108));
        points.add(new Point(667, 133));
        points.add(new Point(595, 125));
        points.add(new Point(472, 131));
        points.add(new Point(374, 152));
        points.add(new Point(271, 171));
        points.add(new Point(204, 173));
        points.add(new Point(129, 181));
        points.add(new Point(124, 217));
        points.add(new Point(104, 256));
        points.add(new Point(93, 285));
        points.add(new Point(88, 327));
        points.add(new Point(89, 347));
        points.add(new Point(102, 360));
        points.add(new Point(90, 356));
        points.add(new Point(57, 359));
        points.add(new Point(38, 347));
        points.add(new Point(3, 349));
        points.add(new Point(3, 1));
        points.add(new Point(1915, 1));
        points.add(new Point(1915, 1119));
        points.add(new Point(5, 1119));
        points.add(new Point(2, 363));
        points.add(new Point(43, 356));
        points.add(new Point(57, 362));
        points.add(new Point(81, 359));
        points.add(new Point(110, 361));
        points.add(new Point(115, 366));
        points.add(new Point(132, 448));
        points.add(new Point(130, 553));
        points.add(new Point(145, 651));
        points.add(new Point(210, 723));
        points.add(new Point(286, 731));
        points.add(new Point(437, 737));
        points.add(new Point(457, 714));
        points.add(new Point(451, 693));
        points.add(new Point(440, 684));
        points.add(new Point(431, 691));
        points.add(new Point(425, 680));
        points.add(new Point(452, 673));
        points.add(new Point(488, 693));
        points.add(new Point(508, 686));
        points.add(new Point(525, 694));
        points.add(new Point(587, 740));
        points.add(new Point(620, 725));
        points.add(new Point(620, 710));
        points.add(new Point(613, 694));
        points.add(new Point(613, 675));
        points.add(new Point(622, 667));
        points.add(new Point(625, 657));
        points.add(new Point(628, 643));
        points.add(new Point(637, 638));
        points.add(new Point(652, 638));
        points.add(new Point(660, 646));
        points.add(new Point(658, 659));
        points.add(new Point(650, 660));
        points.add(new Point(644, 657));
        points.add(new Point(640, 650));
        points.add(new Point(637, 655));
        points.add(new Point(633, 661));
        points.add(new Point(629, 673));
        points.add(new Point(629, 686));
        points.add(new Point(632, 705));
        points.add(new Point(644, 726));
        points.add(new Point(655, 741));
        return points;
    }

    private List<Point> getIsland() {
        List<Point> points = new ArrayList<Point>();
        points.add(new Point(624, 344));
        points.add(new Point(633, 335));
        points.add(new Point(646, 330));
        points.add(new Point(666, 332));
        points.add(new Point(678, 342));
        points.add(new Point(678, 360));
        points.add(new Point(676, 383));
        points.add(new Point(663, 408));
        points.add(new Point(639, 421));
        points.add(new Point(599, 429));
        points.add(new Point(538, 438));
        points.add(new Point(508, 460));
        points.add(new Point(445, 481));
        points.add(new Point(445, 519));
        points.add(new Point(480, 536));
        points.add(new Point(482, 552));
        points.add(new Point(465, 569));
        points.add(new Point(432, 574));
        points.add(new Point(381, 554));
        points.add(new Point(328, 524));
        points.add(new Point(285, 477));
        points.add(new Point(245, 424));
        points.add(new Point(224, 380));
        points.add(new Point(224, 356));
        points.add(new Point(242, 344));
        points.add(new Point(239, 317));
        points.add(new Point(251, 298));
        points.add(new Point(276, 283));
        points.add(new Point(325, 273));
        points.add(new Point(338, 257));
        points.add(new Point(371, 257));
        points.add(new Point(397, 270));
        points.add(new Point(420, 280));
        points.add(new Point(456, 272));
        points.add(new Point(468, 253));
        points.add(new Point(458, 238));
        points.add(new Point(444, 232));
        points.add(new Point(437, 225));
        points.add(new Point(438, 215));
        points.add(new Point(442, 212));
        points.add(new Point(450, 212));
        points.add(new Point(460, 214));
        points.add(new Point(470, 221));
        points.add(new Point(474, 230));
        points.add(new Point(491, 234));
        points.add(new Point(497, 249));
        points.add(new Point(506, 258));
        points.add(new Point(547, 275));
        points.add(new Point(591, 273));
        points.add(new Point(623, 265));
        points.add(new Point(659, 262));
        points.add(new Point(669, 270));
        points.add(new Point(677, 282));
        points.add(new Point(666, 303));
        points.add(new Point(645, 318));
        points.add(new Point(618, 331));
        points.add(new Point(598, 340));
        points.add(new Point(582, 347));
        points.add(new Point(575, 359));
        points.add(new Point(581, 373));
        points.add(new Point(589, 382));
        points.add(new Point(598, 383));
        points.add(new Point(609, 381));
        points.add(new Point(623, 380));
        points.add(new Point(629, 372));
        points.add(new Point(628, 358));
        points.add(new Point(619, 349));
        return points;
    }

    private List<Shape> getMovementRegions() {
        List<Shape> regions = new ArrayList<Shape>();
        regions.add(new Rectangle(250, -300, 300, 300));
        regions.add(new Rectangle(550, -300, 250, 350));
        regions.add(new Rectangle(550, 50, 250, 350));
        regions.add(new Rectangle(250, 50, 300, 350));
        regions.add(new Rectangle(-100, 50, 350, 350));
        regions.add(new Rectangle(-150, -300, 400, 350));
        return regions;
    }
}
