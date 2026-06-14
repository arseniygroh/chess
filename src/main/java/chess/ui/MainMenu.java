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

        VBox menuContent = new VBox(20);
        menuContent.setAlignment(Pos.CENTER);

        Label title = createTitle();

        Button playButton = createButton("Play");
        Button timedPlayButton = createButton("Play Timed");
        Button difficultyButton = createButton("Bot Difficulty");
        Button pvpButton = createButton("Local PvP");
        
        Button onlineButton;
        if (GameSettings.currentUser != null) {
            onlineButton = createButton("Online Lobby");
            onlineButton.setOnAction(e -> root.getChildren().setAll(new LobbyMenu(root, GameSettings.currentUser)));
        } else {
            onlineButton = createButton("Online Login");
            onlineButton.setOnAction(e -> root.getChildren().setAll(new LoginMenu(root)));
        }

        Button leaderboardButton = createButton("Leaderboard");
        Button logoutButton = createButton("Logout");
        Button exitButton = createButton("Exit");

        leaderboardButton.setOnAction(e -> {
            try {
                chess.network.client.ClientConnection.getInstance().connect(GameSettings.serverAddress, GameSettings.port);
            } catch (Exception ex) {
                // Already connected or fail
            }
            root.getChildren().setAll(new LeaderboardMenu(root));
        });

        logoutButton.setOnAction(e -> {
            GameSettings.currentUser = null;
            chess.network.client.ClientConnection.getInstance().stop();
            root.getChildren().setAll(new MainMenu(root));
        });

        Label volumeLabel = new Label("🎵 Music Volume");
        volumeLabel.setTextFill(Color.WHITE);

        Slider volumeSlider = new Slider(
                0,
                1,
                MusicManager.getPlayer().getVolume()
        );

        volumeSlider.setPrefWidth(180);
        volumeSlider.setMaxWidth(180);

        volumeSlider.valueProperty().addListener(
                (obs, oldValue, newValue) ->
                        MusicManager.getPlayer()
                                .setVolume(
                                        newValue.doubleValue()
                                )
        );

        playButton.setOnAction(event -> {

            GameSettings.isBotGame = true;

            root.getChildren().setAll(
                    new GameView(root, false, 10)
            );
        });
        pvpButton.setOnAction(e -> {
            GameSettings.isBotGame = false;
            root.getChildren().setAll(
                    new TimeSelectionMenu(root)
            );
        });

        difficultyButton.setOnAction(event -> {

            root.getChildren().setAll(
                    new DifficultyMenu(root)
            );

        });

        exitButton.setOnAction(event -> {
            System.exit(0);
        });



        timedPlayButton.setOnAction(event -> {
            GameSettings.isBotGame = true;
            root.getChildren().setAll(new TimeSelectionMenu(root));
        });
        difficultyButton.setOnAction(event -> {

            root.getChildren().setAll(
                    new DifficultyMenu(root)
            );

        });

        menuContent.getChildren().addAll(
                title,
                playButton,
                pvpButton,
                timedPlayButton,
                onlineButton,
                leaderboardButton,
                difficultyButton,
                volumeLabel,
                volumeSlider
        );
        
        if (GameSettings.currentUser != null) {
            menuContent.getChildren().add(logoutButton);
        }
        
        menuContent.getChildren().add(exitButton);
        this.getChildren().add(menuContent);
    }

    private Label createTitle() {
        Label title = new Label("CHESS");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        title.setTextFill(Color.WHITE);
        return title;
    }

    private Button createButton(String text) {
        Button button = new Button(text);
        button.setPrefSize(200, 50);
        styleButton(button);
        return button;
    }

    private void styleButton(Button button) {
        String style = "-fx-font-size: 18;" +
                "-fx-background-color: #769656;" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 10;" +
                "-fx-cursor: hand;";
        button.setStyle(style);

        button.setOnMouseEntered(e -> {
            button.setScaleX(1.05);
            button.setScaleY(1.05);
        });

        button.setOnMouseExited(e -> {
            button.setScaleX(1);
            button.setScaleY(1);
        });
    }

    private void createBackground() {
        ImageView background = new ImageView(new Image(getClass().getResourceAsStream("/menu/board.png")));
        Rectangle darkOverlay = new Rectangle();

        darkOverlay.widthProperty().bind(root.widthProperty());
        darkOverlay.heightProperty().bind(root.heightProperty());
        darkOverlay.setFill(Color.rgb(0, 0, 0, 0.4));

        background.setEffect(new GaussianBlur(15));
        background.setPreserveRatio(true);
        background.fitWidthProperty().bind(root.widthProperty());
        background.fitHeightProperty().bind(root.heightProperty());

        TranslateTransition animation = new TranslateTransition(Duration.seconds(15), background);
        animation.setFromX(-30);
        animation.setToX(30);
        animation.setAutoReverse(true);
        animation.setCycleCount(Animation.INDEFINITE);
        animation.play();

        this.getChildren().addAll(background, darkOverlay);
    }
}