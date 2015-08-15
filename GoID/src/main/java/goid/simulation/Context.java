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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

/**
 * @author Matthew Lohbihler
 */
public class Context {
    // Each time tick is 10ms 
    private static final int TICK_LENGTH = 10;

    public final Random random = new Random();
    public final int minX = -10000;
    public final int minY = -10000;
    public final int maxX = 10000;
    public final int maxY = 10000;

    private long elapsedTime;
    private final List<EnvironmentElement> elements = new ArrayList<>();
    private final List<EnvironmentElement> elementsToAdd = new ArrayList<>();
    private final List<EnvironmentElement> elementsCopy = new ArrayList<>();

    public long getElapsedTime() {
        return elapsedTime;
    }

    public void tick() {
        this.elapsedTime += TICK_LENGTH;
    }

    public List<EnvironmentElement> getElements() {
        return elements;
    }

    public void iterateElementsCopy(Consumer<EnvironmentElement> c) {
        synchronized (elementsCopy) {
            for (EnvironmentElement e : elementsCopy)
                c.accept(e);
        }
    }

    public void addElement(EnvironmentElement e) {
        elementsToAdd.add(e);
    }

    public void updateElements(List<EnvironmentElement> removes) {
        if (!removes.isEmpty()) {
            elements.removeAll(removes);
            removes.clear();
        }

        if (!elementsToAdd.isEmpty()) {
            elements.addAll(elementsToAdd);
            elementsToAdd.clear();
        }

        synchronized (elementsCopy) {
            elementsCopy.clear();
            elementsCopy.addAll(elements);
        }
    }
}
