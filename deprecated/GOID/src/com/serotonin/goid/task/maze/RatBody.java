package com.serotonin.goid.task.maze;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptContext;
import javax.script.SimpleScriptContext;

import com.serotonin.goid.util.AgentInfoRenderer;
import com.serotonin.goid.util.BasicEnvironment;
import com.serotonin.goid.util.ControlScript;
import com.serotonin.goid.util.RenderUtils;
import com.serotonin.goid.util.Renderable;
import com.serotonin.goid.util.TurnListener;
import com.serotonin.goid.util2d.Circle;
import com.serotonin.goid.util2d.GeomUtils;
import com.serotonin.goid.util2d.PolarPoint2D;
import com.serotonin.goid.util2d.ShapeDelegator;

/**
 * This is the interface between the behavioural calculator and the environment. Environmental information about the
 * agent (its location, orientation, health, etc) is kept and calculated here.
 */
public class RatBody extends ShapeDelegator<Circle> implements TurnListener, Renderable {
    private static final Color SENSOR_COLOR = new Color(0, 0x60, 0xc0);
    private static final Color BODY_COLOR = new Color(0xa0, 0, 0);
    private static final Color HEAD_COLOR = new Color(0, 0xff, 0);

    public static final double RADIUS = 3;

    private final ScriptContext scriptContext = new SimpleScriptContext();
    private final RatSenses senses = new RatSenses();
    private final RatMotors actuators = new RatMotors();

    private final Circle rightEyeSearchBounds = new Circle(0, 0, 3);
    private final Circle leftEyeSearchBounds = new Circle(0, 0, 3);

    private final BasicEnvironment environment;
    private final ControlScript ratScript;
    private final Circle movementBounds = new Circle(0, 0, RADIUS);

    private double orientation = 0;
    private boolean blocked;
    private boolean leftEyeTriggered, rightEyeTriggered;

    private boolean moved;
    private boolean displayAgentStates;

    public CompletionMonitor goalLocations;

    public RatBody(BasicEnvironment environment, ControlScript ratScript, Circle bounds) {
        super(bounds);
        this.environment = environment;
        this.ratScript = ratScript;
        setOrientation(0);
        resetContext();
    }

    public void setOrientation(double orientation) {
        this.orientation = GeomUtils.normalizeAngle(orientation);
        rightEyeSearchBounds.setCenter(getRightEyeLocation());
        leftEyeSearchBounds.setCenter(getLeftEyeLocation());
        updateSenses();
    }

    void updateSenses() {
        // Adjust eye positions
        rightEyeSearchBounds.setCenter(getRightEyeLocation());
        leftEyeSearchBounds.setCenter(getLeftEyeLocation());

        // Gather sensor information.
        List<Shape> leftEyeSees = new ArrayList<Shape>();
        environment.getIndex().findIntersecting(leftEyeSearchBounds, leftEyeSees);
        leftEyeSees.remove(this);
        leftEyeTriggered = leftEyeSees.size() > 0;

        List<Shape> rightEyeSees = new ArrayList<Shape>();
        environment.getIndex().findIntersecting(rightEyeSearchBounds, rightEyeSees);
        rightEyeSees.remove(this);
        rightEyeTriggered = rightEyeSees.size() > 0;

        senses.leftEye = leftEyeTriggered;
        senses.rightEye = rightEyeTriggered;

        senses.blocked = blocked;

        senses.probe = -1;
        List<Shape> probeSees;
        do {
            senses.probe += 1;
            probeSees = new ArrayList<Shape>();
            Point2D probe = new PolarPoint2D(senses.probe, orientation).toCartesian();
            probe.setLocation(probe.getX() + shape.getCenterX(), probe.getY() + shape.getCenterY());
            environment.getIndex().findIntersecting(new Circle(probe.getX(), probe.getY(), .3), probeSees);
            probeSees.remove(this);
        }
        while (probeSees.isEmpty() && senses.probe < 40);

        if (goalLocations != null) {
            Point2D goalPos = goalLocations.getPositionOfActiveGoal();
            double distance = goalPos.distance(shape.getCenterX(), shape.getCenterY());
            senses.goalDistance = distance;
        }
    }

