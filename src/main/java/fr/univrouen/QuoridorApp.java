package fr.univrouen;

import fr.univrouen.controller.GameController;
import fr.univrouen.controller.GameController.GameListener;
import fr.univrouen.model.Orientation;
import fr.univrouen.view.StdBoard3D;
import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.control.Label;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

public class QuoridorApp extends Application {
    private final Rotate rotateX = new Rotate(-45, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(45, Rotate.Y_AXIS);
    private double lastX, lastY;

    @Override
    public void start(Stage stage) throws Exception {
        // 1. Crée le contrôleur et la vue 3D via la Factory (respect du principe SOLID)
        GameController controller = QuoridorFactory.createGameController();
        StdBoard3D board = new StdBoard3D(controller, "/models/plateau.obj", 42.5, 15.0);

        // 2. Setup Caméra (fixed above board)
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(10000);
        // Position the camera so it looks centered at the board from above
        camera.setTranslateX(0);      // center on board (board grid is at x ~-170 to +170)
        camera.setTranslateY(-160);   // raise camera well above the scene
        camera.setTranslateZ(-1040);   // pull camera back so whole board is visible
        // Initialize rotation angles (will attach to boardContainer, not camera)
        rotateX.setAngle(-160);        // look straight down
        rotateY.setAngle(80);          // no horizontal rotation for top-down view

        // 3. Racine UI: Group with 3D board
        Group boardContainer = new Group(board);
        // Attach rotation transforms to boardContainer so dragging rotates the board, not camera
        boardContainer.getTransforms().addAll(rotateX, rotateY);
        boardContainer.getChildren().add(new PointLight(Color.WHITE));
        boardContainer.getChildren().add(new AmbientLight(Color.rgb(80, 80, 80)));

        // 4. CRÉATION DE LA SCÈNE AVEC COULEUR DE FOND
        // Paramètres : Racine, Largeur, Hauteur, DepthBuffer (true), Antialiasing
        Scene scene = new Scene(boardContainer, 1024, 768, true, SceneAntialiasing.BALANCED);
        
        // Choix de la couleur : web, rgb ou constante
        scene.setFill(Color.web("#1e272e")); // Gris anthracite très sombre
        scene.setCamera(camera);

        stage.setTitle("Quoridor 3D");
        stage.setScene(scene);
        stage.show();
        
        // Create separate HUD window overlay (at frame level, not in scene)
        Label playerLabel = new Label("Player: -");
        playerLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        Label wallsLabel = new Label("Walls: -");
        wallsLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
        
        VBox hud = new VBox(8);
        hud.setStyle("-fx-padding: 15; -fx-background-color: rgba(0,0,0,0.7); -fx-text-fill: white;");
        hud.getChildren().addAll(playerLabel, wallsLabel);
        
        Scene hudScene = new Scene(hud, 280, 120);
        hudScene.setFill(Color.TRANSPARENT);
        
        Stage hudWindow = new Stage();
        hudWindow.setTitle("HUD");
        hudWindow.initStyle(javafx.stage.StageStyle.UNDECORATED);
        hudWindow.setAlwaysOnTop(true);
        hudWindow.setScene(hudScene);
        hudWindow.show();
        
        // Position HUD at top-left of main window
        hudWindow.setX(stage.getX() + 10);
        hudWindow.setY(stage.getY() + 10);
        
        // Keep HUD positioned relative to main stage
        stage.xProperty().addListener((obs, oldVal, newVal) -> hudWindow.setX(stage.getX() + 10));
        stage.yProperty().addListener((obs, oldVal, newVal) -> hudWindow.setY(stage.getY() + 10));
        
        // Listen to controller to update HUD
        controller.addListener(new GameListener() {
            @Override public void onTurnChanged(String currentPlayerId) {
                playerLabel.setText("Player: " + (currentPlayerId==null?"-":currentPlayerId));
                StringBuilder sb = new StringBuilder("Walls: ");
                for (String pid : controller.getPlayerOrder()) {
                    sb.append(pid).append("=").append(controller.getWallsRemaining(pid)).append(" ");
                }
                wallsLabel.setText(sb.toString());
            }
            @Override public void onPawnMoved(String pawnId, int x, int z) { }
            @Override public void onWallPlaced(int x, int z, Orientation orientation) { }
            @Override public void onPlayerRegistered(String playerId) { }
            @Override public void onGameWon(String winnerPlayerId) {
                playerLabel.setText("WINNER: " + winnerPlayerId);
                playerLabel.setStyle("-fx-text-fill: gold; -fx-font-size: 16px; -fx-font-weight: bold;");
                wallsLabel.setText("Press 'N' for new game");
            }
        });

        // Initialize HUD from controller state
        String cur = controller.getCurrentPlayerId();
        playerLabel.setText("Player: " + (cur==null?"-":cur));
        StringBuilder sb = new StringBuilder("Walls: ");
        for (String pid : controller.getPlayerOrder()) {
            sb.append(pid).append("=").append(controller.getWallsRemaining(pid)).append(" ");
        }
        wallsLabel.setText(sb.toString());

        // 5. Gestion des événements (Souris / Zoom)
        handleInputs(scene, camera, board, controller);
        
        // Ensure the 3D board is the primary focus (main view)
        board.setFocusTraversable(true);
        board.requestFocus();
    }

    private void handleInputs(Scene scene, PerspectiveCamera camera, Node focusTarget, GameController controller) {
        final double defaultCamX = 0;
        final double defaultCamY = -800;
        final double defaultCamZ = -600;
        final double defaultRotX = -90;
        final double defaultRotY = 0;

        scene.setOnMousePressed(e -> {
            lastX = e.getSceneX();
            lastY = e.getSceneY();
        });

        scene.setOnMouseDragged(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                rotateY.setAngle(rotateY.getAngle() + (e.getSceneX() - lastX) * 0.3);
                rotateX.setAngle(rotateX.getAngle() - (e.getSceneY() - lastY) * 0.3);
                lastX = e.getSceneX();
                lastY = e.getSceneY();
            }
        });

