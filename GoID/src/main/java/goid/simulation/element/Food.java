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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

/**
 * Instances are originally created by the controller. Spawning of new instances occurs after the spawn delay. After
 * each spawning, the delay gets a little bit longer. New instances inherit the spawning delay of their parents. This
 * causes new growths of food to spread quickly initially, but to then spread more slowly with time.
 * 
 * @author Matthew Lohbihler
 */
public class Food implements EnvironmentElement {
    private static final Ellipse2D shape = new Ellipse2D.Double(-2, -2, 4, 4);
    private static final Color fill = new Color(80, 255, 80, 100);
    private static final Color outline = new Color(20, 160, 20, 200);

    // The sigma for determining the location of a spawned element.
    private static final int SPAWN_SIGMA = 20;

    private final int x;
    private final int y;
    private boolean eaten;

    private int spawnDelay;
    private int nextSpawn;

    public Food(int x, int y) {
        this(x, y, 100 * 10); // Initial spawn delay of 10s
    }

    public Food(int x, int y, int spawnDelay) {
        this.x = x;
        this.y = y;
        this.spawnDelay = spawnDelay;
        nextSpawn = spawnDelay;
    }

    public boolean isEaten() {
        return eaten;
    }

    public void setEaten(boolean eaten) {
        this.eaten = eaten;
    }

    @Override
    public void update(Context ctx) {
        nextSpawn--;
        if (nextSpawn <= 0) {
            spawnDelay += 100 * 5; // Add 5s to the delay.
            nextSpawn = spawnDelay;

            int x = Util.randomGuassian(ctx, this.x, SPAWN_SIGMA);
            int y = Util.randomGuassian(ctx, this.y, SPAWN_SIGMA);

            // Check for an existing element at this location
            boolean found = false;
            for (EnvironmentElement e : ctx.getElements()) {
                if (e instanceof Food) {
                    Food f = (Food) e;
                    if (f.x == x && f.y == y) {
                        found = true;
                        break;
                    }
                }
            }

            if (!found)
                ctx.addElement(new Food(x, y, spawnDelay + 100 * 10 + 13));
            else
                System.out.println("Food creation failed");
        }
    }

    @Override
    public boolean canRemove() {
        return eaten;
    }

    @Override
    public void renderIsolated(Graphics2D g) {
        //        System.out.println("food paint");
        g.translate(x, y);
        g.setColor(fill);
        g.fill(shape);
        g.setColor(outline);
        g.draw(shape);
    }
}
