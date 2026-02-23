package fr.univrouen.view;

import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

/**
 * Représentation visuelle 3D d'un mur JavaFX.
 */
public class Wall3D extends Box {
    public Wall3D(double w, double h, double d, Color color) {
        super(w, h, d);
        setMaterial(new PhongMaterial(color));
    }
}