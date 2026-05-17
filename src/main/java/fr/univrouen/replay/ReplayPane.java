package fr.univrouen.replay;

import fr.univrouen.QuoridorFactory;
import fr.univrouen.controller.GameController;
import fr.univrouen.model.Orientation;
import fr.univrouen.view.StdBoard3D;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.util.List;

public class ReplayPane extends BorderPane {
    private final StackPane boardHolder = new StackPane();
    private final Label nameLabel = new Label("Aucune partie");
    private final Label moveLabel = new Label("Coup 0/0");
    private final Button prevButton = createControlButton("Precedent", false);
    private final Button nextButton = createControlButton("Suivant", false);
    private final Button playButton = createControlButton("Lecture", true);
    private final Button resetButton = createControlButton("Recommencer", false);
    private final Timeline timeline;

    private ReplayGame replay;
    private int index = 0;
    private GameController controller;
    private PerspectiveCamera camera;
    private Rotate rotateX = new Rotate(-160, Rotate.X_AXIS);
    private Rotate rotateY = new Rotate(80, Rotate.Y_AXIS);
    private double lastX;
    private double lastY;

    public ReplayPane() {
        setPadding(new Insets(24));
        setStyle("-fx-background-color: #f7f3ec;");

        nameLabel.setTextFill(Color.web("#3b352f"));
        nameLabel.setFont(Font.font("Garamond", FontWeight.BOLD, 18));

        moveLabel.setTextFill(Color.web("#6f655a"));
        moveLabel.setFont(Font.font("Garamond", 13));

        VBox header = new VBox(4, nameLabel, moveLabel);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 16, 0));

        boardHolder.setStyle("-fx-background-color: #2b2924; -fx-border-color: #d8d2c7; -fx-border-width: 1;");
        boardHolder.setMinSize(640, 520);
        boardHolder.setPrefSize(640, 520);

        HBox controls = new HBox(10, prevButton, playButton, nextButton, resetButton);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setPadding(new Insets(16, 0, 0, 0));

        setTop(header);
        setCenter(boardHolder);
        setBottom(controls);

        timeline = new Timeline(new KeyFrame(Duration.millis(700), e -> stepForward()));
        timeline.setCycleCount(Animation.INDEFINITE);

        prevButton.setOnAction(e -> stepBackward());
        nextButton.setOnAction(e -> stepForward());
        resetButton.setOnAction(e -> goTo(0));
        playButton.setOnAction(e -> togglePlayback());
    }

    public void loadReplay(ReplayGame replay) {
        this.replay = replay;
        this.index = 0;
        nameLabel.setText(replay == null ? "Aucune partie" : replay.getName());
        stopPlayback();
        rebuildBoardToIndex(0);
    }

    private void togglePlayback() {
        if (timeline.getStatus() == Animation.Status.RUNNING) {
            stopPlayback();
            return;
        }
        if (replay == null || replay.getMoves().isEmpty()) return;
        playButton.setText("Pause");
        timeline.play();
    }

    private void stopPlayback() {
        timeline.stop();
        playButton.setText("Lecture");
    }

    private void stepForward() {
        if (replay == null) return;
        if (index >= replay.getMoves().size()) {
            stopPlayback();
            return;
        }
        applyMovesRange(index, index + 1);
    }

    private void stepBackward() {
        if (replay == null) return;
        if (index <= 0) return;
        rebuildBoardToIndex(index - 1);
    }

    private void goTo(int newIndex) {
        if (replay == null) return;
        int size = replay.getMoves().size();
        int clamped = Math.max(0, Math.min(newIndex, size));
        if (clamped == index && boardHolder.getChildren().isEmpty()) {
            rebuildBoardToIndex(clamped);
            return;
        }
        if (clamped > index) {
            applyMovesRange(index, clamped);
        } else if (clamped < index) {
            rebuildBoardToIndex(clamped);
        }
    }

    private void rebuildBoardToIndex(int newIndex) {
        buildBoard();
        applyMovesRange(0, newIndex);
    }

    private void buildBoard() {
        boardHolder.getChildren().clear();
        try {
            controller = QuoridorFactory.createGameController();
            StdBoard3D board = new StdBoard3D(controller, "/models/plateau.obj", 42.5, 15.0);
            board.setMouseTransparent(true);
            centerBoard(board);

            rotateX = new Rotate(rotateX.getAngle(), Rotate.X_AXIS);
            rotateY = new Rotate(rotateY.getAngle(), Rotate.Y_AXIS);

            Group boardContainer = new Group(board);
            boardContainer.getTransforms().addAll(rotateX, rotateY);

            camera = new PerspectiveCamera(true);
            camera.setNearClip(0.1);
            camera.setFarClip(10000);
            camera.setTranslateX(0);
            camera.setTranslateY(-170);
            camera.setTranslateZ(-1250);

            SubScene subScene = new SubScene(boardContainer, 640, 520, true, SceneAntialiasing.BALANCED);
            subScene.setFill(Color.web("#2b2924"));
            subScene.setCamera(camera);
            subScene.widthProperty().bind(boardHolder.widthProperty());
            subScene.heightProperty().bind(boardHolder.heightProperty());

            attachViewControls(subScene);

            boardHolder.getChildren().add(subScene);
        } catch (Exception ex) {
            Label error = new Label("Erreur: chargement du plateau impossible.");
            error.setTextFill(Color.web("#8b7b65"));
            error.setFont(Font.font("Garamond", 14));
            boardHolder.getChildren().add(error);
        }
    }

    private void centerBoard(StdBoard3D board) {
        Bounds bounds = board.getBoundsInLocal();
        double centerX = (bounds.getMinX() + bounds.getMaxX()) / 2.0;
        double centerZ = (bounds.getMinZ() + bounds.getMaxZ()) / 2.0;
        board.setTranslateX(-centerX);
        board.setTranslateZ(-centerZ);
    }

    private void attachViewControls(SubScene subScene) {
        subScene.setOnMousePressed(e -> {
            lastX = e.getSceneX();
            lastY = e.getSceneY();
        });

        subScene.setOnMouseDragged(e -> {
            if (e.isPrimaryButtonDown()) {
                rotateY.setAngle(rotateY.getAngle() + (e.getSceneX() - lastX) * 0.35);
                rotateX.setAngle(rotateX.getAngle() - (e.getSceneY() - lastY) * 0.35);
                lastX = e.getSceneX();
                lastY = e.getSceneY();
            }
        });

        subScene.addEventHandler(ScrollEvent.SCROLL, e -> {
            if (camera == null) return;
            double zoom = camera.getTranslateZ() + e.getDeltaY() * 3;
            if (zoom > -5000 && zoom < -200) camera.setTranslateZ(zoom);
        });
    }

    private void applyMovesRange(int fromIndex, int toIndex) {
        if (replay == null) return;
        if (controller == null) buildBoard();
        if (controller == null) return;

        List<ReplayMove> moves = replay.getMoves();
        int limit = Math.min(toIndex, moves.size());
        int start = Math.max(0, Math.min(fromIndex, limit));
        for (int i = start; i < limit; i++) {
            ReplayMove move = moves.get(i);
            boolean ok = applyMove(move);
            if (!ok) {
                stopPlayback();
                index = i;
                updateMoveLabel();
                return;
            }
        }
        index = limit;
        updateMoveLabel();
        if (index >= moves.size()) stopPlayback();
    }

    private boolean applyMove(ReplayMove move) {
        if (move == null) return false;
        if (move.getType() == ReplayMove.Type.PAWN) {
            return controller.movePawn(move.getPlayerId(), move.getX(), move.getZ());
        }
        return controller.placeWall(move.getPlayerId(), move.getX(), move.getZ(), move.getOrientation());
    }

    private void updateMoveLabel() {
        if (replay == null) {
            moveLabel.setText("Coup 0/0");
            return;
        }
        int total = replay.getMoves().size();
        if (index == 0) {
            moveLabel.setText("Coup 0/" + total);
        } else {
            ReplayMove last = replay.getMoves().get(index - 1);
            moveLabel.setText("Coup " + index + "/" + total + " : " + describeMove(last));
        }
    }

    private String describeMove(ReplayMove move) {
        if (move == null) return "-";
        String playerName = displayPlayerName(move.getPlayerId());
        if (move.getType() == ReplayMove.Type.PAWN) {
            return playerName + " -> (" + move.getX() + "," + move.getZ() + ")";
        }
        String orient = move.getOrientation() == Orientation.HORIZONTAL ? "H" : "V";
        return playerName + " barriere " + orient + " @ (" + move.getX() + "," + move.getZ() + ")";
    }

    private String displayPlayerName(String playerId) {
        if (playerId == null || playerId.isBlank()) return "-";
        if (playerId.equalsIgnoreCase("p1")) return "Joueur 1";
        if (playerId.equalsIgnoreCase("p2")) return "Joueur 2";
        return "Joueur " + playerId;
    }

    private Button createControlButton(String text, boolean primary) {
        Button button = new Button(text);
        button.setPrefWidth(120);
        button.setPrefHeight(32);
        button.setFont(Font.font("Garamond", FontWeight.BOLD, 13));
        if (primary) {
            button.setTextFill(Color.web("#f5f1e8"));
            button.setStyle(
                "-fx-background-radius: 16;" +
                "-fx-background-color: #3b352f;" +
                "-fx-cursor: hand;"
            );
        } else {
            button.setTextFill(Color.web("#3b352f"));
            button.setStyle(
                "-fx-background-radius: 16;" +
                "-fx-background-color: transparent;" +
                "-fx-border-color: #b8afa2;" +
                "-fx-border-radius: 16;" +
                "-fx-cursor: hand;"
            );
        }
        return button;
    }
}
