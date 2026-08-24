package co.unicauca.polimorfismo.figuras;

/**
 * Triangulo isosceles definido por base y altura, tal como lo describe el
 * enunciado del taller (constructor con dos parametros: base y altura).
 * <p>
 * Con solo esos dos datos no es posible obtener el perimetro de un
 * triangulo arbitrario, por lo que se asume que la altura es perpendicular
 * al punto medio de la base (triangulo isosceles). Los dos lados iguales
 * se obtienen aplicando el teorema de Pitagoras:
 * lado = sqrt((base/2)^2 + altura^2).
 */
public class Triangle extends Figure {

    private final double base;
    private final double height;

    public Triangle(double base, double height) {
        super(0.0, 0.0);
        if (base <= 0 || height <= 0) {
            throw new IllegalArgumentException("La base y la altura deben ser mayores que cero");
        }
        this.base = base;
        this.height = height;
    }

    public double getBase() {
        return base;
    }

    public double getHeight() {
        return height;
    }

    private double sideLength() {
        return Math.sqrt(Math.pow(base / 2.0, 2) + Math.pow(height, 2));
    }

    @Override
    public double calculateArea() {
        return (base * height) / 2.0;
    }

    @Override
    public double calculatePerimeter() {
        return base + 2 * sideLength();
    }
}
