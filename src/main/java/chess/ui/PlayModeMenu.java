package chess.ui;

import chess.GameSettings;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class PlayModeMenu extends StackPane {

    public PlayModeMenu(StackPane root) {
        setStyle("-fx-background-color: #2b2b2b;");

        VBox content = new VBox(15);
        content.setAlignment(Pos.CENTER);

        Label title = new Label("SELECT GAME MODE");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setTextFill(Color.WHITE);
        VBox.setMargin(title, new javafx.geometry.Insets(0, 0, 10, 0));

        Label botLabel = createSectionLabel("Bot");
        Button playTimed = createMenuButton("Play Timed");
        Button playTraining = createMenuButton("Training");

        playTimed.setOnAction(e -> {
            GameSettings.isBotGame = true;
            root.getChildren().setAll(new TimeSelectionMenu(root));
        });

        playTraining.setOnAction(e -> {
            GameSettings.isBotGame = true;
            root.getChildren().setAll(new GameView(root, false, 10));
        });

        Label pvpLabel = createSectionLabel("PvP");
        Button localPvP = createMenuButton("Local");
        
        Button onlineBtn;
        if (GameSettings.currentUser != null) {
            onlineBtn = createMenuButton("Online Lobby");
            onlineBtn.setOnAction(e -> {
                root.getChildren().setAll(new LobbyMenu(root, GameSettings.currentUser));
            });
        } else {
            onlineBtn = createMenuButton("Online Login");
            onlineBtn.setOnAction(e -> {
                root.getChildren().setAll(new LoginMenu(root));
            });
        }

        localPvP.setOnAction(e -> {
            GameSettings.isBotGame = false;
            root.getChildren().setAll(new TimeSelectionMenu(root));
        });

        Button backButton = createMenuButton("Back");
        backButton.setStyle("-fx-background-color: #555555; -fx-text-fill: white; -fx-font-size: 18; -fx-background-radius: 10;");
        backButton.setOnAction(e -> root.getChildren().setAll(new MainMenu(root)));
        VBox.setMargin(backButton, new javafx.geometry.Insets(10, 0, 0, 0));

        content.getChildren().addAll(
                title,
                botLabel, playTimed, playTraining,
                pvpLabel, localPvP, onlineBtn,
                backButton
        );

        getChildren().add(content);
    }

    private Label createSectionLabel(String text) {
        Label label = new Label("--- " + text + " ---");
        label.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        label.setTextFill(Color.web("#769656"));
        return label;
    }

    private Button createMenuButton(String text) {
        Button btn = new Button(text);
        btn.setPrefSize(250, 45);
        btn.setStyle(
                "-fx-font-size: 18;" +
                        "-fx-background-color: #769656;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-cursor: hand;"
        );
        btn.setOnMouseEntered(e -> btn.setScaleX(1.05));
        btn.setOnMouseExited(e -> btn.setScaleX(1.0));
        return btn;
    }
}