package chess.ui;

import chess.GameSettings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class SkinSelectionMenu extends StackPane {

    public SkinSelectionMenu(StackPane root) {
        setStyle("-fx-background-color: #2b2b2b;");

        VBox content = new VBox(30);
        content.setAlignment(Pos.CENTER);

        Label title = new Label("CHOOSE YOUR PIECES");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 40));
        title.setTextFill(Color.WHITE);

        ToggleGroup group = new ToggleGroup();

        HBox sevenRow = createSkinOption("Seven", "seven", group);
        HBox alphaRow = createSkinOption("Alpha", "alpha", group);
        HBox cardinalRow = createSkinOption("Cardinal", "cardinal", group);

        group.getToggles().forEach(toggle -> {
            if (toggle.getUserData().equals(GameSettings.pieceSkin)) {
                toggle.setSelected(true);
            }
        });

        VBox optionsBox = new VBox(20, sevenRow, alphaRow, cardinalRow);
        optionsBox.setAlignment(Pos.CENTER);
        optionsBox.setMaxWidth(Region.USE_PREF_SIZE);
        optionsBox.setPadding(new Insets(0, 0, 0, 20));

        Button saveBtn = createMenuButton("Save & Back", "#769656");
        saveBtn.setOnAction(e -> {
            RadioButton selected = (RadioButton) group.getSelectedToggle();
            if (selected != null) {
                GameSettings.pieceSkin = (String) selected.getUserData();
            }
            root.getChildren().setAll(new MainMenu(root));
        });

        Button backBtn = createMenuButton("Cancel", "#555555");
        backBtn.setOnAction(e -> root.getChildren().setAll(new MainMenu(root)));

        content.getChildren().addAll(title, optionsBox, saveBtn, backBtn);
        getChildren().add(content);
    }

    private HBox createSkinOption(String text, String folderName, ToggleGroup group) {
        RadioButton rb = new RadioButton(text);
        rb.setToggleGroup(group);
        rb.setUserData(folderName);
        rb.setTextFill(Color.WHITE);
        rb.setFont(Font.font("Arial", 20));
        rb.setStyle("-fx-cursor: hand;");
        rb.setMinWidth(120);

        HBox previewBox = new HBox(10);
        previewBox.setAlignment(Pos.CENTER_LEFT);
        previewBox.setPadding(new Insets(0, 0, 0, 20));

        try {
            String path = "/pieces/" + folderName + "/";
            previewBox.getChildren().addAll(
                    createPreviewImage(path + "wK.png"),
                    createPreviewImage(path + "bN.png")
            );
        } catch (Exception e) {
            System.err.println("Could not load preview for: " + folderName);
        }

        HBox fullRow = new HBox(10, rb, previewBox);
        fullRow.setAlignment(Pos.CENTER_LEFT);

        fullRow.setOnMouseClicked(e -> rb.setSelected(true));
        fullRow.setStyle("-fx-cursor: hand; -fx-padding: 5; -fx-background-radius: 5;");
        fullRow.setOnMouseEntered(e -> fullRow.setStyle("-fx-background-color: #3c3f41; -fx-cursor: hand; -fx-padding: 5; -fx-background-radius: 5;"));
        fullRow.setOnMouseExited(e -> fullRow.setStyle("-fx-background-color: transparent; -fx-padding: 5;"));

        return fullRow;
    }

    private ImageView createPreviewImage(String path) {
        Image img = new Image(getClass().getResourceAsStream(path));
        ImageView view = new ImageView(img);
        view.setFitHeight(45);
        view.setPreserveRatio(true);
        view.setSmooth(true);
        return view;
    }

    private Button createMenuButton(String text, String color) {
        Button btn = new Button(text);
        btn.setPrefSize(250, 50);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-size: 18; -fx-background-radius: 10; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setScaleX(1.05));
        btn.setOnMouseExited(e -> btn.setScaleX(1.0));
        return btn;
    }
}