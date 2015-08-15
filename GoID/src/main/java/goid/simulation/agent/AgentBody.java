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
package goid.simulation.agent;

import static goid.simulation.util.Util.clamp;
import goid.simulation.Context;
import goid.simulation.EnvironmentElement;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

/**
 * @author Matthew Lohbihler
 */
public class AgentBody implements EnvironmentElement {
    private final Agent agent;
    private double a;
    private double x;
    private double y;

    private final SensoryInput input = new SensoryInput();
    private final Actuators actuators = new Actuators();

    private final Ellipse2D shape = new Ellipse2D.Double(-10, -10, 20, 20);
    private final Line2D line = new Line2D.Double(0, 0, 10, 0);

    public AgentBody(Agent agent, double a, double x, double y) {
        this.agent = agent;
        this.a = a;
        this.x = x;
        this.y = y;
    }

    @Override
    public void update(Context ctx) {
        agent.update(input, actuators);

        a += clamp(actuators.turn, -0.1, 0.1);

        double forward = clamp(actuators.forward, 0, 3);
        x += StrictMath.cos(a) * forward;
        y += StrictMath.sin(a) * forward;
    }

    @Override
    public boolean canRemove() {
        return false;
    }

    @Override
    public void renderIsolated(Graphics2D g) {
        g.translate(x, y);
        g.rotate(a);
        g.setColor(Color.RED);
        g.fill(shape);
        g.setColor(Color.BLACK);
        g.draw(shape);
        g.draw(line);
    }
}
