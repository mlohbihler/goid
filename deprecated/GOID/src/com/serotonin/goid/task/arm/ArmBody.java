package com.serotonin.goid.task.arm;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.script.ScriptContext;
import javax.script.SimpleScriptContext;

import com.serotonin.goid.util.AgentInfoRenderer;
import com.serotonin.goid.util.ControlScript;
import com.serotonin.goid.util.Renderable;
import com.serotonin.goid.util.TurnListener;
import com.serotonin.goid.util2d.GeomUtils;

public class ArmBody implements TurnListener, Renderable {
    private static final Color ARM_COLOR = new Color(0x20, 0x20, 0x20);
    public static final double ARM_LENGTH = 150;
    private static final double ARM_WIDTH = 7;
    private static final double JOINT_RADIUS = 6;

    public static final double MIN_SHOULDER_ANGLE = -Math.PI / 2;
    public static final double MAX_SHOULDER_ANGLE = Math.PI * 3 / 4;
    public static final double MIN_ELBOW_ANGLE = -Math.PI;
    public static final double MAX_ELBOW_ANGLE = 0;

    private static final double L1 = 1;
    private static final double L2 = 1;
    private static final double M1 = 1;
    private static final double M2 = 1;
    private static final double G = 0.00098;

    private final Ellipse2D jointTemplate;
    private final Path2D armTemplate;

    private Shape shoulder;
    private Shape upperArm;
    private Shape elbow;
    private Shape foreArm;
    private Shape wrist;

    private double shoulderAngle = Math.PI / 2;
    private double elbowAngle = 0;
    private double shoulderMomentum = 0;
    private double elbowMomentum = 0;
    private boolean targetCollected;
    private int targetsCollected;
    private double energyUsed = 0;

    private final ArmEnvironment environment;
    private final ControlScript script;
    private final ScriptContext scriptContext = new SimpleScriptContext();
    private final ArmSenses senses = new ArmSenses();
    private final ArmActuators actuators = new ArmActuators();

    private boolean displayAgentStates;

    public ArmBody(ArmEnvironment environment, ControlScript script) {
        this.environment = environment;
        this.script = script;

        jointTemplate = new Ellipse2D.Double(0, 0, JOINT_RADIUS * 2, JOINT_RADIUS * 2);

        armTemplate = new Path2D.Double();
        armTemplate.moveTo(0, 0);
        armTemplate.lineTo(ARM_LENGTH, 0);
        armTemplate.lineTo(ARM_LENGTH, ARM_WIDTH);
        armTemplate.lineTo(0, ARM_WIDTH);
        armTemplate.closePath();

        calculateArmShape();

        senses.targetLocation = new Point2D.Double();
        senses.wristToTarget = new Point2D.Double();
        senses.elbowToTarget = new Point2D.Double();
        updateSenses();
        resetContext();
    }

    private void calculateArmShape() {
        AffineTransform transform = new AffineTransform();

        // Draw the shoulder
        transform.translate(-JOINT_RADIUS, -JOINT_RADIUS);
        shoulder = transform.createTransformedShape(jointTemplate);

        // Draw the upper arm
        transform.translate(JOINT_RADIUS, JOINT_RADIUS - (ARM_WIDTH / 2));
        transform.rotate(shoulderAngle, 0, ARM_WIDTH / 2);
        upperArm = transform.createTransformedShape(armTemplate);

        // Draw the elbow
        transform.translate(ARM_LENGTH - JOINT_RADIUS, (ARM_WIDTH / 2) - JOINT_RADIUS);
        elbow = transform.createTransformedShape(jointTemplate);

        // Draw the forearm
        transform.translate(JOINT_RADIUS, JOINT_RADIUS - (ARM_WIDTH / 2));
        transform.rotate(elbowAngle, 0, ARM_WIDTH / 2);
        foreArm = transform.createTransformedShape(armTemplate);

        // Draw the wrist
        transform.translate(ARM_LENGTH - JOINT_RADIUS, (ARM_WIDTH / 2) - JOINT_RADIUS);
        wrist = transform.createTransformedShape(jointTemplate);
    }

    private void updateSenses() {
        senses.targetLocation.setLocation(environment.getTargetLocation());
        senses.wristLocation = getWristLocation();
        calcDiff(senses.targetLocation, senses.wristLocation, senses.wristToTarget);
        calcDiff(senses.targetLocation, getElbowLocation(), senses.elbowToTarget);
        senses.shoulderAngle = shoulderAngle;
        senses.elbowAngle = elbowAngle;
        senses.shoulderMomentum = shoulderMomentum;
        senses.elbowMomentum = elbowMomentum;
        senses.realForearmAngle = shoulderAngle + elbowAngle;
        senses.targetCollected = targetCollected;
        senses.energyUsed = energyUsed;
    }

