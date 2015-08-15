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

import goid.agent.TestAgent;
import goid.simulation.agent.AgentBody;
import goid.simulation.element.FoodController;

import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for updating the environment elements according to the frame rate. This is separated from the Loop to
 * facilitate testing.
 * 
 * @author Matthew Lohbihler
 */
public class Environment {
    // The shared context.
    private final Context ctx = new Context();

    // A list of elements that should be removed. Checked each turn.
    private final List<EnvironmentElement> removes = new ArrayList<>();

    public Environment() {
        // Add the agent to the environment
        ctx.getElements().add(new AgentBody(new TestAgent(), 0, 200, 200));
        ctx.getElements().add(new FoodController(ctx));

        //        ctx.getElements().add(new Throbber(0.2, 10, 10));
        //        ctx.getElements().add(new Throbber(-0.25, 100, 100));

        ctx.updateElements(removes);
    }

    public Context getCtx() {
        return ctx;
    }

    public void update() {
        // Update the time
        ctx.tick();

        //
        // Update all elements
        for (EnvironmentElement e : ctx.getElements()) {
            e.update(ctx);
            if (e.canRemove())
                removes.add(e);
        }

        //
        // Look for elements to remove from the loop.
        ctx.updateElements(removes);
    }
}
