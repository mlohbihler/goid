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

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.AffineTransform;

/**
 * @author Matthew Lohbihler
 */
public interface EnvironmentElement {
    void update(Context ctx);

    boolean canRemove();

    final AffineTransform tx = new AffineTransform();

    default void render(Graphics2D g) {
        // Save the transform
        AffineTransform origTx = g.getTransform();
        tx.setTransform(origTx);
        g.setTransform(tx);

        // Save the paint
        Paint paint = g.getPaint();

        renderIsolated(g);

        // Reset the paint
        g.setPaint(paint);

        // Reset the transform
        g.setTransform(origTx);
    }

    void renderIsolated(Graphics2D g);
}
