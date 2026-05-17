package fr.univrouen;

import fr.univrouen.ai.AiEngine;
import fr.univrouen.ai.Move;
import fr.univrouen.controller.GameController;
import fr.univrouen.controller.GameController.GameListener;
import fr.univrouen.controller.MenuController;
import fr.univrouen.model.GameSettings;
import fr.univrouen.model.Orientation;
import fr.univrouen.model.StdBoard;
import fr.univrouen.replay.ReplayGame;
import fr.univrouen.replay.ReplayLoader;
import fr.univrouen.replay.ReplayPane;
import fr.univrouen.view.StdBoard3D;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

public class QuoridorApp extends Application {
    private static final double WINDOW_WIDTH = 1024;
    private static final double WINDOW_HEIGHT = 768;
    private static final String SKIN_ROOT = "/models/skins";
    private static final Map<GameSettings.AiAlgorithm, SkinSpec> AI_SKINS = buildSkinMap();

    private final Rotate rotateX = new Rotate(-45, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(45, Rotate.Y_AXIS);
    private final GameSettings settings = new GameSettings();
    private final MenuController menuController = new MenuController();
    private final AiEngine aiEngine = new AiEngine();
    private double lastX, lastY;
    private volatile boolean aiThinking = false;
    private final EnumSet<GameSettings.AiAlgorithm> unlockedSkins = EnumSet.noneOf(GameSettings.AiAlgorithm.class);
    private SkinSpec activeSkin;

    private Stage primaryStage;
    private Scene menuScene;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        this.menuScene = menuController.createMenuScene(new MenuController.MenuActions() {
            @Override public void onNewGame() { showGameScene(); }
            @Override public void onChooseDifficulty() { showDifficultyScene(); }
            @Override public void onRules() { showRulesScene(); }
            @Override public void onExamples() { showExamplesScene(); }
            @Override public void onQuit() { Platform.exit(); }
        }, WINDOW_WIDTH, WINDOW_HEIGHT);

        stage.setTitle("Quoridor 3D");
        stage.setScene(menuScene);
        stage.show();

    }

    private void showMenuScene() {
        primaryStage.setScene(menuScene);
        primaryStage.show();
    }

    private void showGameScene() {
        aiThinking = false;
        try {
            Scene gameScene = buildGameScene(settings);
            primaryStage.setScene(gameScene);
            primaryStage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
            primaryStage.setScene(createPlaceholderScene(
                "Erreur de lancement",
                "Impossible de charger la scene 3D."
            ));
        }
    }

    private void showDifficultyScene() {
        Scene difficultyScene = menuController.createDifficultyScene(
            settings,
            this::showMenuScene,
            WINDOW_WIDTH,
            WINDOW_HEIGHT
        );
        primaryStage.setScene(difficultyScene);
    }

    private void showRulesScene() {
        primaryStage.setScene(createRulesScene());
    }

    private void showExamplesScene() {
        primaryStage.setScene(createExamplesScene());
    }

