package fr.univrouen.view;

import fr.univrouen.model.Board;
import fr.univrouen.model.Orientation;
import fr.univrouen.controller.GameController;
import fr.univrouen.controller.GameController.GameListener;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import org.fxyz3d.importers.Importer3D;
import org.fxyz3d.importers.Model3D;

import java.util.HashMap;
import java.util.Map;

/**
 * Implémentation standard de la vue 3D du plateau Quoridor.
 * Manipule uniquement des interfaces (Board, Pawn, GameController) pour respecter SOLID.
 */
public class StdBoard3D extends Group implements Board3D {
    private final double cellSize;
    private final double yPos;
    private final double centerOffset;
    private final Group interactiveGrid = new Group();
    private final Board boardModel;
    private final Map<String, Pawn3D> pawnViews = new HashMap<>();
    private String selectedPawnId = null;
    private final GameController controller;
    private Box[][] gridBoxes;
    private PhongMaterial hoverMat;
    private PhongMaterial idleMat;
    private PhongMaterial validMat;
    private boolean actionTakenThisTurn = false;

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
        this.controller = controller;
        this.boardModel = controller.getModel();

        // Chargement du modèle
        Model3D model = Importer3D.load(getClass().getResource(modelPath));
        for (Node node : model.getMeshViews()) {
            if (node instanceof MeshView) ((MeshView) node).setCullFace(CullFace.NONE);
            this.getChildren().add(node);
        }
        
        createGrid();
        this.getChildren().add(interactiveGrid);

        // Add lights attached to the board for better visibility
        javafx.scene.PointLight pl = new javafx.scene.PointLight(Color.rgb(255,255,255));
        pl.setTranslateY(yPos + 200);
        pl.setTranslateZ(-500);
        pl.setTranslateX(0);
        this.getChildren().add(pl);
        this.getChildren().add(new javafx.scene.AmbientLight(Color.rgb(100,100,100)));

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
                if (p != null) p.updatePosition(logicToPx(x), yPos + 35, logicToPz(z), x, z);
            }
            @Override public void onWallPlaced(int x, int z, Orientation orientation) { /* view already spawns wall on placement */ }
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
                cell.setOnMouseEntered(e -> cell.setMaterial(hoverMat));
                cell.setOnMouseExited(e -> { if (!isValidHighlighted(fx,fz)) cell.setMaterial(idleMat); });
                cell.setOnMouseClicked(e -> onCellClick(fx, fz, px, pz, e));

                interactiveGrid.getChildren().add(cell);
                gridBoxes[x][z] = cell;
            }
        }
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
            if (ok) spawnWallView(lx, lz, evt.isShiftDown() ? Orientation.HORIZONTAL : Orientation.VERTICAL);
            else System.out.println("Wall placement refused (overlap, crossing, isolation, or no walls left)");
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
                if (view != null) {
                    double nx = logicToPx(lx);
                    double nz = logicToPz(lz);
                    view.updatePosition(nx, yPos + 35, nz, lx, lz);
                    view.setSelected(false);
                }
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
        p.updatePosition(px, yPos + 35, pz, 0, 0);
        this.getChildren().add(p);
    }

    private void spawnPawnForModel(fr.univrouen.model.Pawn pm, Color color) {
        double px = logicToPx(pm.getX());
        double pz = logicToPz(pm.getZ());
        Pawn3D p = new Pawn3D(color, 12);
        p.setPawnId(pm.getId());
        p.updatePosition(px, yPos + 35, pz, pm.getX(), pm.getZ());
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
        Wall3D w;
        if (orientation == Orientation.HORIZONTAL) {
            double wWidth = cellSize * 2 + 8;
            double wDepth = 6;
            w = new Wall3D(wWidth, wallHeight, wDepth, Color.DARKRED);
        } else {
            double wWidth = 6;
            double wDepth = cellSize * 2 + 8;
            w = new Wall3D(wWidth, wallHeight, wDepth, Color.DARKRED);
        }
        w.setTranslateX(centerX);
        w.setTranslateZ(centerZ);
        w.setTranslateY(yPos - 4);
        this.getChildren().add(w);
    }

    private double logicToPx(int lx) {
        return lx * cellSize - centerOffset;
    }

    private double logicToPz(int lz) {
        return lz * cellSize - centerOffset;
    }

    private String findPawnIdAt(int lx, int lz) {
        for (Map.Entry<String, Pawn3D> e : pawnViews.entrySet()) {
            Pawn3D p = e.getValue();
            if (p.getLogicX() == lx && p.getLogicZ() == lz) return e.getKey();
        }
        return null;
    }
}
