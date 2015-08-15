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
package goid.simulation.util;

import goid.simulation.Context;

/**
 * @author Matthew Lohbihler
 */
public class Util {
    public static int mod(int i, int divisor) {
        return ((i % divisor) + divisor) % divisor;
    }

    /**
     * If f is less than min, return min. If f is greater than max, return max. Otherwise return f.
     * 
     * @param f
     *            the value to be clamped
     * @param min
     *            the minimum value
     * @param max
     *            the maximum value
     * @return
     */
    public static double clamp(double f, double min, double max) {
        if (f < min)
            return min;
        if (f > max)
            return max;
        return f;
    }

    public static int randomInt(Context ctx, int min, int max) {
        return ctx.random.nextInt(max - min) + min;
    }

    public static int randomGuassian(Context ctx, int centroid, int sigma) {
        return (int) (ctx.random.nextGaussian() * sigma + centroid + 0.5);
    }
}
