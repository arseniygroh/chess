package chess.ui;

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

public class MainMenu extends StackPane {

    private final StackPane root;

    public MainMenu(StackPane root) {
        this.root = root;

        createBackground();

        VBox menuContent = new VBox(20);
        menuContent.setAlignment(Pos.CENTER);

        Label title = createTitle();
        Button playButton = createButton("Play");
        Button difficultyButton = createButton("Bot Difficulty");
        Button exitButton = createButton("Exit");

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
            root.getChildren().setAll(new GameView(root, false));
        });
        difficultyButton.setOnAction(event -> {

            root.getChildren().setAll(
                    new DifficultyMenu(root)
            );

        });

        exitButton.setOnAction(event -> {
            System.exit(0);
        });

        Button timedPlayButton = createButton("Play Timed (10m)");

        timedPlayButton.setOnAction(event -> {
            root.getChildren().setAll(new GameView(root, true));
        });
        difficultyButton.setOnAction(event -> {

            root.getChildren().setAll(
                    new DifficultyMenu(root)
            );

        });

        menuContent.getChildren().addAll(
                title,
                playButton,
                timedPlayButton,
                difficultyButton,
                volumeLabel,
                volumeSlider,
                exitButton
        );
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