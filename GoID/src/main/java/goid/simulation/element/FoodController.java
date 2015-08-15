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
import goid.simulation.util.Util;

import java.awt.Graphics2D;

/**
 * @author Matthew Lohbihler
 */
public class FoodController implements EnvironmentElement {
    // Create a new instance every half hour
    private static final int NEW_INSTANCE_DELAY = 100 * 60 * 30 + 1;

    private int nextNewInstance = NEW_INSTANCE_DELAY;

    public FoodController(Context ctx) {
        // Initially create a bunch of instances.
        //        for (int i = 0; i < 10; i++)
        //            createInstance(ctx);
        createInstance(ctx);
    }

    @Override
    public void update(Context ctx) {
        nextNewInstance--;
        if (nextNewInstance <= 0) {
            createInstance(ctx);
            nextNewInstance = NEW_INSTANCE_DELAY;
        }
    }

    private void createInstance(Context ctx) {
        //        int x = Util.randomInt(ctx, ctx.minX, ctx.maxX);
        //        int y = Util.randomInt(ctx, ctx.minY, ctx.maxY);
        int x = Util.randomInt(ctx, -200, 200);
        int y = Util.randomInt(ctx, -200, 200);
        ctx.addElement(new Food(x, y));
        System.out.println("Food created at " + x + "," + y);
    }

    @Override
    public boolean canRemove() {
        return false;
    }

    @Override
    public void renderIsolated(Graphics2D g) {
        // no op
    }

    @Override
    public void render(Graphics2D g) {
        // no op
    }
}
