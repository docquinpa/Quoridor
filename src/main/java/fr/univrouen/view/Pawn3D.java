package fr.univrouen.view;

import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;

/**
 * Représentation visuelle 3D d'un pawn (pion) JavaFX.
 */
public class Pawn3D extends Sphere {
    private int logicX;
    private int logicZ;
    private String id;

    public Pawn3D(Color color, double radius) {
        super(radius);
        setMaterial(new PhongMaterial(color));
    }

    public void updatePosition(double x, double y, double z, int lx, int lz) {
        this.setTranslateX(x);
        this.setTranslateY(y);
        this.setTranslateZ(z);
        this.logicX = lx;
        this.logicZ = lz;
    }

    public int getLogicX() { return logicX; }
    public int getLogicZ() { return logicZ; }
    public void setPawnId(String id) { this.id = id; }
    public String getPawnId() { return id; }
    public void setSelected(boolean sel) {
        PhongMaterial m = (PhongMaterial) getMaterial();
        if (m == null) m = new PhongMaterial();
        if (sel) {
            m.setSpecularColor(Color.YELLOW);
            m.setSpecularPower(64);
        } else {
            m.setSpecularColor(Color.GRAY);
            m.setSpecularPower(8);
        }
        setMaterial(m);
    }
}