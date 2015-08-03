package com.serotonin.goid.task.donut;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
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
 * 
 * @author Matthew Lohbihler
 */
public class BugBody extends ShapeDelegator<Circle> implements TurnListener, Renderable {
    private static final Color SENSOR_COLOR = new Color(0, 0x60, 0xc0);
    private static final Color BODY_COLOR = new Color(0xa0, 0, 0);
    private static final Color HEAD_COLOR = new Color(0, 0xff, 0);

    public static final double RADIUS = 2;

    private final List<Shape> sensorResult = new ArrayList<Shape>();
    private final Circle movementBounds = new Circle(0, 0, RADIUS);

    private final ScriptContext scriptContext = new SimpleScriptContext();
    private final BugSenses senses = new BugSenses();
    private final BugMotors actuators = new BugMotors();

    private final BasicEnvironment environment;
    private final ControlScript bugScript;
    private final Circle sensorSearchBounds = new Circle(0, 0, 3);

    private double orientation = 0;
    private boolean blocked;

    private boolean moved;
    private boolean displayAgentStates;

    public BugBody(BasicEnvironment environment, ControlScript bugScript, Circle bounds) {
        super(bounds);
        this.environment = environment;
        this.bugScript = bugScript;
        setOrientation(0);
        resetContext();
    }

    public void setOrientation(double orientation) {
        this.orientation = GeomUtils.normalizeAngle(orientation);
        sensorSearchBounds.setCenter(getHeadLocation());
        updateSenses();
    }

    void updateSenses() {
        senses.orientation = orientation;
        senses.blocked = blocked;
        senses.obstacles = getObstacles();
    }

    //
    // Turn Listener
    //
    public void next(long turn) {
        // Gather sensor information.
        sensorSearchBounds.setCenter(getHeadLocation());
        sensorResult.clear();
        environment.getIndex().findIntersecting(sensorSearchBounds, sensorResult);
        sensorResult.remove(this);

        movementBounds.setLocation(shape);
        moved = false;

        updateSenses();

        // Run the brain
        bugScript.execute(scriptContext);

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
            sensorResult.clear();
            environment.getIndex().findIntersecting(movementBounds, sensorResult);
            sensorResult.remove(this);
            if (sensorResult.isEmpty()) {
                shape.setLocation(movementBounds);
                environment.getIndex().put(this);
            }
            else
                blocked = true;
        }

        orientation = GeomUtils.normalizeAngle(orientation);
    }

    public List<Shape> getProximity() {
        return sensorResult;
    }

    private double[] getObstacles() {
        if (sensorResult.isEmpty())
            return null;

        double[] result = new double[sensorResult.size()];
        for (int i = 0; i < result.length; i++) {
            Shape s = sensorResult.get(i);
            Area a = new Area(sensorSearchBounds);
            a.intersect(new Area(s));
            Rectangle2D r = a.getBounds2D();
            PolarPoint2D polar = new PolarPoint2D(new Point2D.Double(r.getCenterX() - shape.getCenterX(),
                    r.getCenterY() - shape.getCenterY()));
            double deviance = polar.getAngle() - orientation;
            deviance = GeomUtils.normalizeAngle(deviance + Math.PI) - Math.PI;
            result[i] = deviance;
        }

        return result;
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
            g.draw(sensorSearchBounds);
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
        level = GeomUtils.constrain(level, 0, 0.1);
        Point2D headVector = getHeadVector();
        GeomUtils.adjustLength(headVector, level);
        movementBounds.translate(headVector);
        moved = true;
    }

    private void moveBackward(double level) {
        level = GeomUtils.constrain(level, 0, 0.02);
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
        level = GeomUtils.constrain(level, 0.05);
        orientation += level;
    }

    public void resetContext() {
        scriptContext.getBindings(ScriptContext.ENGINE_SCOPE).clear();
        scriptContext.setAttribute(ControlScript.KEY_SENSES, senses, ScriptContext.ENGINE_SCOPE);
        scriptContext.setAttribute(ControlScript.KEY_ACTUATORS, actuators, ScriptContext.ENGINE_SCOPE);
    }
}
