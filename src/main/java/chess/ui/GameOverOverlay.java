package chess.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import static javafx.scene.layout.Region.USE_PREF_SIZE;

public class GameOverOverlay extends StackPane {

    public GameOverOverlay(
            String winner,
            Runnable onNewGame,
            Runnable onReview,
            Runnable onMainMenu
    ) {


        setAlignment(Pos.CENTER);

        setMaxSize(
                USE_PREF_SIZE,
                USE_PREF_SIZE
        );

        VBox dialog = new VBox(20);

        dialog.setAlignment(Pos.CENTER);

        dialog.setPrefSize(350, 350);

        dialog.setStyle(
                "-fx-background-color: #2b2b2b;" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-color: #769656;" +
                        "-fx-border-width: 3;" +
                        "-fx-border-radius: 20;"
        );

        Label title = new Label("CHECKMATE!");

        title.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        32
                )
        );

        title.setTextFill(Color.WHITE);

        Label winnerLabel =
                new Label(winner + " Wins!");

        winnerLabel.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        22
                )
        );

        winnerLabel.setTextFill(Color.LIGHTGRAY);

        Button newGame =
                createButton("New Game");

        Button mainMenu =
                createButton("Main Menu");
        Button review =
                createButton("Review Game");

        review.setOnAction(e -> onReview.run());

        newGame.setOnAction(e -> onNewGame.run());

        mainMenu.setOnAction(e -> onMainMenu.run());

        dialog.getChildren().addAll(
                title,
                winnerLabel,
                newGame,
                review,
                mainMenu
        );

        getChildren().add(dialog);
    }

    private Button createButton(String text) {

        Button btn = new Button(text);

        btn.setPrefSize(200, 50);

        btn.setStyle(
                "-fx-font-size: 18;" +
                        "-fx-background-color: #769656;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 10;"
        );

        return btn;
    }
}
