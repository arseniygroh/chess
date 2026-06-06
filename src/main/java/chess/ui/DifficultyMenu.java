package chess.ui;

import chess.GameSettings;
import chess.bot.BotDifficulty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class DifficultyMenu extends StackPane{

    public DifficultyMenu(StackPane root) {
        setStyle(
                "-fx-background-color: #2b2b2b;"
        );
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);


        VBox content = new VBox(20);

        content.setAlignment(Pos.CENTER);

        Label title = new Label("BOT DIFFICULTY");

        title.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        40
                )
        );

        title.setTextFill(Color.WHITE);
        Label currentDifficulty = new Label(
                "Selected: " +
                        GameSettings.botDifficulty
        );

        currentDifficulty.setTextFill(
                Color.LIGHTGRAY
        );

        currentDifficulty.setFont(
                Font.font(16)
        );

        ToggleGroup group = new ToggleGroup();

        RadioButton easy = new RadioButton("Easy");
        RadioButton medium = new RadioButton("Medium");
        RadioButton hard = new RadioButton("Hard");

        easy.setToggleGroup(group);
        medium.setToggleGroup(group);
        hard.setToggleGroup(group);

        easy.setTextFill(Color.WHITE);
        medium.setTextFill(Color.WHITE);
        hard.setTextFill(Color.WHITE);

        switch (GameSettings.botDifficulty) {
            case EASY -> easy.setSelected(true);
            case MEDIUM -> medium.setSelected(true);
            case HARD -> hard.setSelected(true);
        }

        Button saveButton = new Button("Save");
        Button backButton = new Button("Back");

        saveButton.setOnAction(event -> {

            if (easy.isSelected()) {
                GameSettings.botDifficulty = BotDifficulty.EASY;
            }

            if (medium.isSelected()) {
                GameSettings.botDifficulty = BotDifficulty.MEDIUM;
            }

            if (hard.isSelected()) {
                GameSettings.botDifficulty = BotDifficulty.HARD;
            }

            root.getChildren().setAll(
                    new MainMenu(root)
            );
        });

        backButton.setOnAction(event ->
                root.getChildren().setAll(
                        new MainMenu(root)
                )
        );

        content.getChildren().addAll(
                title,
                currentDifficulty,
                easy,
                medium,
                hard,
                saveButton,
                backButton
        );

        styleButton(saveButton);
        styleButton(backButton);

        getChildren().add(content);


    }
    private void styleButton(Button button) {

        button.setPrefSize(200, 50);

        button.setStyle(
                "-fx-font-size: 18;" +
                        "-fx-background-color: #769656;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 10;"
        );
    }
}