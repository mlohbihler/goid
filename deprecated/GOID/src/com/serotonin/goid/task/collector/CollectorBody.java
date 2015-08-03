package com.serotonin.goid.task.collector;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptContext;
import javax.script.SimpleScriptContext;

import com.serotonin.goid.util.AgentInfoRenderer;
import com.serotonin.goid.util.ControlScript;
import com.serotonin.goid.util.RenderUtils;
import com.serotonin.goid.util.Renderable;
import com.serotonin.goid.util.TurnListener;
import com.serotonin.goid.util2d.Circle;
import com.serotonin.goid.util2d.GeomUtils;
import com.serotonin.goid.util2d.PolarPoint2D;

public class CollectorBody implements TurnListener, Renderable {
    private static final Color BODY_COLOR = new Color(0xa0, 0, 0);
    private static final Color HEAD_COLOR = new Color(0, 0xff, 0);
    private static final Color VISUAL_PERCEPTION_COLOR = new Color(0xa0, 0xa0, 0, 0x80);

    public static final double RADIUS = 4;

    private static final Path2D VISUAL_PERCEPTION_TEMPLATE;
    static {
        Path2D path = new Path2D.Double();
        path.moveTo(RADIUS, RADIUS);
        path.lineTo(RADIUS * 10, RADIUS * 10);
        path.lineTo(RADIUS * 10, -RADIUS * 10);
        path.lineTo(RADIUS, -RADIUS);
        path.closePath();
        VISUAL_PERCEPTION_TEMPLATE = path;
    }

    private final CollectorEnvironment environment;
    private final ControlScript script;
    private final Circle shape = new Circle(250, 350, RADIUS);
    private double orientation = 0; // Math.PI * 3 / 2;
    private boolean targetCollected;
    private double lastTurnAmount;
    private double lastMoveAmount;
    private final ScriptContext scriptContext = new SimpleScriptContext();
    private final CollectorSenses senses = new CollectorSenses();
    private final CollectorActuators actuators = new CollectorActuators();
    private int targetsCollected;
    private boolean displayAgentStates;

    // Runtime values reused for efficiency.
    private final List<Area> sensorResults = new ArrayList<Area>();

    public CollectorBody(CollectorEnvironment environment, ControlScript script) {
        this.environment = environment;
        this.script = script;
        resetContext();
    }

    //
    // Turn Listener
    //
    public void next(long turn) {
        // Gather sensor information.
        senses.orientation = GeomUtils.round(orientation, 2);
        senses.obstacles = toArray(getObstacles());
        senses.targets = toArray(getTargets());
        senses.targetCollected = targetCollected;
        senses.lastMoveAmount = GeomUtils.round(lastMoveAmount, 2);
        senses.lastTurnAmount = GeomUtils.round(lastTurnAmount, 2);

        // Run the script
        script.execute(scriptContext);

        // Execute actuators
        if (actuators.move != 0)
            move(actuators.move);
        if (actuators.turn != 0)
            turn(actuators.turn);
    }

    //
    // Renderer
    //
    public void render(Graphics2D g) {
        if (RenderUtils.isWithinClipBounds(g, shape)) {
            Stroke oldStroke = g.getStroke();
            g.setStroke(new BasicStroke(0.25f));
            g.setColor(BODY_COLOR);
            g.fill(getShape());

            g.setColor(HEAD_COLOR);
            Point2D nose = new PolarPoint2D(RADIUS, orientation).toCartesian();
            double x = shape.getCenterX();
            double y = shape.getCenterY();
            Line2D agentNose = new Line2D.Double(x, y, x + nose.getX(), y + nose.getY());
            g.draw(agentNose);

            g.setColor(VISUAL_PERCEPTION_COLOR);
            g.fill(getVisualPerceptionShape());

            g.setStroke(oldStroke);

            if (displayAgentStates)
                AgentInfoRenderer.render(g, senses, (int) (x + RADIUS), (int) (y + RADIUS));
        }
    }

