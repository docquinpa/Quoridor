package fr.univrouen.view;

import fr.univrouen.model.Board;
import fr.univrouen.model.Orientation;
import fr.univrouen.model.StdBoard;
import fr.univrouen.controller.GameController;
import fr.univrouen.controller.GameController.GameListener;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.util.Duration;
import org.fxyz3d.importers.Importer3D;
import org.fxyz3d.importers.Model3D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implémentation standard de la vue 3D du plateau Quoridor.
 * Manipule uniquement des interfaces (Board, Pawn, GameController) pour respecter SOLID.
 */
public class StdBoard3D extends Group implements Board3D {
    private final double cellSize;
    private final double yPos;
    private final double centerOffset;
    private final Group boardModelGroup = new Group();
    private final Group interactiveGrid = new Group();
    private final Board boardModel;
    private final Map<String, Pawn3D> pawnViews = new HashMap<>();
    private String selectedPawnId = null;
    private final GameController controller;
    private Box[][] gridBoxes;
    private PhongMaterial hoverMat;
    private PhongMaterial idleMat;
    private PhongMaterial validMat;
    private Wall3D wallPreview;
    private Orientation previewOrientation;
    private double pawnYOffset;
    private boolean actionTakenThisTurn = false;
    private String boardModelPath;

    /**
     * Crée une vue 3D du plateau.
     * @param controller le contrôleur de jeu (dépendance injectée)
     * @param modelPath le chemin vers le modèle 3D
     * @param cellSize la taille d'une cellule en pixels
     * @param yPos la hauteur Y du plateau
     * @throws Exception si le chargement du modèle échoue
     */
    public StdBoard3D(GameController controller, String modelPath, double cellSize, double yPos) throws Exception {
        this.cellSize = cellSize;
        this.yPos = yPos;
        this.centerOffset = (9 * cellSize) / 2.0 - (cellSize / 2.0);
        this.pawnYOffset = cellSize * 0.1;
        this.controller = controller;
        this.boardModel = controller.getModel();

        this.boardModelPath = modelPath;
        loadBoardModel(modelPath);
        this.getChildren().add(boardModelGroup);

        createGrid();
        this.getChildren().add(interactiveGrid);

        // Subtle chess-like lighting: warm key and soft rim light
        javafx.scene.PointLight key = new javafx.scene.PointLight(Color.rgb(255, 244, 214));
        key.setTranslateY(yPos + 260);
        key.setTranslateZ(-560);
        key.setTranslateX(-180);

        javafx.scene.PointLight rim = new javafx.scene.PointLight(Color.rgb(200, 210, 230));
        rim.setTranslateY(yPos + 140);
        rim.setTranslateZ(420);
        rim.setTranslateX(220);

        this.getChildren().add(key);
        this.getChildren().add(rim);
        this.getChildren().add(new javafx.scene.AmbientLight(Color.rgb(90, 90, 90)));

        // spawn two pawns (opponents) at opposite sides: p1 (top) and p2 (bottom)
        // Utilise la Factory pour créer les pawns (respecte SOLID)
        fr.univrouen.model.Pawn pm1 = fr.univrouen.QuoridorFactory.createPawn("p1", 4, 0, (x,z) -> z==8);
        fr.univrouen.model.Pawn pm2 = fr.univrouen.QuoridorFactory.createPawn("p2", 4, 8, (x,z) -> z==0);
        controller.registerPlayer(pm1);
        controller.registerPlayer(pm2);

        spawnPawnForModel(pm1, Color.GOLD);
        spawnPawnForModel(pm2, Color.SILVER);

        // listen to game events to update views if model changes elsewhere
        controller.addListener(new GameListener() {
            @Override public void onTurnChanged(String currentPlayerId) { /* UI listens externally */ }
            @Override public void onPawnMoved(String pawnId, int x, int z) {
                Pawn3D p = pawnViews.get(pawnId);
                if (p != null) p.animateTo(logicToPx(x), pawnY(), logicToPz(z), x, z);
            }
            @Override public void onWallPlaced(int x, int z, Orientation orientation) {
                spawnWallView(x, z, orientation);
            }
            @Override public void onPlayerRegistered(String playerId) { /* no-op */ }
            @Override public void onGameWon(String winnerPlayerId) {
                System.out.println("=== GAME WON ===");
                System.out.println("Winner: " + winnerPlayerId);
            }
        });

        // HUD is handled by the JavaFX scene (QuoridorApp). StdBoard3D exposes state getters.
    }

