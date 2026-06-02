package chess.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class MainMenu extends VBox {

    private final StackPane root;

    public MainMenu(StackPane root) {

        this.root = root;
        createBackground();
        Label title = createTitle();

        Button playButton = createButton("Play");
        Button difficultyButton = createButton("Bot Difficulty");
        Button exitButton = createButton("Exit");

        playButton.setOnAction(event -> {

            ChessBoard board = new ChessBoard();

            root.getChildren().setAll(board);

        });

        exitButton.setOnAction(event -> {

            System.exit(0);

        });

        setAlignment(Pos.CENTER);
        setFillWidth(true);
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        setSpacing(20);

        getChildren().addAll(
                title,
                playButton,
                difficultyButton,
                exitButton
        );
    }

    private Label createTitle() {

        Label title = new Label("CHESS");

        title.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        48
                )
        );

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

        String style =
                "-fx-font-size: 18;" +
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

        button.setOnMousePressed(e -> {

            button.setScaleX(0.95);
            button.setScaleY(0.95);

        });

        button.setOnMouseReleased(e -> {

            button.setScaleX(1.05);
            button.setScaleY(1.05);

        });
    }
    private void createBackground() {

        ImageView background = new ImageView(
                new Image(
                        getClass().getResourceAsStream(
                                "/menu/board.png"
                        )
                )
        );

        Rectangle darkOverlay = new Rectangle();

        darkOverlay.widthProperty().bind(
                root.widthProperty()
        );

        darkOverlay.heightProperty().bind(
                root.heightProperty()
        );

        darkOverlay.setFill(
                Color.rgb(0, 0, 0, 0.4)
        );

        background.setEffect(
                new GaussianBlur(15)
        );

        background.setPreserveRatio(true);

        background.setScaleX(1.05);
        background.setScaleY(1.05);

        background.fitWidthProperty().bind(
                root.widthProperty()
        );

        background.fitHeightProperty().bind(
                root.heightProperty()
        );

        TranslateTransition animation =
                new TranslateTransition(
                        Duration.seconds(15),
                        background
                );

        animation.setFromX(-30);
        animation.setToX(30);

        animation.setAutoReverse(true);

        animation.setCycleCount(
                Animation.INDEFINITE
        );

        animation.play();

        root.getChildren().addAll(
                background,
                darkOverlay
        );
    }
}