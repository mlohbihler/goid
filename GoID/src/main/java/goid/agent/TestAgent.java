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
package goid.agent;

import goid.simulation.agent.Actuators;
import goid.simulation.agent.Agent;
import goid.simulation.agent.SensoryInput;

/**
 * @author Matthew Lohbihler
 */
public class TestAgent implements Agent {
    @Override
    public void update(SensoryInput input, Actuators actuators) {
        actuators.forward = 1;
        actuators.turn = 0.05;
    }
}