    //
    // Turn Listener
    //
    public void next(long turn) {
        Point2D target = new Point2D.Double();
        target.setLocation(environment.getTargetLocation());

        // Gather sensor information.
        updateSenses();

        // Run the script
        script.execute(scriptContext);

        shoulderMomentum += actuators.shoulderTorque;
        elbowMomentum += actuators.elbowTorque;
        energyUsed += Math.abs(actuators.shoulderTorque) + Math.abs(actuators.elbowTorque);

        // Execute actuators
        double theta1 = shoulderAngle - Math.PI / 2;
        double theta2 = shoulderAngle + elbowAngle - Math.PI / 2;
        double p1 = shoulderMomentum;
        double p2 = elbowMomentum;
        double thetaDiff = theta1 - theta2;
        double mSumSin = M1 + M2 * Math.sin(thetaDiff) * Math.sin(thetaDiff);

        double da1 = L2 * p1 - L1 * p2 * Math.cos(thetaDiff);
        da1 /= L1 * L1 * L2 * mSumSin;

        double da2 = L1 * (M1 + M2) * p2 - L2 * M2 * p1 * Math.cos(thetaDiff);
        da2 /= L1 * L2 * L2 * M2 * mSumSin;

        double c1 = (p1 * p2 * Math.sin(thetaDiff)) / (L1 * L2 * mSumSin);

        double c2 = L2 * L2 * M2 * p1 * p1 + L1 * L1 * (M1 + M2) * p2 * p2;
        c2 -= L1 * L2 * M2 * p1 * p2 * Math.cos(thetaDiff);
        c2 *= Math.sin(2 * thetaDiff);
        c2 /= 2 * L1 * L1 * L2 * L2 * mSumSin * mSumSin;

        double dm1 = -(M1 + M2) * G * L1 * Math.sin(theta1) - c1 + c2;
        double dm2 = -M2 * G * L2 * Math.sin(theta2) + c1 - c2;

        // Adjust
        shoulderMomentum += dm1;
        elbowMomentum += dm2;

        // Adjust for friction
        shoulderMomentum = GeomUtils.drag(shoulderMomentum, 0.0001);
        elbowMomentum = GeomUtils.drag(elbowMomentum, 0.0001);

        shoulderAngle += da1 / 20;
        if (shoulderAngle < MIN_SHOULDER_ANGLE) {
            shoulderAngle = MIN_SHOULDER_ANGLE;
            shoulderMomentum *= 0.8;
        }
        else if (shoulderAngle > MAX_SHOULDER_ANGLE) {
            shoulderAngle = MAX_SHOULDER_ANGLE;
            shoulderMomentum *= 0.8;
        }

        elbowAngle += (da2 - da1) / 20;
        if (elbowAngle < MIN_ELBOW_ANGLE) {
            elbowAngle = MIN_ELBOW_ANGLE;
            elbowMomentum *= 0.8;
        }
        else if (elbowAngle > MAX_ELBOW_ANGLE) {
            elbowAngle = MAX_ELBOW_ANGLE;
            elbowMomentum *= 0.8;
        }

        calculateArmShape();
    }

    //
    // Renderer
    //
    public void render(Graphics2D g) {
        g.setColor(ARM_COLOR);
        g.fill(shoulder);
        g.fill(upperArm);
        g.fill(elbow);
        g.fill(foreArm);
        g.fill(wrist);

        if (displayAgentStates)
            AgentInfoRenderer.render(g, senses, 20, 20);
    }

    public void setDisplayAgentStates(boolean display) {
        displayAgentStates = display;
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

    public double getEnergyUsed() {
        return energyUsed;
    }

    private void calcDiff(Point2D p1, Point2D p2, Point2D set) {
        set.setLocation(p1.getX() - p2.getX(), p1.getY() - p2.getY());
    }

    private Point2D getElbowLocation() {
        Rectangle2D bounds = elbow.getBounds2D();
        return new Point2D.Double(bounds.getCenterX(), bounds.getCenterY());
    }

    private Point2D getWristLocation() {
        Rectangle2D bounds = wrist.getBounds2D();
        return new Point2D.Double(bounds.getCenterX(), bounds.getCenterY());
    }

    public Rectangle2D getWristBounds() {
        return wrist.getBounds2D();
    }

    public void resetContext() {
        scriptContext.getBindings(ScriptContext.ENGINE_SCOPE).clear();
        scriptContext.setAttribute(ControlScript.KEY_SENSES, senses, ScriptContext.ENGINE_SCOPE);
        scriptContext.setAttribute(ControlScript.KEY_ACTUATORS, actuators, ScriptContext.ENGINE_SCOPE);
    }
}