    private Scene createPlaceholderScene(String titleText, String bodyText) {
        Label title = new Label(titleText);
        title.setTextFill(Color.web("#f5f1e8"));
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 32));

        Label body = new Label(bodyText);
        body.setTextFill(Color.web("#d4c9b7"));
        body.setFont(Font.font("Georgia", 16));
        body.setWrapText(true);
        body.setMaxWidth(680);

        Button back = new Button("Retour au menu");
        back.setPrefWidth(220);
        back.setPrefHeight(40);
        back.setFont(Font.font("Georgia", FontWeight.BOLD, 15));
        back.setTextFill(Color.web("#2b1b3a"));
        back.setStyle(
            "-fx-background-radius: 22;" +
            "-fx-background-color: linear-gradient(#f6d365, #fda085);" +
            "-fx-cursor: hand;"
        );
        back.setOnAction(e -> showMenuScene());

        VBox box = new VBox(16, title, body, back);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(40));

        StackPane root = new StackPane(box);
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #0b1b2b, #2b1b3a);");

        return new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
    }

    private Scene buildGameScene(GameSettings settings) throws Exception {
        GameController controller = QuoridorFactory.createGameController();
        StdBoard3D board = new StdBoard3D(controller, "/models/plateau.obj", 42.5, 15.0);

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(10000);
        camera.setTranslateX(0);
        camera.setTranslateY(-160);
        camera.setTranslateZ(-1040);

        rotateX.setAngle(-160);
        rotateY.setAngle(80);

        Group boardContainer = new Group(board);
        boardContainer.getTransforms().addAll(rotateX, rotateY);
        SubScene subScene = new SubScene(boardContainer, WINDOW_WIDTH, WINDOW_HEIGHT, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.web("#2b2924"));
        subScene.setCamera(camera);

        StackPane centerPane = new StackPane(subScene);
        centerPane.setStyle("-fx-background-color: #2b2924;");
        subScene.widthProperty().bind(centerPane.widthProperty());
        subScene.heightProperty().bind(centerPane.heightProperty());

        Label winnerLabel = new Label();
        winnerLabel.setTextFill(Color.web("#f5f1e8"));
        winnerLabel.setFont(Font.font("Garamond", FontWeight.BOLD, 28));
        winnerLabel.setWrapText(true);
        winnerLabel.setTextAlignment(TextAlignment.CENTER);
        winnerLabel.setStyle(
            "-fx-background-color: rgba(59,53,47,0.88);" +
            "-fx-background-radius: 12;" +
            "-fx-padding: 14 28;"
        );

        StackPane winnerOverlay = new StackPane(winnerLabel);
        winnerOverlay.setMouseTransparent(true);
        winnerOverlay.setVisible(false);
        winnerOverlay.setOpacity(0);
        centerPane.getChildren().add(winnerOverlay);

        VBox sidePanel = createSidePanel(controller, settings);
        attachAi(controller, settings);
        applyActiveSkin(board, controller, settings);
        attachWinnerOverlay(controller, settings, board, winnerOverlay, winnerLabel);

        BorderPane root = new BorderPane();
        root.setCenter(centerPane);
        root.setRight(sidePanel);
        BorderPane.setMargin(sidePanel, new Insets(24, 24, 24, 0));

        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT, true);
        scene.setFill(Color.web("#2b2924"));

        handleInputs(scene, camera, subScene, controller);

        subScene.setFocusTraversable(true);
        subScene.requestFocus();

        return scene;
    }

    private void attachAi(GameController controller, GameSettings settings) {
        controller.addListener(new GameListener() {
            @Override public void onTurnChanged(String currentPlayerId) {
                triggerAiIfNeeded(controller, settings, currentPlayerId);
            }
            @Override public void onPawnMoved(String pawnId, int x, int z) { }
            @Override public void onWallPlaced(int x, int z, Orientation orientation) { }
            @Override public void onPlayerRegistered(String playerId) { }
            @Override public void onGameWon(String winnerPlayerId) { aiThinking = false; }
        });

        Platform.runLater(() -> triggerAiIfNeeded(controller, settings, controller.getCurrentPlayerId()));
    }

    private void triggerAiIfNeeded(GameController controller, GameSettings settings, String currentPlayerId) {
        if (!settings.isAiPlayer(currentPlayerId)) return;
        if (aiThinking) return;
        aiThinking = true;

        Thread worker = new Thread(() -> {
            Move move = null;
            if (controller.getModel() instanceof StdBoard board) {
                aiEngine.setAlgorithm(AiEngine.Algorithm.valueOf(settings.getAiAlgorithm().name()));
                move = aiEngine.chooseMove(board.copy(), currentPlayerId, controller.getPlayerOrder(), settings);
            }
            Move finalMove = move;
            Platform.runLater(() -> {
                try {
                    if (finalMove != null) {
                        if (finalMove.isPawnMove()) {
                            controller.movePawn(finalMove.getPlayerId(), finalMove.getX(), finalMove.getZ());
                        } else {
                            controller.placeWall(finalMove.getPlayerId(), finalMove.getX(), finalMove.getZ(), finalMove.getOrientation());
                        }
                    }
                } finally {
                    aiThinking = false;
                }
            });
        }, "ai-turn");
        worker.setDaemon(true);
        worker.start();
    }

    private void attachWinnerOverlay(GameController controller, GameSettings settings, StdBoard3D board, StackPane overlay, Label label) {
        controller.addListener(new GameListener() {
            @Override public void onTurnChanged(String currentPlayerId) {
                if (overlay.isVisible()) {
                    overlay.setVisible(false);
                    overlay.setOpacity(0);
                }
            }
            @Override public void onPawnMoved(String pawnId, int x, int z) { }
            @Override public void onWallPlaced(int x, int z, Orientation orientation) { }
            @Override public void onPlayerRegistered(String playerId) { }
            @Override public void onGameWon(String winnerPlayerId) {
                String baseText = "Victoire: " + displayPlayerName(winnerPlayerId);
                String skinText = handleSkinUnlock(board, controller, settings, winnerPlayerId);
                if (skinText != null && !skinText.isBlank()) {
                    label.setText(baseText + "\n" + skinText);
                } else {
                    label.setText(baseText);
                }
                overlay.setVisible(true);
                FadeTransition fade = new FadeTransition(Duration.millis(260), overlay);
                fade.setFromValue(0);
                fade.setToValue(1);
                fade.play();
            }
        });
    }

    private void applyActiveSkin(StdBoard3D board, GameController controller, GameSettings settings) {
        if (activeSkin == null) return;
        applySkinToBoardAndPawn(board, controller, settings, activeSkin);
    }

    private String handleSkinUnlock(StdBoard3D board, GameController controller, GameSettings settings, String winnerPlayerId) {
        if (winnerPlayerId == null || !settings.isAiEnabled()) return null;
        if (settings.isAiPlayer(winnerPlayerId)) return null;

        SkinSpec skin = AI_SKINS.get(settings.getAiAlgorithm());
        if (skin == null) return null;

        boolean applied = applySkinToBoardAndPawn(board, controller, settings, skin);
        if (!applied) return null;

        boolean newlyUnlocked = unlockedSkins.add(settings.getAiAlgorithm());
        activeSkin = skin;
        return newlyUnlocked ? "Skin debloque: " + skin.displayName : "Skin: " + skin.displayName;
    }

    private boolean applySkinToBoardAndPawn(StdBoard3D board, GameController controller, GameSettings settings, SkinSpec skin) {
        boolean boardApplied = board.applyBoardSkin(skin.boardModelPath);
        String ownerId = resolveSkinOwnerId(controller, settings);
        boolean pawnApplied = ownerId != null && board.applyPawnSkin(ownerId, skin.pawnModelPath, skin.pawnScale, skin.pawnYOffset);
        return boardApplied || pawnApplied;
    }

    private String resolveSkinOwnerId(GameController controller, GameSettings settings) {
        List<String> players = controller.getPlayerOrder();
        if (players == null || players.isEmpty()) return null;
        if (!settings.isAiEnabled()) return players.get(0);
        String aiId = settings.getAiPlayerId();
        for (String pid : players) {
            if (!pid.equals(aiId)) return pid;
        }
        return players.get(0);
    }

    private static Map<GameSettings.AiAlgorithm, SkinSpec> buildSkinMap() {
        EnumMap<GameSettings.AiAlgorithm, SkinSpec> skins = new EnumMap<>(GameSettings.AiAlgorithm.class);
        skins.put(GameSettings.AiAlgorithm.MINIMAX,
            new SkinSpec("Minimax", SKIN_ROOT + "/minimax_board.obj", SKIN_ROOT + "/minimax_pawn.obj", 0, 0));
        skins.put(GameSettings.AiAlgorithm.ALPHA_BETA,
            new SkinSpec("Alpha-Beta", SKIN_ROOT + "/alpha_beta_board.obj", SKIN_ROOT + "/alpha_beta_pawn.obj", 0, 0));
        skins.put(GameSettings.AiAlgorithm.NEG_ALPHA_BETA,
            new SkinSpec("Nega-Alpha-Beta", SKIN_ROOT + "/neg_alpha_beta_board.obj", SKIN_ROOT + "/neg_alpha_beta_pawn.obj", 0, 0));
        skins.put(GameSettings.AiAlgorithm.SSS_STAR,
            new SkinSpec("SSS*", SKIN_ROOT + "/sss_star_board.obj", SKIN_ROOT + "/sss_star_pawn.obj", 0, 0));
        return skins;
    }

    private Scene createRulesScene() {
        Label title = new Label("Regles du Quoridor");
        title.setTextFill(Color.web("#2b2620"));
        title.setFont(Font.font("Garamond", FontWeight.BOLD, 32));

        Label subtitle = new Label("Version courte pour une partie elegante.");
        subtitle.setTextFill(Color.web("#4e463c"));
        subtitle.setFont(Font.font("Garamond", 14));

        VBox header = new VBox(6, title, subtitle);
        header.setPadding(new Insets(32, 40, 16, 40));

        Region divider = new Region();
        divider.setPrefHeight(1);
        divider.setStyle("-fx-background-color: #d8d2c7;");

        VBox sections = new VBox(18,
            createRuleSection("Objectif", "Atteindre la ligne opposee avec votre pion."),
            createRuleSection("Tour de jeu", "A chaque tour, choisissez: deplacement du pion ou pose d'une barriere."),
            createRuleSection("Barrieres", "Une barriere couvre deux segments. Elle ne doit jamais couper tous les chemins."),
            createRuleSection("Saut et diagonale", "Si un pion est devant vous, sautez-le. Si le saut est bloque, deplacez en diagonale."),
            createRuleSection("Commandes", "Clic sur un pion pour le selectionner. Shift: mur horizontal. Ctrl: mur vertical."),
            createRuleSection("Raccourcis", "N: nouvelle partie. Home: reset camera. Molette: zoom.")
        );
        sections.setPadding(new Insets(24, 40, 24, 40));

        ScrollPane scroll = new ScrollPane(sections);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-background: #f7f3ec;" +
            "-fx-control-inner-background: #f7f3ec;"
        );

        Button back = createPanelButton("Retour au menu", false);
        back.setOnAction(e -> showMenuScene());

        HBox footer = new HBox(back);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(16, 40, 32, 40));

        BorderPane root = new BorderPane();
        root.setTop(new VBox(header, divider));
        root.setCenter(scroll);
        root.setBottom(footer);
        root.setStyle("-fx-background-color: #f7f3ec;");

        return new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
    }

    private VBox createRuleSection(String titleText, String bodyText) {
        Label title = new Label(titleText);
        title.setTextFill(Color.web("#2f2a24"));
        title.setFont(Font.font("Garamond", FontWeight.BOLD, 16));

        Label body = new Label(bodyText);
        body.setTextFill(Color.web("#4e463c"));
        body.setFont(Font.font("Garamond", 14));
        body.setWrapText(true);
        body.setMaxWidth(720);

        VBox section = new VBox(6, title, body);
        section.setPadding(new Insets(12, 16, 12, 16));
        section.setStyle(
            "-fx-background-color: #fcfaf6;" +
            "-fx-border-color: #e3ded3;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;"
        );
        return section;
    }

    private Scene createExamplesScene() {
        ReplayPane replayPane = new ReplayPane();
        List<ReplayGame> games = ReplayLoader.loadAll();
        if (!games.isEmpty()) {
            replayPane.loadReplay(games.get(0));
        }

        Label listTitle = new Label("Exemples de parties");
        listTitle.setTextFill(Color.web("#3b352f"));
        listTitle.setFont(Font.font("Garamond", FontWeight.BOLD, 18));

        Label listHint = new Label("Selectionnez une partie pour la rejouer pas a pas.");
        listHint.setTextFill(Color.web("#6f655a"));
        listHint.setFont(Font.font("Garamond", 13));
        listHint.setWrapText(true);

        VBox listBox = new VBox(12, listTitle, listHint);
        listBox.setPrefWidth(260);
        listBox.setPadding(new Insets(24));
        listBox.setStyle(
            "-fx-background-color: #f5f1e8;" +
            "-fx-border-color: #d8d2c7;" +
            "-fx-border-width: 0 1 0 0;"
        );

        if (games.isEmpty()) {
            Label empty = new Label("Aucune partie disponible.");
            empty.setTextFill(Color.web("#6f655a"));
            empty.setFont(Font.font("Garamond", 13));
            listBox.getChildren().add(empty);
        } else {
            for (ReplayGame game : games) {
                Button entry = createListButton(game.getName());
                entry.setOnAction(e -> replayPane.loadReplay(game));
                listBox.getChildren().add(entry);
            }
        }

        Button back = createPanelButton("Retour au menu", false);
        back.setOnAction(e -> showMenuScene());

        HBox footer = new HBox(back);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(16, 24, 24, 24));

        BorderPane root = new BorderPane();
        root.setLeft(listBox);
        root.setCenter(replayPane);
        root.setBottom(footer);
        root.setStyle("-fx-background-color: #f7f3ec;");

        return new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
    }

    private Button createListButton(String text) {
        Button button = new Button(text);
        button.setPrefWidth(210);
        button.setPrefHeight(32);
        button.setFont(Font.font("Garamond", FontWeight.BOLD, 13));
        button.setTextFill(Color.web("#3b352f"));
        button.setStyle(
            "-fx-background-radius: 16;" +
            "-fx-background-color: transparent;" +
            "-fx-border-color: #b8afa2;" +
            "-fx-border-radius: 16;" +
            "-fx-cursor: hand;"
        );
        return button;
    }

    private VBox createSidePanel(GameController controller, GameSettings settings) {
        Label title = new Label("Tableau de jeu");
        title.setTextFill(Color.web("#3b352f"));
        title.setFont(Font.font("Garamond", FontWeight.BOLD, 20));

        Label playerHeader = new Label("Joueur courant");
        playerHeader.setTextFill(Color.web("#6f655a"));
        playerHeader.setFont(Font.font("Garamond", 13));

        Label playerValue = new Label("-");
        playerValue.setTextFill(Color.web("#3b352f"));
        playerValue.setFont(Font.font("Garamond", FontWeight.BOLD, 16));

        Label wallsHeader = new Label("Barrieres restantes");
        wallsHeader.setTextFill(Color.web("#6f655a"));
        wallsHeader.setFont(Font.font("Garamond", 13));

        Label wallsValue = new Label("-");
        wallsValue.setTextFill(Color.web("#3b352f"));
        wallsValue.setFont(Font.font("Garamond", 14));
        wallsValue.setWrapText(true);

        Label difficultyHeader = new Label("Difficulte");
        difficultyHeader.setTextFill(Color.web("#6f655a"));
        difficultyHeader.setFont(Font.font("Garamond", 13));

        Label difficultyValue = new Label(settings.getDifficultyLabel() + " (profondeur " + settings.getSearchDepth() + ")");
        difficultyValue.setTextFill(Color.web("#3b352f"));
        difficultyValue.setFont(Font.font("Garamond", 14));

        Label algoHeader = new Label("Algorithme IA");
        algoHeader.setTextFill(Color.web("#6f655a"));
        algoHeader.setFont(Font.font("Garamond", 13));

        Label algoValue = new Label(settings.getAiAlgorithmLabel());
        algoValue.setTextFill(Color.web("#3b352f"));
        algoValue.setFont(Font.font("Garamond", 14));

        Region spacer = new Region();
        spacer.setMinHeight(20);

        Button restart = createPanelButton("Recommencer", true);
        restart.setOnAction(e -> showGameScene());

        Button back = createPanelButton("Retour au menu", false);
        back.setOnAction(e -> showMenuScene());

        VBox panel = new VBox(10,
            title,
            playerHeader,
            playerValue,
            wallsHeader,
            wallsValue,
            difficultyHeader,
            difficultyValue,
            algoHeader,
            algoValue,
            spacer,
            restart,
            back
        );
        panel.setPrefWidth(260);
        panel.setStyle(
            "-fx-background-color: #f5f1e8;" +
            "-fx-border-color: #d8d2c7;" +
            "-fx-border-width: 0 0 0 1;" +
            "-fx-padding: 24;"
        );

        Runnable refresh = () -> {
            String cur = controller.getCurrentPlayerId();
            playerValue.setText(displayPlayerName(cur));
            StringBuilder sb = new StringBuilder();
            for (String pid : controller.getPlayerOrder()) {
                sb.append(displayPlayerName(pid)).append(": ").append(controller.getWallsRemaining(pid)).append("\n");
            }
            wallsValue.setText(sb.toString().trim());
        };

        controller.addListener(new GameListener() {
            @Override public void onTurnChanged(String currentPlayerId) { refresh.run(); }
            @Override public void onPawnMoved(String pawnId, int x, int z) { }
            @Override public void onWallPlaced(int x, int z, Orientation orientation) { refresh.run(); }
            @Override public void onPlayerRegistered(String playerId) { refresh.run(); }
            @Override public void onGameWon(String winnerPlayerId) {
                playerValue.setText("Gagnant: " + displayPlayerName(winnerPlayerId));
                playerValue.setTextFill(Color.web("#8b7b65"));
            }
        });

        refresh.run();
        return panel;
    }

    private Button createPanelButton(String text, boolean primary) {
        Button button = new Button(text);
        button.setPrefWidth(220);
        button.setPrefHeight(36);
        button.setFont(Font.font("Garamond", FontWeight.BOLD, 14));
        if (primary) {
            button.setTextFill(Color.web("#f5f1e8"));
            button.setStyle(
                "-fx-background-radius: 18;" +
                "-fx-background-color: #3b352f;" +
                "-fx-cursor: hand;"
            );
        } else {
            button.setTextFill(Color.web("#3b352f"));
            button.setStyle(
                "-fx-background-radius: 18;" +
                "-fx-background-color: transparent;" +
                "-fx-border-color: #b8afa2;" +
                "-fx-border-radius: 18;" +
                "-fx-cursor: hand;"
            );
        }
        return button;
    }

    private String displayPlayerName(String playerId) {
        if (playerId == null || playerId.isBlank()) return "-";
        if (playerId.equalsIgnoreCase("p1")) return "Joueur 1";
        if (playerId.equalsIgnoreCase("p2")) return "Joueur 2";
        return "Joueur " + playerId;
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
                showGameScene();
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

    private static final class SkinSpec {
        private final String displayName;
        private final String boardModelPath;
        private final String pawnModelPath;
        private final double pawnScale;
        private final double pawnYOffset;

        private SkinSpec(String displayName, String boardModelPath, String pawnModelPath, double pawnScale, double pawnYOffset) {
            this.displayName = displayName;
            this.boardModelPath = boardModelPath;
            this.pawnModelPath = pawnModelPath;
            this.pawnScale = pawnScale;
            this.pawnYOffset = pawnYOffset;
        }
    }

    public static void main(String[] args) { launch(args); }
}