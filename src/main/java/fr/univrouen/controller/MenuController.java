package fr.univrouen.controller;

import fr.univrouen.model.GameSettings;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.util.List;

public class MenuController {
    public interface MenuActions {
        void onNewGame();
        void onChooseDifficulty();
        void onRules();
        void onExamples();
        void onQuit();
    }

    public Scene createMenuScene(MenuActions actions, double width, double height) {
        StackPane root = new StackPane();
        root.setPrefSize(width, height);

        Pane background = createBackground(width, height);

        Label title = new Label("QUORIDOR 3D");
        title.setTextFill(Color.web("#f5f1e8"));
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 42));

        Label subtitle = new Label("Strategie, murs, et course");
        subtitle.setTextFill(Color.web("#d4c9b7"));
        subtitle.setFont(Font.font("Georgia", 16));

        Button newGame = createMenuButton("Nouvelle Partie");
        newGame.setOnAction(e -> actions.onNewGame());

        Button difficulty = createMenuButton("Choisir la difficulte");
        difficulty.setOnAction(e -> actions.onChooseDifficulty());

        Button rules = createMenuButton("Regles");
        rules.setOnAction(e -> actions.onRules());

        Button examples = createMenuButton("Exemples de parties");
        examples.setOnAction(e -> actions.onExamples());

        Button quit = createMenuButton("Quitter");
        quit.setOnAction(e -> actions.onQuit());

        VBox menu = new VBox(14, title, subtitle, newGame, difficulty, rules, examples, quit);
        menu.setAlignment(Pos.CENTER);
        menu.setPadding(new Insets(40));

        root.getChildren().addAll(background, menu);

        playIntro(List.of(title, subtitle, newGame, difficulty, rules, examples, quit));

        return new Scene(root, width, height);
    }

    public Scene createDifficultyScene(GameSettings settings, Runnable onBack, double width, double height) {
        StackPane root = new StackPane();
        root.setPrefSize(width, height);

        Pane background = createBackground(width, height);

        Label title = new Label("Choisir la difficulte");
        title.setTextFill(Color.web("#f5f1e8"));
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 36));

        Label subtitle = new Label("La difficulte ajuste la profondeur de recherche de l'IA.");
        subtitle.setTextFill(Color.web("#d4c9b7"));
        subtitle.setFont(Font.font("Georgia", 14));

        Button easy = createChoiceButton("Facile");
        Button medium = createChoiceButton("Moyen");
        Button hard = createChoiceButton("Difficile");

        Label difficultyCurrent = new Label();
        difficultyCurrent.setTextFill(Color.web("#d4c9b7"));
        difficultyCurrent.setFont(Font.font("Georgia", 14));

        Label algoHeader = new Label("Algorithme IA");
        algoHeader.setTextFill(Color.web("#d4c9b7"));
        algoHeader.setFont(Font.font("Georgia", FontWeight.BOLD, 14));

        Button minimax = createChoiceButton("Minimax");
        Button alphaBeta = createChoiceButton("Alpha-Beta");
        Button negAlphaBeta = createChoiceButton("Nega-Alpha-Beta");
        Button sssStar = createChoiceButton("SSS*");

        Label algoCurrent = new Label();
        algoCurrent.setTextFill(Color.web("#d4c9b7"));
        algoCurrent.setFont(Font.font("Georgia", 13));

        easy.setOnAction(e -> {
            settings.setDifficulty(GameSettings.Difficulty.EASY);
            applyDifficultySelection(settings, easy, medium, hard, difficultyCurrent);
        });
        medium.setOnAction(e -> {
            settings.setDifficulty(GameSettings.Difficulty.MEDIUM);
            applyDifficultySelection(settings, easy, medium, hard, difficultyCurrent);
        });
        hard.setOnAction(e -> {
            settings.setDifficulty(GameSettings.Difficulty.HARD);
            applyDifficultySelection(settings, easy, medium, hard, difficultyCurrent);
        });

        minimax.setOnAction(e -> {
            settings.setAiAlgorithm(GameSettings.AiAlgorithm.MINIMAX);
            applyAlgorithmSelection(settings, minimax, alphaBeta, negAlphaBeta, sssStar, algoCurrent);
        });
        alphaBeta.setOnAction(e -> {
            settings.setAiAlgorithm(GameSettings.AiAlgorithm.ALPHA_BETA);
            applyAlgorithmSelection(settings, minimax, alphaBeta, negAlphaBeta, sssStar, algoCurrent);
        });
        negAlphaBeta.setOnAction(e -> {
            settings.setAiAlgorithm(GameSettings.AiAlgorithm.NEG_ALPHA_BETA);
            applyAlgorithmSelection(settings, minimax, alphaBeta, negAlphaBeta, sssStar, algoCurrent);
        });
        sssStar.setOnAction(e -> {
            settings.setAiAlgorithm(GameSettings.AiAlgorithm.SSS_STAR);
            applyAlgorithmSelection(settings, minimax, alphaBeta, negAlphaBeta, sssStar, algoCurrent);
        });

        applyDifficultySelection(settings, easy, medium, hard, difficultyCurrent);
        applyAlgorithmSelection(settings, minimax, alphaBeta, negAlphaBeta, sssStar, algoCurrent);

        Button back = createMenuButton("Retour au menu");
        back.setOnAction(e -> onBack.run());

        VBox menu = new VBox(
            12,
            title,
            subtitle,
            easy,
            medium,
            hard,
            difficultyCurrent,
            algoHeader,
            minimax,
            alphaBeta,
            negAlphaBeta,
            sssStar,
            algoCurrent,
            back
        );
        menu.setAlignment(Pos.CENTER);
        menu.setPadding(new Insets(40));

        root.getChildren().addAll(background, menu);

        playIntro(List.of(title, subtitle, easy, medium, hard, difficultyCurrent, algoHeader, minimax, alphaBeta, negAlphaBeta, sssStar, algoCurrent, back));

        return new Scene(root, width, height);
    }

    private Button createMenuButton(String text) {
        Button button = new Button(text);
        button.setPrefWidth(260);
        button.setPrefHeight(44);
        button.setFont(Font.font("Georgia", FontWeight.BOLD, 16));
        button.setTextFill(Color.web("#2b1b3a"));
        button.setStyle(
            "-fx-background-radius: 24;" +
            "-fx-background-color: linear-gradient(#f6d365, #fda085);" +
            "-fx-cursor: hand;"
        );

        DropShadow shadow = new DropShadow(12, Color.rgb(0, 0, 0, 0.35));
        shadow.setOffsetY(4);
        button.setEffect(shadow);

        button.setOnMouseEntered(e -> button.setStyle(
            "-fx-background-radius: 24;" +
            "-fx-background-color: linear-gradient(#fde68a, #f59e9e);" +
            "-fx-cursor: hand;"
        ));

        button.setOnMouseExited(e -> button.setStyle(
            "-fx-background-radius: 24;" +
            "-fx-background-color: linear-gradient(#f6d365, #fda085);" +
            "-fx-cursor: hand;"
        ));

        return button;
    }

    private Button createChoiceButton(String text) {
        Button button = new Button(text);
        button.setPrefWidth(260);
        button.setPrefHeight(44);
        button.setFont(Font.font("Georgia", FontWeight.BOLD, 16));
        button.setTextFill(Color.web("#f5f1e8"));
        button.setStyle(
            "-fx-background-radius: 22;" +
            "-fx-background-color: rgba(255,255,255,0.12);" +
            "-fx-border-color: rgba(255,255,255,0.25);" +
            "-fx-border-radius: 22;" +
            "-fx-cursor: hand;"
        );

        DropShadow shadow = new DropShadow(10, Color.rgb(0, 0, 0, 0.25));
        shadow.setOffsetY(3);
        button.setEffect(shadow);

        button.setOnMouseEntered(e -> {
            if (!isSelected(button)) {
                button.setStyle(
                    "-fx-background-radius: 22;" +
                    "-fx-background-color: rgba(255,255,255,0.2);" +
                    "-fx-border-color: rgba(255,255,255,0.4);" +
                    "-fx-border-radius: 22;" +
                    "-fx-cursor: hand;"
                );
            }
        });

        button.setOnMouseExited(e -> {
            if (!isSelected(button)) {
                button.setStyle(
                    "-fx-background-radius: 22;" +
                    "-fx-background-color: rgba(255,255,255,0.12);" +
                    "-fx-border-color: rgba(255,255,255,0.25);" +
                    "-fx-border-radius: 22;" +
                    "-fx-cursor: hand;"
                );
            }
        });

        return button;
    }

    private Pane createBackground(double width, double height) {
        Pane background = new Pane();
        background.setPrefSize(width, height);
        background.setStyle("-fx-background-color: linear-gradient(to bottom right, #0b1b2b, #2b1b3a);");

        Circle glowLeft = new Circle(240, Color.rgb(255, 255, 255, 0.06));
        glowLeft.setTranslateX(-260);
        glowLeft.setTranslateY(-200);

        Circle glowRight = new Circle(180, Color.rgb(255, 255, 255, 0.05));
        glowRight.setTranslateX(320);
        glowRight.setTranslateY(180);

        background.getChildren().addAll(glowLeft, glowRight);
        return background;
    }

    private void applyDifficultySelection(GameSettings settings, Button easy, Button medium, Button hard, Label current) {
        GameSettings.Difficulty diff = settings.getDifficulty();
        setSelected(easy, diff == GameSettings.Difficulty.EASY);
        setSelected(medium, diff == GameSettings.Difficulty.MEDIUM);
        setSelected(hard, diff == GameSettings.Difficulty.HARD);

        current.setText(
            "Selection actuelle: " + settings.getDifficultyLabel() +
            " (profondeur " + settings.getSearchDepth() + ")"
        );
    }

    private void applyAlgorithmSelection(GameSettings settings, Button minimax, Button alphaBeta,
                                         Button negAlphaBeta, Button sssStar, Label current) {
        GameSettings.AiAlgorithm algo = settings.getAiAlgorithm();
        setSelected(minimax, algo == GameSettings.AiAlgorithm.MINIMAX);
        setSelected(alphaBeta, algo == GameSettings.AiAlgorithm.ALPHA_BETA);
        setSelected(negAlphaBeta, algo == GameSettings.AiAlgorithm.NEG_ALPHA_BETA);
        setSelected(sssStar, algo == GameSettings.AiAlgorithm.SSS_STAR);

        current.setText("Algo actuel: " + settings.getAiAlgorithmLabel());
    }

    private void setSelected(Button button, boolean selected) {
        button.getProperties().put("selected", selected);
        if (selected) {
            button.setTextFill(Color.web("#1b2838"));
            button.setStyle(
                "-fx-background-radius: 22;" +
                "-fx-background-color: linear-gradient(#84fab0, #8fd3f4);" +
                "-fx-border-color: rgba(255,255,255,0.5);" +
                "-fx-border-radius: 22;" +
                "-fx-cursor: hand;"
            );
        } else {
            button.setTextFill(Color.web("#f5f1e8"));
            button.setStyle(
                "-fx-background-radius: 22;" +
                "-fx-background-color: rgba(255,255,255,0.12);" +
                "-fx-border-color: rgba(255,255,255,0.25);" +
                "-fx-border-radius: 22;" +
                "-fx-cursor: hand;"
            );
        }
    }

    private boolean isSelected(Button button) {
        return Boolean.TRUE.equals(button.getProperties().get("selected"));
    }

    private void playIntro(List<Node> nodes) {
        int index = 0;
        for (Node node : nodes) {
            node.setOpacity(0);
            FadeTransition fade = new FadeTransition(Duration.millis(420), node);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.setDelay(Duration.millis(90L * index));

            TranslateTransition slide = new TranslateTransition(Duration.millis(420), node);
            slide.setFromY(12);
            slide.setToY(0);
            slide.setDelay(Duration.millis(90L * index));

            new ParallelTransition(fade, slide).play();
            index++;
        }
    }
}