    private void createGrid() {
        hoverMat = new PhongMaterial(Color.rgb(255, 0, 0, 0.4));
        idleMat = new PhongMaterial(Color.TRANSPARENT);
        validMat = new PhongMaterial(Color.rgb(0, 255, 0, 0.35));

        int size = boardModel.getSize();
        gridBoxes = new Box[size][size];

        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                Box cell = new Box(cellSize * 0.8, 2, cellSize * 0.8);
                double px = x * cellSize - centerOffset;
                double pz = z * cellSize - centerOffset;

                cell.setTranslateX(px);
                cell.setTranslateZ(pz);
                cell.setTranslateY(yPos);
                cell.setMaterial(idleMat);

                final int fx = x; final int fz = z;
                cell.setOnMouseEntered(e -> {
                    cell.setMaterial(hoverMat);
                    handleWallPreview(fx, fz, e);
                });
                cell.setOnMouseMoved(e -> handleWallPreview(fx, fz, e));
                cell.setOnMouseExited(e -> {
                    if (!isValidHighlighted(fx,fz)) cell.setMaterial(idleMat);
                    if (!e.isShiftDown() && !e.isControlDown()) hideWallPreview();
                });
                cell.setOnMouseClicked(e -> onCellClick(fx, fz, px, pz, e));

                interactiveGrid.getChildren().add(cell);
                gridBoxes[x][z] = cell;
            }
        }
    }

    private void loadBoardModel(String modelPath) throws Exception {
        List<Node> nodes = loadBoardMeshNodes(modelPath);
        boardModelGroup.getChildren().setAll(nodes);
    }

    private List<Node> loadBoardMeshNodes(String modelPath) throws Exception {
        Model3D model = Importer3D.load(getClass().getResource(modelPath));
        List<Node> nodes = new ArrayList<>();
        for (Node node : model.getMeshViews()) {
            if (node instanceof MeshView) {
                ((MeshView) node).setCullFace(CullFace.NONE);
            }
            nodes.add(node);
        }
        return nodes;
    }

    public boolean applyBoardSkin(String modelPath) {
        if (modelPath == null || modelPath.isBlank()) return false;
        if (modelPath.equals(boardModelPath)) return true;
        if (getClass().getResource(modelPath) == null) {
            System.out.println("Board skin not found: " + modelPath);
            return false;
        }
        try {
            List<Node> nodes = loadBoardMeshNodes(modelPath);
            boardModelGroup.getChildren().setAll(nodes);
            boardModelPath = modelPath;
            return true;
        } catch (Exception ex) {
            System.out.println("Failed to load board skin: " + modelPath + " -> " + ex.getMessage());
            return false;
        }
    }

    public boolean applyPawnSkin(String pawnId, String modelPath, double scale, double yOffset) {
        if (pawnId == null || pawnId.isBlank()) return false;
        Pawn3D pawn = pawnViews.get(pawnId);
        if (pawn == null) return false;
        return pawn.applyModel(modelPath, scale, yOffset);
    }

    private boolean isValidHighlighted(int x,int z) {
        return gridBoxes != null && gridBoxes[x][z].getMaterial() == validMat;
    }

    private void onCellClick(int lx, int lz, double px, double pz, javafx.scene.input.MouseEvent evt) {
        System.out.println("Logique: " + lx + "," + lz + " shift:" + evt.isShiftDown() + " ctrl:" + evt.isControlDown());

        // Wall placement: Shift = HORIZONTAL, Ctrl = VERTICAL (only current player can place)
        if (evt.isShiftDown() || evt.isControlDown()) {
            String cur = controller.getCurrentPlayerId();
            if (cur == null) return;
            boolean ok = false;
            if (evt.isShiftDown()) ok = controller.placeWall(cur, lx, lz, Orientation.HORIZONTAL);
            if (evt.isControlDown()) ok = controller.placeWall(cur, lx, lz, Orientation.VERTICAL);
            if (!ok) System.out.println("Wall placement refused (overlap, crossing, isolation, or no walls left)");
            hideWallPreview();
            return;
        }

        // If clicking on a pawn -> select it (only if it's current player's pawn)
        String clickedPawnId = findPawnIdAt(lx, lz);
        if (clickedPawnId != null) {
            // only allow selecting current player
            String cur = controller.getCurrentPlayerId();
            if (cur == null || cur.equals(clickedPawnId)) {
                if (selectedPawnId != null) {
                    Pawn3D prev = pawnViews.get(selectedPawnId);
                    if (prev != null) prev.setSelected(false);
                    clearValidHighlights();
                }
                selectedPawnId = clickedPawnId;
                Pawn3D sel = pawnViews.get(selectedPawnId);
                if (sel != null) sel.setSelected(true);
                // highlight valid moves
                highlightValidMoves(selectedPawnId);
                System.out.println("Selected pawn: " + selectedPawnId);
            }
            return;
        }

        // If a pawn is selected, try to move it
        if (selectedPawnId != null) {
            if (controller.movePawn(selectedPawnId, lx, lz)) {
                Pawn3D view = pawnViews.get(selectedPawnId);
                if (view != null) view.setSelected(false);
                selectedPawnId = null;
                clearValidHighlights();
            } else {
                System.out.println("Move invalid for " + selectedPawnId);
            }
        }

    }

    private void handlePawnClick(String pawnId, MouseEvent evt) {
        // mirror selection logic from cell click but for direct pawn clicks
        if (pawnId == null) return;
        String cur = controller.getCurrentPlayerId();
        if (cur == null || cur.equals(pawnId)) {
            if (selectedPawnId != null) {
                Pawn3D prev = pawnViews.get(selectedPawnId);
                if (prev != null) prev.setSelected(false);
                clearValidHighlights();
            }
            selectedPawnId = pawnId;
            Pawn3D sel = pawnViews.get(selectedPawnId);
            if (sel != null) sel.setSelected(true);
            highlightValidMoves(selectedPawnId);
            System.out.println("Selected pawn (direct): " + selectedPawnId);
        }
    }

    @Override
    public Board getBoardModel() { 
        return this.boardModel; 
    }

    @Override
    public String getCurrentPlayerId() { 
        return controller.getCurrentPlayerId(); 
    }

    @Override
    public java.util.List<String> getPlayerOrder() { 
        return controller.getPlayerOrder(); 
    }

    private void highlightValidMoves(String pawnId) {
        if (pawnId == null || gridBoxes == null) return;
        int size = boardModel.getSize();
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                if (boardModel.canMove(pawnId, x, z)) {
                    gridBoxes[x][z].setMaterial(validMat);
                } else {
                    gridBoxes[x][z].setMaterial(idleMat);
                }
            }
        }
    }

    private void clearValidHighlights() {
        if (gridBoxes == null) return;
        int size = boardModel.getSize();
        for (int x = 0; x < size; x++) for (int z = 0; z < size; z++) gridBoxes[x][z].setMaterial(idleMat);
    }

    private String otherPlayer(String id) {
        if (id == null) return null;
        java.util.List<String> po = controller.getPlayerOrder();
        int idx = po.indexOf(id);
        if (idx < 0) return null;
        int next = (idx + 1) % po.size();
        return po.get(next);
    }

    @Override
    public void spawnPawn(double px, double pz, Color color) {
        Pawn3D p = new Pawn3D(color, 12);
        p.updatePosition(px, pawnY(), pz, 0, 0);
        this.getChildren().add(p);
    }

    private void spawnPawnForModel(fr.univrouen.model.Pawn pm, Color color) {
        double px = logicToPx(pm.getX());
        double pz = logicToPz(pm.getZ());
        Pawn3D p = new Pawn3D(color, 12);
        p.setPawnId(pm.getId());
        p.updatePosition(px, pawnY(), pz, pm.getX(), pm.getZ());
        // allow clicking the pawn directly
        final String pid = pm.getId();
        p.setOnMouseClicked((MouseEvent e) -> {
            e.consume();
            handlePawnClick(pid, e);
        });
        pawnViews.put(pm.getId(), p);
        this.getChildren().add(p);
        // Player registration is handled via GameController.registerPlayer when initializing the board.
    }

    private void spawnWallView(int x, int z, Orientation orientation) {
        double centerX = logicToPx(x) + cellSize/2.0;
        double centerZ = logicToPz(z) + cellSize/2.0;
        double wallHeight = 10;
        double wallSpan = cellSize * 2.0;
        double wallThickness = 6;
        Wall3D w;
        if (orientation == Orientation.HORIZONTAL) {
            w = new Wall3D(wallSpan, wallHeight, wallThickness, Color.DARKRED);
        } else {
            w = new Wall3D(wallThickness, wallHeight, wallSpan, Color.DARKRED);
        }
        w.setTranslateX(centerX);
        w.setTranslateZ(centerZ);
        w.setTranslateY(yPos - 4);
        this.getChildren().add(w);

        w.setScaleX(0.2);
        w.setScaleY(0.2);
        w.setScaleZ(0.2);
        ScaleTransition scale = new ScaleTransition(Duration.millis(180), w);
        scale.setInterpolator(Interpolator.EASE_OUT);
        scale.setToX(1);
        scale.setToY(1);
        scale.setToZ(1);
        scale.play();
    }

    private double logicToPx(int lx) {
        return lx * cellSize - centerOffset;
    }

    private double logicToPz(int lz) {
        return lz * cellSize - centerOffset;
    }

    private double pawnY() {
        return yPos + pawnYOffset;
    }

    private String findPawnIdAt(int lx, int lz) {
        for (Map.Entry<String, Pawn3D> e : pawnViews.entrySet()) {
            Pawn3D p = e.getValue();
            if (p.getLogicX() == lx && p.getLogicZ() == lz) return e.getKey();
        }
        return null;
    }

    private void handleWallPreview(int lx, int lz, MouseEvent evt) {
        if (!evt.isShiftDown() && !evt.isControlDown()) {
            hideWallPreview();
            return;
        }
        Orientation orientation = evt.isShiftDown() ? Orientation.HORIZONTAL : Orientation.VERTICAL;
        boolean valid = canPreviewWall(lx, lz, orientation);
        showWallPreview(lx, lz, orientation, valid);
    }

    private boolean canPreviewWall(int lx, int lz, Orientation orientation) {
        String cur = controller.getCurrentPlayerId();
        if (cur == null) return false;
        if (boardModel instanceof StdBoard board) {
            StdBoard copy = board.copy();
            return copy.placeWallForPlayer(cur, lx, lz, orientation);
        }
        return false;
    }

    private void showWallPreview(int x, int z, Orientation orientation, boolean valid) {
        if (wallPreview == null || previewOrientation != orientation) {
            if (wallPreview != null) this.getChildren().remove(wallPreview);
            wallPreview = createPreviewWall(orientation);
            previewOrientation = orientation;
            this.getChildren().add(wallPreview);
        }

        double centerX = logicToPx(x) + cellSize / 2.0;
        double centerZ = logicToPz(z) + cellSize / 2.0;
        wallPreview.setTranslateX(centerX);
        wallPreview.setTranslateZ(centerZ);
        wallPreview.setTranslateY(yPos - 4);

        PhongMaterial material = new PhongMaterial(valid ? Color.rgb(84, 156, 115) : Color.rgb(168, 88, 76));
        wallPreview.setMaterial(material);
    }

    private Wall3D createPreviewWall(Orientation orientation) {
        double wallHeight = 10;
        double wallSpan = cellSize * 2.0;
        double wallThickness = 6;
        Wall3D w;
        if (orientation == Orientation.HORIZONTAL) {
            w = new Wall3D(wallSpan, wallHeight, wallThickness, Color.rgb(84, 156, 115));
        } else {
            w = new Wall3D(wallThickness, wallHeight, wallSpan, Color.rgb(84, 156, 115));
        }
        w.setOpacity(0.65);
        w.setMouseTransparent(true);
        return w;
    }

    private void hideWallPreview() {
        if (wallPreview != null) {
            this.getChildren().remove(wallPreview);
            wallPreview = null;
            previewOrientation = null;
        }
    }
}
