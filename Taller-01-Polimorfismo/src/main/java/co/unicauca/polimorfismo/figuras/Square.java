package co.unicauca.polimorfismo.figuras;

/** Cuadrado definido por la longitud de su lado. */
public class Square extends Figure {

    private final double side;

    public Square(double side) {
        super(0.0, 0.0);
        if (side <= 0) {
            throw new IllegalArgumentException("El lado debe ser mayor que cero");
        }
        this.side = side;
    }

    public double getSide() {
        return side;
    }

    @Override
    public double calculateArea() {
        return side * side;
    }

    @Override
    public double calculatePerimeter() {
        return 4 * side;
    }
}
