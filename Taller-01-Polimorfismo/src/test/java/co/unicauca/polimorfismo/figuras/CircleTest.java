package co.unicauca.polimorfismo.figuras;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CircleTest {

    private static final double DELTA = 1e-6;

    @Test
    void calculateAreaShouldUseCircleFormula() {
        Circle circle = new Circle(1.0f);
        assertEquals(Math.PI, circle.calculateArea(), DELTA);
    }

    @Test
    void calculatePerimeterShouldUseCircleFormula() {
        Circle circle = new Circle(2.0f);
        assertEquals(2 * Math.PI * 2.0, circle.calculatePerimeter(), DELTA);
    }

    @Test
    void constructorShouldRejectNonPositiveRadius() {
        assertThrows(IllegalArgumentException.class, () -> new Circle(0f));
        assertThrows(IllegalArgumentException.class, () -> new Circle(-3f));
    }

    @Test
    void circleIsPolymorphicallyAFigure() {
        Figure figure = new Circle(1.0f);
        assertTrue(figure instanceof Figure);
        assertEquals(Math.PI, figure.calculateArea(), DELTA);
    }
}
