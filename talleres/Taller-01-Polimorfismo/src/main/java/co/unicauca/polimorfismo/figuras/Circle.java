package co.unicauca.polimorfismo.figuras;

/**
 * Circulo definido por su radio. Se ubica en el origen (0,0) por defecto,
 * ya que el taller solo requiere comparar area y perimetro entre figuras.
 */
public class Circle extends Figure {

    private final float radius;

    public Circle(float radius) {
        super(0.0, 0.0);
        if (radius <= 0) {
            throw new IllegalArgumentException("El radio debe ser mayor que cero");
        }
        this.radius = radius;
    }

    public float getRadius() {
        return radius;
    }

    @Override
    public double calculateArea() {
        return Math.PI * radius * radius;
    }

    @Override
    public double calculatePerimeter() {
        return 2 * Math.PI * radius;
    }
}