        // Zoom with mouse wheel
        scene.addEventHandler(ScrollEvent.SCROLL, e -> {
            double zoom = camera.getTranslateZ() + e.getDeltaY() * 3;
            if (zoom > -5000 && zoom < -200) camera.setTranslateZ(zoom);
        });

        // Keyboard controls: WASD to move camera on X/Z, R/F to raise/lower Y, +/- to zoom, N for new game, HOME to reset camera
        scene.setOnKeyPressed((KeyEvent ke) -> {
            double step = 20.0;
            double zoomStep = 50.0;
            if (ke.getCode() == KeyCode.W) camera.setTranslateZ(camera.getTranslateZ() + step);
            if (ke.getCode() == KeyCode.S) camera.setTranslateZ(camera.getTranslateZ() - step);
            if (ke.getCode() == KeyCode.A) camera.setTranslateX(camera.getTranslateX() - step);
            if (ke.getCode() == KeyCode.D) camera.setTranslateX(camera.getTranslateX() + step);
            if (ke.getCode() == KeyCode.R) camera.setTranslateY(camera.getTranslateY() - step);
            if (ke.getCode() == KeyCode.F) camera.setTranslateY(camera.getTranslateY() + step);
            // Zoom in/out: + (or =) to zoom in, - to zoom out
            if (ke.getCode() == KeyCode.PLUS || ke.getCode() == KeyCode.EQUALS) {
                double newZ = camera.getTranslateZ() + zoomStep;
                if (newZ > -5000 && newZ < -200) camera.setTranslateZ(newZ);
            }
            if (ke.getCode() == KeyCode.MINUS) {
                double newZ = camera.getTranslateZ() - zoomStep;
                if (newZ > -5000 && newZ < -200) camera.setTranslateZ(newZ);
            }
            // Reset camera to default view (HOME key)
            if (ke.getCode() == KeyCode.HOME) {
                camera.setTranslateX(defaultCamX);
                camera.setTranslateY(defaultCamY);
                camera.setTranslateZ(defaultCamZ);
                rotateX.setAngle(defaultRotX);
                rotateY.setAngle(defaultRotY);
                System.out.println("Camera reset to default view");
            }
            // New game (N key)
            if (ke.getCode() == KeyCode.N) {
                System.out.println("Starting new game...");
                controller.resetGame();
            }
            // Print camera coordinates to console for easy tuning
            printCameraStatus(camera);
        });

        // Ensure scene gets keyboard focus when clicked (focus the provided node)
        scene.setOnMouseClicked(e -> focusTarget.requestFocus());
    }

    private void printCameraStatus(PerspectiveCamera camera) {
        System.out.println(String.format(
            "Camera TX=%.1f TY=%.1f TZ=%.1f | rotX=%.1f rotY=%.1f",
            camera.getTranslateX(), camera.getTranslateY(), camera.getTranslateZ(), rotateX.getAngle(), rotateY.getAngle()
        ));
    }

    public static void main(String[] args) { launch(args); }
}