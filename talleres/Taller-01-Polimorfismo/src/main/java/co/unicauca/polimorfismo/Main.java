package co.unicauca.polimorfismo;

import co.unicauca.polimorfismo.figuras.Circle;
import co.unicauca.polimorfismo.figuras.Figure;
import co.unicauca.polimorfismo.figuras.Square;
import co.unicauca.polimorfismo.figuras.Triangle;

import java.util.ArrayList;
import java.util.List;

/**
 * Aplicacion cliente que demuestra el polimorfismo de la jerarquia Figure:
 * un mismo mensaje (calculateArea/calculatePerimeter) enviado a objetos de
 * tipos distintos (Circle, Square, Triangle) produce comportamientos
 * diferentes, sin que el cliente conozca la clase concreta de cada figura.
 */
public class Main {

    public static void main(String[] args) {
        Figure fig1 = new Circle(1.0f);      // radio
        Figure fig2 = new Square(2.3);       // lado
        Figure fig3 = new Triangle(4.2, 4.5); // base y altura

        List<Figure> figures = new ArrayList<>();
        figures.add(fig1);
        figures.add(fig2);
        figures.add(fig3);

        for (Figure fig : figures) {
            System.out.println(fig.getClass().getSimpleName() + ":");
            System.out.println("  Area: " + fig.calculateArea());
            System.out.println("  Perimeter: " + fig.calculatePerimeter());
        }
    }
}
