package chess.ui;

import chess.GameSettings;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.animation.Animation;
import javafx.animation.TranslateTransition;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import javafx.scene.control.ChoiceBox;
import javafx.collections.FXCollections;

public class MainMenu extends StackPane {

    private final StackPane root;

    public MainMenu(StackPane root) {
        this.root = root;

        createBackground();

        // Main Container (Card)
        VBox card = new VBox(30);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(400);
        card.setMaxHeight(650);
        card.setStyle("-fx-background-color: rgba(43, 43, 43, 0.9);" +
                      "-fx-background-radius: 20;" +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 20, 0, 0, 10);" +
                      "-fx-padding: 40;");

        Label title = createTitle();

        VBox buttonsBox = new VBox(15);
        buttonsBox.setAlignment(Pos.CENTER);

        Button playButton = createButton("Play", true);
        Button profileButton = createButton("👤 Profile", false);
        Button leaderboardButton = createButton("🏆 Leaderboard", false);
        Button skinsButton = createButton("🎨 Skins", false);
        Button difficultyButton = createButton("⚙ Bot Difficulty", false);
        
        String authText = GameSettings.currentUser == null ? "🔑 Login" : "🚪 Logout";
        Button authButton = createButton(authText, false);

        playButton.setOnAction(event -> root.getChildren().setAll(new PlayModeMenu(root)));

        profileButton.setOnAction(event -> {
            if (GameSettings.currentUser != null) {
                root.getChildren().setAll(new ProfileMenu(root, GameSettings.currentUser, true, false, true));
            } else {
                root.getChildren().setAll(new LoginMenu(root, false, this));
            }
        });

        authButton.setOnAction(event -> {
            if (GameSettings.currentUser == null) {
                root.getChildren().setAll(new LoginMenu(root, false, this));
            } else {
                chess.util.CredentialsManager.clearCredentials();
                GameSettings.currentUser = null;
                chess.network.client.ClientConnection.getInstance().stop();
                root.getChildren().setAll(new MainMenu(root));
            }
        });

        leaderboardButton.setOnAction(e -> {
            try {
                chess.network.client.ClientConnection.getInstance().connect(GameSettings.serverAddress, GameSettings.port);
            } catch (Exception ex) {}
            root.getChildren().setAll(new LeaderboardMenu(root));
        });

        difficultyButton.setOnAction(event -> root.getChildren().setAll(new DifficultyMenu(root)));
        skinsButton.setOnAction(e -> root.getChildren().setAll(new SkinSelectionMenu(root)));

        buttonsBox.getChildren().addAll(playButton, profileButton, leaderboardButton, skinsButton, difficultyButton, authButton);

        // Volume Control
        VBox volumeBox = new VBox(8);
        volumeBox.setAlignment(Pos.CENTER);
        Label volumeLabel = new Label("🎵 Music Volume");
        volumeLabel.setTextFill(Color.LIGHTGRAY);
        volumeLabel.setFont(Font.font("Arial", 14));

        Slider volumeSlider = new Slider(0, 1, MusicManager.getPlayer().getVolume());
        volumeSlider.setPrefWidth(200);
        volumeSlider.setMaxWidth(200);
        volumeSlider.setStyle("-fx-control-inner-background: #4a4a4a;");
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> 
            MusicManager.getPlayer().setVolume(newVal.doubleValue())
        );

        volumeBox.getChildren().addAll(volumeLabel, volumeSlider);

        Button exitButton = new Button("Exit Game");
        exitButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #a04040; -fx-font-size: 16; -fx-cursor: hand;");
        exitButton.setOnMouseEntered(e -> exitButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff5555; -fx-font-size: 16; -fx-cursor: hand;"));
        exitButton.setOnMouseExited(e -> exitButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #a04040; -fx-font-size: 16; -fx-cursor: hand;"));
        exitButton.setOnAction(event -> System.exit(0));

        card.getChildren().addAll(title, buttonsBox, volumeBox, exitButton);
        this.getChildren().add(card);
    }

    private Label createTitle() {
        Label title = new Label("CHESS");
        title.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 60));
        title.setTextFill(Color.WHITE);
        title.setStyle("-fx-effect: dropshadow(gaussian, rgba(118, 150, 86, 0.8), 10, 0, 0, 0);");
        return title;
    }

    private Button createButton(String text, boolean isPrimary) {
        Button button = new Button(text);
        button.setPrefWidth(280);
        button.setPrefHeight(50);
        
        String baseStyle = "-fx-font-size: 18;" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 12;" +
                "-fx-cursor: hand;" +
                "-fx-font-weight: bold;";
        
        String colorStyle = isPrimary ? "-fx-background-color: #769656;" : "-fx-background-color: #4a4a4a;";
        button.setStyle(baseStyle + colorStyle);

        button.setOnMouseEntered(e -> {
            button.setStyle(baseStyle + (isPrimary ? "-fx-background-color: #8db56d;" : "-fx-background-color: #5a5a5a;"));
            button.setScaleX(1.03);
            button.setScaleY(1.03);
        });

        button.setOnMouseExited(e -> {
            button.setStyle(baseStyle + colorStyle);
            button.setScaleX(1);
            button.setScaleY(1);
        });
        
        return button;
    }

    private void createBackground() {
        try {
            ImageView background = new ImageView(new Image(getClass().getResourceAsStream("/menu/board.png")));
            background.setEffect(new GaussianBlur(20));
            background.setPreserveRatio(true);
            background.fitWidthProperty().bind(root.widthProperty().add(100));
            background.fitHeightProperty().bind(root.heightProperty().add(100));

            Rectangle darkOverlay = new Rectangle();
            darkOverlay.widthProperty().bind(root.widthProperty());
            darkOverlay.heightProperty().bind(root.heightProperty());
            darkOverlay.setFill(Color.rgb(0, 0, 0, 0.6));

            TranslateTransition animation = new TranslateTransition(Duration.seconds(20), background);
            animation.setFromX(-50);
            animation.setToX(50);
            animation.setAutoReverse(true);
            animation.setCycleCount(Animation.INDEFINITE);
            animation.play();

            this.getChildren().addAll(background, darkOverlay);
        } catch (Exception e) {
            this.setStyle("-fx-background-color: #1e1e1e;");
        }
    }
}