package fr.univrouen.view;

import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.TriangleMesh;
import javafx.util.Duration;
import org.fxyz3d.importers.Importer3D;
import org.fxyz3d.importers.Model3D;

import java.util.ArrayList;
import java.util.List;

/**
 * Représentation visuelle 3D d'un pawn (pion) JavaFX.
 */
public class Pawn3D extends Group {
    private int logicX;
    private int logicZ;
    private String id;

    private final Group pawnMesh = new Group();
    private final double radius;
    private boolean usingDefaultMesh = true;

    private final PhongMaterial baseMat;
    private final PhongMaterial bodyMat;
    private final PhongMaterial headMat;
    private final Cylinder highlight;

    public Pawn3D(Color color, double radius) {
        this.radius = radius;
        baseMat = new PhongMaterial(color.deriveColor(0, 1, 0.85, 1));
        bodyMat = new PhongMaterial(color);
        headMat = new PhongMaterial(color.deriveColor(0, 1, 1.15, 1));

        highlight = new Cylinder(radius * 0.85, radius * 0.08);
        highlight.setMaterial(new PhongMaterial(Color.rgb(246, 229, 141, 0.6)));
        highlight.setVisible(false);
        highlight.setMouseTransparent(true);

        buildDefaultMesh();
        this.getChildren().addAll(pawnMesh, highlight);
        setPickOnBounds(true);
    }

    public boolean applyModel(String modelPath, double scale, double yOffset) {
        if (modelPath == null || modelPath.isBlank()) {
            buildDefaultMesh();
            return true;
        }

        java.net.URL resource = getClass().getResource(modelPath);
        if (resource == null) {
            System.out.println("Pawn skin not found: " + modelPath);
            buildDefaultMesh();
            return false;
        }

        try {
            Model3D model = Importer3D.load(resource);
            List<Node> nodes = new ArrayList<>();
            double minX = Double.POSITIVE_INFINITY;
            double minY = Double.POSITIVE_INFINITY;
            double minZ = Double.POSITIVE_INFINITY;
            double maxX = Double.NEGATIVE_INFINITY;
            double maxY = Double.NEGATIVE_INFINITY;
            double maxZ = Double.NEGATIVE_INFINITY;

            for (Node node : model.getMeshViews()) {
                if (node instanceof MeshView) {
                    ((MeshView) node).setCullFace(CullFace.NONE);
                }
                Bounds b = node.getBoundsInLocal();
                minX = Math.min(minX, b.getMinX());
                minY = Math.min(minY, b.getMinY());
                minZ = Math.min(minZ, b.getMinZ());
                maxX = Math.max(maxX, b.getMaxX());
                maxY = Math.max(maxY, b.getMaxY());
                maxZ = Math.max(maxZ, b.getMaxZ());
                nodes.add(node);
            }

            if (nodes.isEmpty()) {
                buildDefaultMesh();
                return false;
            }

            double width = maxX - minX;
            double height = maxY - minY;
            double depth = maxZ - minZ;
            double maxDim = Math.max(width, Math.max(height, depth));
            double targetSize = radius * 2.0;
            double finalScale = scale > 0 ? scale : (maxDim > 0 ? targetSize / maxDim : 1.0);

            pawnMesh.getChildren().setAll(nodes);
            pawnMesh.setScaleX(finalScale);
            pawnMesh.setScaleY(finalScale);
            pawnMesh.setScaleZ(finalScale);
            pawnMesh.setTranslateX(0);
            pawnMesh.setTranslateZ(0);
            pawnMesh.setTranslateY(yOffset - minY * finalScale);

            highlight.setTranslateY(yOffset + 1);
            usingDefaultMesh = false;
            return true;
        } catch (Exception ex) {
            System.out.println("Failed to load pawn skin: " + modelPath + " -> " + ex.getMessage());
            buildDefaultMesh();
            return false;
        }
    }

    public void updatePosition(double x, double y, double z, int lx, int lz) {
        this.setTranslateX(x);
        this.setTranslateY(y);
        this.setTranslateZ(z);
        this.logicX = lx;
        this.logicZ = lz;
    }

    public void animateTo(double x, double y, double z, int lx, int lz) {
        this.logicX = lx;
        this.logicZ = lz;

        TranslateTransition transition = new TranslateTransition(Duration.millis(220), this);
        transition.setInterpolator(Interpolator.EASE_BOTH);
        transition.setToX(x);
        transition.setToY(y);
        transition.setToZ(z);
        transition.play();
    }

    public int getLogicX() { return logicX; }
    public int getLogicZ() { return logicZ; }
    public void setPawnId(String id) { this.id = id; }
    public String getPawnId() { return id; }
    public void setSelected(boolean sel) {
        if (sel) {
            highlight.setVisible(true);
            applySpecular(Color.web("#f6e58d"), 64);
        } else {
            highlight.setVisible(false);
            applySpecular(Color.web("#808080"), 8);
        }
    }

    private void applySpecular(Color color, double power) {
        if (!usingDefaultMesh) return;
        baseMat.setSpecularColor(color);
        baseMat.setSpecularPower(power);
        bodyMat.setSpecularColor(color);
        bodyMat.setSpecularPower(power);
        headMat.setSpecularColor(color);
        headMat.setSpecularPower(power);
    }

    private void buildDefaultMesh() {
        usingDefaultMesh = true;
        pawnMesh.getChildren().clear();
        pawnMesh.setScaleX(1);
        pawnMesh.setScaleY(1);
        pawnMesh.setScaleZ(1);
        pawnMesh.setTranslateX(0);
        pawnMesh.setTranslateY(0);
        pawnMesh.setTranslateZ(0);

        double baseHeight = radius * 0.22;
        double bodyHeight = radius * 1.2;
        double bodyBase = radius * 1.05;
        double headRadius = radius * 0.4;

        Cylinder base = new Cylinder(radius * 0.7, baseHeight);
        base.setMaterial(baseMat);
        base.setTranslateY(baseHeight / 2.0);

        MeshView body = createPyramid(bodyBase, bodyHeight);
        body.setMaterial(bodyMat);
        body.setTranslateY(baseHeight);

        Sphere head = new Sphere(headRadius);
        head.setMaterial(headMat);
        head.setTranslateY(baseHeight + bodyHeight + headRadius);

        highlight.setTranslateY(baseHeight + 1);
        pawnMesh.getChildren().addAll(base, body, head);
    }

    private MeshView createPyramid(double baseSize, double height) {
        float half = (float) (baseSize / 2.0);
        float h = (float) height;

        TriangleMesh mesh = new TriangleMesh();
        mesh.getPoints().addAll(
            -half, 0, -half,
             half, 0, -half,
             half, 0,  half,
            -half, 0,  half,
             0,    h,  0
        );
        mesh.getTexCoords().addAll(0, 0);
        mesh.getFaces().addAll(
            0, 0, 1, 0, 4, 0,
            1, 0, 2, 0, 4, 0,
            2, 0, 3, 0, 4, 0,
            3, 0, 0, 0, 4, 0,
            0, 0, 2, 0, 1, 0,
            0, 0, 3, 0, 2, 0
        );

        MeshView view = new MeshView(mesh);
        view.setCullFace(CullFace.BACK);
        return view;
    }
}