package co.unicauca.polimorfismo.figuras;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SquareTest {

    private static final double DELTA = 1e-9;

    @Test
    void calculateAreaShouldUseSideSquared() {
        Square square = new Square(2.3);
        assertEquals(2.3 * 2.3, square.calculateArea(), DELTA);
    }

    @Test
    void calculatePerimeterShouldUseFourTimesSide() {
        Square square = new Square(2.3);
        assertEquals(4 * 2.3, square.calculatePerimeter(), DELTA);
    }

    @Test
    void constructorShouldRejectNonPositiveSide() {
        assertThrows(IllegalArgumentException.class, () -> new Square(0));
        assertThrows(IllegalArgumentException.class, () -> new Square(-1));
    }
}
