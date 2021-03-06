package org.htwkvisu.scoring;

import javafx.geometry.Point2D;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;

public class ExponentialFallOfTest {

    private int radius = 10;
    private ExponentialFallOf fallOf;
    private static double TINY_DELTA = 0.0001;

    @Before
    public void setUp() throws Exception {
        fallOf = new ExponentialFallOf(radius, 5, 2);
    }

    @Test
    public void getValue() throws Exception {
        Random rnd = new Random();

        for (int i = 0; i < radius; i++) {
            double val = rnd.nextDouble();

            double xVal = rnd.nextDouble();
            double yVal = rnd.nextDouble();
            double distance = rnd.nextDouble();

            Point2D pt = new Point2D(xVal, yVal);
            Point2D sample = new Point2D(xVal, yVal + distance);

            fallOf = new ExponentialFallOf(pt.distance(sample), val, rnd.nextDouble());
            assertEquals(val, fallOf.getValue(pt, pt), TINY_DELTA);
            assertEquals(0, fallOf.getValue(pt, sample), TINY_DELTA);
        }
    }

    @Test
    public void getMaximumValue() throws Exception {
        Random rnd = new Random();

        for (int i = 0; i < radius; i++) {
            double val = rnd.nextDouble();

            fallOf = new ExponentialFallOf(radius, val, 2);
            assertEquals(val, fallOf.getMaximumValue(), TINY_DELTA);
        }
    }

    @Test
    public void getRadius() throws Exception {
        fallOf = new ExponentialFallOf(radius, 1, 1);
        assertEquals(radius, fallOf.getRadius(), TINY_DELTA);
    }
}