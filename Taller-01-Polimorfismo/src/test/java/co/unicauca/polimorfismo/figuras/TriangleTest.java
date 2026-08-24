package co.unicauca.polimorfismo.figuras;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TriangleTest {

    private static final double DELTA = 1e-9;

    @Test
    void calculateAreaShouldBeHalfBaseTimesHeight() {
        Triangle triangle = new Triangle(4.2, 4.5);
        assertEquals((4.2 * 4.5) / 2.0, triangle.calculateArea(), DELTA);
    }

    @Test
    void calculatePerimeterShouldAddTwoEqualSidesToBase() {
        Triangle triangle = new Triangle(6.0, 4.0);
        // lado = sqrt((6/2)^2 + 4^2) = sqrt(9 + 16) = 5 (triangulo 3-4-5 escalado)
        double expectedSide = 5.0;
        double expectedPerimeter = 6.0 + 2 * expectedSide;
        assertEquals(expectedPerimeter, triangle.calculatePerimeter(), DELTA);
    }

    @Test
    void constructorShouldRejectNonPositiveDimensions() {
        assertThrows(IllegalArgumentException.class, () -> new Triangle(0, 4.5));
        assertThrows(IllegalArgumentException.class, () -> new Triangle(4.2, 0));
        assertThrows(IllegalArgumentException.class, () -> new Triangle(-1, -1));
    }
}
