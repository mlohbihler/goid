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
package goid.simulation.element;

import goid.simulation.Context;
import goid.simulation.EnvironmentElement;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * A test of an element that changes in the rendering thread instead of the environment thread.
 * 
 * @author Matthew Lohbihler
 */
public class Throbber implements EnvironmentElement {
    private final double r;
    private final int x;
    private final int y;

    private double a;

    public Throbber(double r, int x, int y) {
        this.r = r;
        this.x = x;
        this.y = y;
    }

    @Override
    public void update(Context ctx) {
        // no op
    }

    @Override
    public boolean canRemove() {
        return false;
    }

    @Override
    public void renderIsolated(Graphics2D g) {
        a += r;
        g.rotate(a, x, y);
        g.setColor(Color.BLUE);
        g.drawLine(x - 8, y, x + 8, y);
    }
}
