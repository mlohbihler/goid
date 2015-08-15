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

/**
 * An agent represents the brain or controller of the agent body.
 * 
 * @author Matthew Lohbihler
 */
public interface Agent {
    /**
     * The agent uses the input to determine the state of the environment as well as can be sensed. Based upon this
     * information, the agent sets values in the actuators which will subsequently be acted upon by the agent body.
     * This method is called each environmental tick.
     * 
     * @param input
     * @param actuators
     */
    void update(SensoryInput input, Actuators actuators);
}
