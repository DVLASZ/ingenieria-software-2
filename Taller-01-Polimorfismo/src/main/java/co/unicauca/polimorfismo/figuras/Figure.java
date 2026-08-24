package co.unicauca.polimorfismo.figuras;

/**
 * Clase base abstracta de la jerarquia de figuras geometricas.
 * Define el contrato comun (calculateArea/calculatePerimeter) que toda
 * figura concreta debe implementar, habilitando el polimorfismo cuando
 * el codigo cliente manipula referencias de tipo Figure.
 */
public abstract class Figure {

    private final double x1;
    private final double y1;

    protected Figure(double x1, double y1) {
        this.x1 = x1;
        this.y1 = y1;
    }

    public double getX1() {
        return x1;
    }

    public double getY1() {
        return y1;
    }

    public abstract double calculateArea();

    public abstract double calculatePerimeter();

    @Override
    public String toString() {
        return String.format("%s[area=%.2f, perimeter=%.2f]",
                getClass().getSimpleName(), calculateArea(), calculatePerimeter());
    }
}
