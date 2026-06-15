package chess.ui;

import chess.GameSettings;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class PlayModeMenu extends StackPane {

    public PlayModeMenu(StackPane root) {
        setStyle("-fx-background-color: #2b2b2b;");

        VBox card = new VBox(25);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(400);
        card.setStyle("-fx-background-color: #333333; -fx-padding: 40; -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 5);");

        Label title = new Label("GAME MODE");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        title.setTextFill(Color.WHITE);

        VBox sections = new VBox(20);
        sections.setAlignment(Pos.CENTER);

        CheckBox fogToggle = new CheckBox("Fog of War");
        fogToggle.setSelected(GameSettings.isFogOfWar);
        fogToggle.setTextFill(Color.WHITE);
        fogToggle.setFont(Font.font("Segoe UI", 16));
        fogToggle.setStyle("-fx-cursor: hand;");
        fogToggle.selectedProperty().addListener((obs, oldVal, newVal) -> GameSettings.isFogOfWar = newVal);

        // Bot Section
        VBox botSection = new VBox(10);
        botSection.setAlignment(Pos.CENTER);
        Label botLabel = createSectionLabel("🤖 VS BOT");
        Button playTimed = createMenuButton("Timed Match", true);
        Button playTraining = createMenuButton("Training (No Timer)", false);

        playTimed.setOnAction(e -> {
            GameSettings.isBotGame = true;
            GameSettings.isNetworkGame = false;
            root.getChildren().setAll(new TimeSelectionMenu(root, this));
        });

        playTraining.setOnAction(e -> {
            GameSettings.isBotGame = true;
            GameSettings.isNetworkGame = false;
            root.getChildren().setAll(new GameView(root, false, 10));
        });
        botSection.getChildren().addAll(botLabel, playTimed, playTraining);

        // PvP Section
        VBox pvpSection = new VBox(10);
        pvpSection.setAlignment(Pos.CENTER);
        Label pvpLabel = createSectionLabel("👥 PLAYER VS PLAYER");
        Button localPvP = createMenuButton("Local Multiplayer", true);
        
        Button onlineBtn;
        if (GameSettings.currentUser != null) {
            onlineBtn = createMenuButton("Online Lobby", false);
            onlineBtn.setOnAction(e -> {
                try {
                    chess.network.client.ClientConnection.getInstance().connect(GameSettings.serverAddress, GameSettings.port);
                } catch (Exception ex) {}
                root.getChildren().setAll(new LobbyMenu(root, GameSettings.currentUser));
            });
        } else {
            onlineBtn = createMenuButton("Online Login", false);
            onlineBtn.setOnAction(e -> {
                root.getChildren().setAll(new LoginMenu(root, true, this));
            });
        }

        localPvP.setOnAction(e -> {
            GameSettings.isBotGame = false;
            GameSettings.isNetworkGame = false;
            // Force disable fog for local PvP regardless of toggle
            boolean previousFog = GameSettings.isFogOfWar;
            GameSettings.isFogOfWar = false;
            
            TimeSelectionMenu timeMenu = new TimeSelectionMenu(root, this);
            // Restore fog setting when going back
            root.getChildren().setAll(timeMenu);
        });
        pvpSection.getChildren().addAll(pvpLabel, localPvP, onlineBtn);

        sections.getChildren().addAll(fogToggle, botSection, pvpSection);

        Button backButton = new Button("← Back to Menu");
        backButton.setStyle("-fx-background-color: transparent; -fx-text-fill: lightgray; -fx-font-size: 16; -fx-cursor: hand;");
        backButton.setOnMouseEntered(e -> backButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 16; -fx-cursor: hand;"));
        backButton.setOnMouseExited(e -> backButton.setStyle("-fx-background-color: transparent; -fx-text-fill: lightgray; -fx-font-size: 16; -fx-cursor: hand;"));
        backButton.setOnAction(e -> root.getChildren().setAll(new MainMenu(root)));

        card.getChildren().addAll(title, sections, backButton);
        getChildren().add(card);
    }

    private Label createSectionLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        label.setTextFill(Color.web("#769656"));
        label.setPadding(new javafx.geometry.Insets(0, 0, 5, 0));
        return label;
    }

    private Button createMenuButton(String text, boolean isPrimary) {
        Button btn = new Button(text);
        btn.setPrefWidth(280);
        btn.setPrefHeight(45);
        
        String baseStyle = "-fx-font-size: 16;" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 10;" +
                "-fx-cursor: hand;" +
                "-fx-font-weight: bold;";
        
        String colorStyle = isPrimary ? "-fx-background-color: #769656;" : "-fx-background-color: #4a4a4a;";
        btn.setStyle(baseStyle + colorStyle);

        btn.setOnMouseEntered(e -> {
            btn.setStyle(baseStyle + (isPrimary ? "-fx-background-color: #8db56d;" : "-fx-background-color: #5a5a5a;"));
            btn.setScaleX(1.03);
            btn.setScaleY(1.03);
        });

        btn.setOnMouseExited(e -> {
            btn.setStyle(baseStyle + colorStyle);
            btn.setScaleX(1);
            btn.setScaleY(1);
        });
        
        return btn;
    }
}