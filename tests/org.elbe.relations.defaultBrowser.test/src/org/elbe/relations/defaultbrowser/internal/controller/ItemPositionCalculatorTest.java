/***************************************************************************
 * This package is part of Relations application.
 * Copyright (C) 2004-2013, Benno Luthiger
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 ***************************************************************************/
package org.elbe.relations.defaultbrowser.internal.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.junit.jupiter.api.Test;

/**
 * @author lbenno
 */
public class ItemPositionCalculatorTest {
    private final static int HEIGHT = 30;
    private final static int WIDTH = 120;
    private final static int RADIUS = 200;

    @Test
    public void testDo() {
        ItemPositionCalculator lCalculator = new ItemPositionCalculator(WIDTH,
                HEIGHT, RADIUS, 2);
        assertEquals(2, lCalculator.getCount());
        assertEquals(1.57079, lCalculator.getOffset(), 0.001);
        assertEquals(0.33221, lCalculator.getDelta(), 0.001);
        assertFalse(lCalculator.hasMore());

        lCalculator = new ItemPositionCalculator(WIDTH, HEIGHT, RADIUS, 5);
        assertEquals(5, lCalculator.getCount());
        assertEquals(1.3216357, lCalculator.getOffset(), 0.001);
        assertEquals(0.3322140, lCalculator.getDelta(), 0.001);
        assertFalse(lCalculator.hasMore());

        lCalculator = new ItemPositionCalculator(WIDTH, HEIGHT, RADIUS, 25);
        assertEquals(16, lCalculator.getCount());
        assertEquals(0.40804704, lCalculator.getOffset(), 0.001);
        assertEquals(0.33221408, lCalculator.getDelta(), 0.001);
        assertTrue(lCalculator.hasMore());

        final PrecisionPoint[] lExpected = new PrecisionPoint[] {
                new PrecisionPoint(79, -183), new PrecisionPoint(134, -147),
                new PrecisionPoint(175, -95), new PrecisionPoint(197, -33),
                new PrecisionPoint(197, 33), new PrecisionPoint(175, 95),
                new PrecisionPoint(134, 147), new PrecisionPoint(79, 183),
                new PrecisionPoint(-79, 183), new PrecisionPoint(-134, 147),
                new PrecisionPoint(-175, 95), new PrecisionPoint(-197, 33),
                new PrecisionPoint(-197, -33), new PrecisionPoint(-175, -95),
                new PrecisionPoint(-134, -147), new PrecisionPoint(-79, -183) };
        final List<PrecisionPoint> lPoints = lCalculator.getPositions();
        assertEquals(lExpected.length, lPoints.size());
        for (int i = 0; i < lExpected.length; i++) {
            final PrecisionPoint lPoint = lPoints.get(i);
            assertEquals(lExpected[i].preciseX(), lPoint.preciseX(), 1);
            assertEquals(lExpected[i].preciseY(), lPoint.preciseY(), 1);
        }
    }

    @Test
    public void testCenterTranslation() {
        final int[][] lAreas = new int[][] { { 655, 540 }, { 656, 541 }, { 800, 600 }, { 121, 31 }, { 0, 0 } };
        final int[][] lItems = new int[][] { { WIDTH, HEIGHT }, { 121, 31 } };
        for (final int[] lItem : lItems) {
            for (final int[] lArea : lAreas) {
                final PrecisionPoint lTranslate = ItemPositionCalculator.getCenterTranslation(lItem[0], lItem[1],
                        lArea[0], lArea[1]);
                assertEquals(lArea[0] / 2.0, lTranslate.x + lItem[0] / 2.0, 1);
                assertEquals(lArea[1] / 2.0, lTranslate.y + lItem[1] / 2.0, 1);
            }
        }

        final PrecisionPoint lTranslate = ItemPositionCalculator.getCenterTranslation(WIDTH, HEIGHT, 800, 600);
        assertEquals(340, lTranslate.x);
        assertEquals(285, lTranslate.y);
    }

    @Test
    public void testSymmetry() {
        for (final int lRemaining : new int[] { 2, 4, 6, 25 }) {
            final ItemPositionCalculator lCalculator = new ItemPositionCalculator(WIDTH, HEIGHT, RADIUS, lRemaining);
            double lSumX = 0;
            double lSumY = 0;
            for (final PrecisionPoint lPoint : lCalculator.getPositions()) {
                lSumX += lPoint.preciseX();
                lSumY += lPoint.preciseY();
            }
            assertEquals(0, lSumX, 0.001, "x not symmetric for " + lRemaining);
            assertEquals(0, lSumY, 0.001, "y not symmetric for " + lRemaining);
        }
    }

}
