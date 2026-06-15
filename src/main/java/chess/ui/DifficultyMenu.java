package chess.ui;

import chess.GameSettings;
import chess.bot.BotDifficulty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class DifficultyMenu extends StackPane {

    public DifficultyMenu(StackPane root) {
        setStyle("-fx-background-color: #2b2b2b;");

        VBox card = new VBox(25);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(400);
        card.setStyle("-fx-background-color: #333333; -fx-padding: 40; -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 5);");

        Label title = new Label("BOT DIFFICULTY");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        title.setTextFill(Color.WHITE);

        ToggleGroup group = new ToggleGroup();

        RadioButton easy = createDifficultyOption("Easy", BotDifficulty.EASY, group);
        RadioButton medium = createDifficultyOption("Medium", BotDifficulty.MEDIUM, group);
        RadioButton hard = createDifficultyOption("Hard", BotDifficulty.HARD, group);

        switch (GameSettings.botDifficulty) {
            case EASY -> easy.setSelected(true);
            case MEDIUM -> medium.setSelected(true);
            case HARD -> hard.setSelected(true);
        }

        VBox optionsBox = new VBox(15, easy, medium, hard);
        optionsBox.setAlignment(Pos.CENTER);
        optionsBox.setStyle("-fx-padding: 10 0;");

        Button saveButton = createMenuButton("Apply & Save", true);
        
        saveButton.setOnAction(event -> {
            if (easy.isSelected()) GameSettings.botDifficulty = BotDifficulty.EASY;
            if (medium.isSelected()) GameSettings.botDifficulty = BotDifficulty.MEDIUM;
            if (hard.isSelected()) GameSettings.botDifficulty = BotDifficulty.HARD;
            root.getChildren().setAll(new MainMenu(root));
        });

        Button backButton = new Button("← Back to Menu");
        backButton.setStyle("-fx-background-color: transparent; -fx-text-fill: Color.LIGHTGRAY; -fx-font-size: 16; -fx-cursor: hand;");
        backButton.setOnMouseEntered(e -> backButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 16; -fx-cursor: hand;"));
        backButton.setOnMouseExited(e -> backButton.setStyle("-fx-background-color: transparent; -fx-text-fill: Color.LIGHTGRAY; -fx-font-size: 16; -fx-cursor: hand;"));
        backButton.setOnAction(event -> root.getChildren().setAll(new MainMenu(root)));

        card.getChildren().addAll(title, optionsBox, saveButton, backButton);
        getChildren().add(card);
    }

    private RadioButton createDifficultyOption(String text, BotDifficulty diff, ToggleGroup group) {
        RadioButton rb = new RadioButton(text);
        rb.setToggleGroup(group);
        rb.setFont(Font.font("Segoe UI", 18));
        rb.setTextFill(Color.WHITE);
        rb.setStyle("-fx-cursor: hand; -fx-padding: 5;");
        return rb;
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