    public void setDisplayAgentStates(boolean display) {
        displayAgentStates = display;
    }

    public Shape getShape() {
        return shape;
    }

    public double getX() {
        return shape.getCenterX();
    }

    public double getY() {
        return shape.getCenterY();
    }

    public void setLocation(double x, double y) {
        shape.setCenter(x, y);
    }

    public void setTargetCollected(boolean targetCollected) {
        this.targetCollected = targetCollected;
        if (targetCollected)
            targetsCollected++;
    }

    public boolean isTargetCollected() {
        return targetCollected;
    }

    public int getTargetsCollected() {
        return targetsCollected;
    }

    public double getOrientation() {
        return orientation;
    }

    public void turn(double amount) {
        lastTurnAmount = GeomUtils.constrain(amount, 0.3);
        orientation += lastTurnAmount;
        orientation = GeomUtils.normalizeAngle(orientation);
    }

    public Shape getVisualPerceptionShape() {
        AffineTransform transform = new AffineTransform();
        transform.translate(shape.getCenterX(), shape.getCenterY());
        transform.rotate(orientation);
        return transform.createTransformedShape(VISUAL_PERCEPTION_TEMPLATE);
    }

    private List<PolarPoint2D> getObstacles() {
        List<PolarPoint2D> results = new ArrayList<PolarPoint2D>();

        sensorResults.clear();
        environment.getIndex().findIntersections(getVisualPerceptionShape(), sensorResults);

        if (!sensorResults.isEmpty()) {
            List<Area> continugousResults = new ArrayList<Area>();
            for (Area area : sensorResults)
                GeomUtils.splitNoncontiguousAreas(area, continugousResults);

            Point2D here = new Point2D.Double(shape.getCenterX(), shape.getCenterY());
            for (Area area : continugousResults) {
                Point2D there = GeomUtils.nearestPoint(here, area);
                if (there != null) {
                    there.setLocation(there.getX() - here.getX(), there.getY() - here.getY());
                    PolarPoint2D polar = new PolarPoint2D(there);
                    polar.setAngle(polar.getAngle() - orientation);
                    results.add(polar);
                }
            }
        }

        return results;
    }

    private List<PolarPoint2D> getTargets() {
        List<PolarPoint2D> results = new ArrayList<PolarPoint2D>();

        for (Shape target : environment.findTargets(getVisualPerceptionShape())) {
            Rectangle2D r = target.getBounds2D();
            PolarPoint2D polar = new PolarPoint2D(new Point2D.Double(r.getCenterX() - shape.getCenterX(),
                    r.getCenterY() - shape.getCenterY()));
            polar.setAngle(polar.getAngle() - orientation);
            results.add(polar);
        }

        return results;
    }

    private PolarPoint2D[] toArray(List<PolarPoint2D> list) {
        PolarPoint2D[] arr = new PolarPoint2D[list.size()];
        list.toArray(arr);
        return arr;
    }

    public void move(double amount) {
        // Save the current location.
        if (amount != 0) {
            double ox = shape.getCenterX();
            double oy = shape.getCenterY();

            // Do the move.
            lastMoveAmount = GeomUtils.constrain(amount, -0.5, 1);
            Point2D displacement = new PolarPoint2D(lastMoveAmount, orientation).toCartesian();
            shape.translate(displacement);

            // Check for clipping.
            sensorResults.clear();
            environment.getIndex().findIntersections(getShape(), sensorResults);
            if (!sensorResults.isEmpty())
                shape.setCenter(ox, oy);
        }
    }

    public void resetContext() {
        scriptContext.getBindings(ScriptContext.ENGINE_SCOPE).clear();
        scriptContext.setAttribute(ControlScript.KEY_SENSES, senses, ScriptContext.ENGINE_SCOPE);
        scriptContext.setAttribute(ControlScript.KEY_ACTUATORS, actuators, ScriptContext.ENGINE_SCOPE);
    }
}
