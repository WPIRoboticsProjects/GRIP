package edu.wpi.grip.preloader;

import java.util.Collection;
import java.util.stream.Collectors;

import javafx.scene.Group;
import javafx.scene.shape.Polygon;

public class HexagonGrid extends Group {

  private static final double ang30 = Math.toRadians(30);

  /**
   * Creates a new hexagon grid with the given number of rows and columns.
   *
   * @param cols    the number of columns in the grid
   * @param rows    the number of rows in the grid
   * @param radius  the radius of the hexagons in the grid
   * @param padding the padding between each hexagon
   */
  public HexagonGrid(int cols, int rows, double radius, double padding) {
    double xOffset = Math.cos(ang30) * (radius + padding);
    double yOffset = Math.sin(ang30) * (radius + padding) * 3;
    for (int row = 0; row < rows; row++) {
      for (int col = 0; col < cols; col++) {
        double x = xOffset * (col * 2 + row % 2);
        double y = radius + yOffset * row;
        Polygon hexagon = new Hexagon(radius);
        hexagon.setRotate(90);
        hexagon.setTranslateX(x);
        hexagon.setTranslateY(y);
        getChildren().add(hexagon);
      }
    }
  }

  /**
   * Gets the hexagons in the grid. Do not apply any transforms to the hexagons; they are already in
   * the correct locations and orientations.
   */
  public Collection<Polygon> hexagons() {
    return getChildren().stream()
        .filter(n -> n instanceof Hexagon)
        .map(n -> (Hexagon) n)
        .collect(Collectors.toList());
  }

  private static class Hexagon extends Polygon {

    private static final double ROOT_THREE_OVER_2 = Math.sqrt(3) / 2;

    public Hexagon(double radius) {
      super(radius, 0,
          radius / 2, ROOT_THREE_OVER_2 * radius,
          -radius / 2, ROOT_THREE_OVER_2 * radius,
          -radius, 0,
          -radius / 2, -ROOT_THREE_OVER_2 * radius,
          radius / 2, -ROOT_THREE_OVER_2 * radius);
    }

  }

}
