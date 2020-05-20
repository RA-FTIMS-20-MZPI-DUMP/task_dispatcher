package com.dispatcher;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PointTest {

    Point point1 = new Point(5, 7);
    Point point2 = new Point(3, 2);

    @Test
    void getDistance() {
        double distance = point1.getDistance(point2);
        assertEquals(5.385, distance, 0.001);
    }

    @Test
    void getX() {
        assertEquals(5, point1.getX());
    }

    @Test
    void getY() {
        assertEquals(7, point1.getY());
    }

}