    //
    // Turn Listener
    //
    public void next(long turn) {
        movementBounds.setLocation(shape);
        moved = false;

        // Run the brain
        ratScript.execute(scriptContext);

        // Execute motor commands
        if (actuators.forwardMovement != 0)
            moveForward(actuators.forwardMovement);
        if (actuators.backwardMovement != 0)
            moveBackward(actuators.backwardMovement);
        if (actuators.turn != 0)
            turn(actuators.turn);

        // Reset the location
        blocked = false;
        if (moved) {
            ArrayList<Shape> collisionResults = new ArrayList<Shape>();
            environment.getIndex().findIntersecting(movementBounds, collisionResults);
            collisionResults.remove(this);
            if (collisionResults.isEmpty()) {
                shape.setLocation(movementBounds);
                environment.getIndex().put(this);
            }
            else
                blocked = true;
        }

        orientation = GeomUtils.normalizeAngle(orientation);

        updateSenses(); // I put this back here to make sure the sense bubbles were attached to the robot
    }

    /**
     * Gets the location of the head in environment space coordinates.
     */
    private Point2D getHeadLocation() {
        Point2D head = getHeadVector();
        head.setLocation(head.getX() + shape.getCenterX(), head.getY() + shape.getCenterY());
        return head;
    }

    /**
     * Gets the location of the head relative to the center of the body.
     */
    private Point2D getHeadVector() {
        return new PolarPoint2D(RADIUS, orientation).toCartesian();
    }

    private Point2D getLeftEyeLocation() {
        Point2D eye = new PolarPoint2D(RADIUS, orientation - Math.PI / 4).toCartesian();
        eye.setLocation(eye.getX() + shape.getCenterX(), eye.getY() + shape.getCenterY());
        return eye;
    }

    private Point2D getRightEyeLocation() {
        Point2D eye = new PolarPoint2D(RADIUS, orientation + Math.PI / 4).toCartesian();
        eye.setLocation(eye.getX() + shape.getCenterX(), eye.getY() + shape.getCenterY());
        return eye;
    }

    public void setDisplayAgentStates(boolean display) {
        displayAgentStates = display;
    }

    public void render(Graphics2D g) {
        if (RenderUtils.isWithinClipBounds(g, shape)) {
            double x = shape.getCenterX();
            double y = shape.getCenterY();

            Stroke oldStroke = g.getStroke();
            g.setStroke(new BasicStroke(0.25f));
            g.setColor(SENSOR_COLOR);
            g.draw(rightEyeSearchBounds);
            g.draw(leftEyeSearchBounds);

            Point2D probe = new PolarPoint2D(senses.probe, orientation).toCartesian();
            probe.setLocation(probe.getX() + shape.getCenterX(), probe.getY() + shape.getCenterY());
            g.draw(new Circle(probe.getX(), probe.getY(), .5));

            g.setColor(BODY_COLOR);
            g.fill(this);
            g.setColor(HEAD_COLOR);
            Point2D head = getHeadLocation();
            g.draw(new Line2D.Double(x, y, head.getX(), head.getY()));
            g.setStroke(oldStroke);

            if (displayAgentStates)
                AgentInfoRenderer.render(g, senses, (int) (x + RADIUS), (int) (y + RADIUS));
        }
    }

    //
    // / Motor commands
    //
    private void moveForward(double level) {
        level = GeomUtils.constrain(level, 0, 1);
        Point2D headVector = getHeadVector();
        GeomUtils.adjustLength(headVector, level);
        movementBounds.translate(headVector);
        moved = true;
    }

    private void moveBackward(double level) {
        level = GeomUtils.constrain(level, 0, 0.5);
        Point2D headVector = getHeadVector();
        GeomUtils.adjustLength(headVector, -level);
        movementBounds.translate(headVector);
        moved = true;
    }

    /**
     * Positive values turn to the right.
     * 
     * @param level
     */
    private void turn(double level) {
        level = GeomUtils.constrain(level, 0.5);
        orientation += level;
    }

    public void resetContext() {
        scriptContext.getBindings(ScriptContext.ENGINE_SCOPE).clear();
        scriptContext.setAttribute(ControlScript.KEY_SENSES, senses, ScriptContext.ENGINE_SCOPE);
        scriptContext.setAttribute(ControlScript.KEY_ACTUATORS, actuators, ScriptContext.ENGINE_SCOPE);
    }
}
