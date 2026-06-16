package chess.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.layout.Region;


public class TimeSelectionMenu extends VBox {
    private final StackPane root;
    private final Node backScreen;

    public TimeSelectionMenu(StackPane root) {
        this(root, null);
    }

    public TimeSelectionMenu(StackPane root, Node backScreen) {
        this.root = root;
        this.backScreen = backScreen;

        this.setAlignment(Pos.CENTER);
        this.setSpacing(20);
        this.setStyle("-fx-background-color: #2b2b2b;");

        Label title = new Label("⏳ Select Time:");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 40));
        title.setTextFill(Color.WHITE);

        ToggleGroup group = new ToggleGroup();

        RadioButton rb5 = createRadioButton("5 Minutes", 5, group);
        RadioButton rb10 = createRadioButton("10 Minutes", 10, group);
        RadioButton rb15 = createRadioButton("15 Minutes", 15, group);
        RadioButton rb30 = createRadioButton("30 Minutes", 30, group);

        rb10.setSelected(true);

        VBox optionsBox = new VBox(15, rb5, rb10, rb15, rb30);
        optionsBox.setAlignment(Pos.CENTER_LEFT);
        optionsBox.setMaxWidth(Region.USE_PREF_SIZE);
        optionsBox.setPadding(new Insets(0, 0, 0, 15));

        Button continueButton = createMenuButton("Continue");
        Button backButton = createMenuButton("Back");

        continueButton.setOnAction(e -> {
            RadioButton selected = (RadioButton) group.getSelectedToggle();
            int minutes = (int) selected.getUserData();

            root.getChildren().setAll(new GameView(root, true, minutes));
        });

        backButton.setOnAction(e -> {
            if (backScreen != null) {
                root.getChildren().setAll(backScreen);
            } else {
                root.getChildren().setAll(new MainMenu(root));
            }
        });

        this.getChildren().addAll(title, optionsBox, continueButton, backButton);
    }

    private RadioButton createRadioButton(String text, int minutes, ToggleGroup group) {
        RadioButton rb = new RadioButton(text);
        rb.setToggleGroup(group);
        rb.setUserData(minutes);
        rb.setTextFill(Color.WHITE);
        rb.setFont(Font.font("Arial", 16));
        rb.setStyle("-fx-cursor: hand;");
        return rb;
    }

    private Button createMenuButton(String text) {
        Button btn = new Button(text);
        btn.setPrefSize(200, 50);
        btn.setStyle(
                "-fx-background-color: #769656;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 18;" +
                        "-fx-background-radius: 10;" +
                        "-fx-cursor: hand;"
        );

        btn.setOnMouseEntered(e -> btn.setScaleX(1.05));
        btn.setOnMouseExited(e -> btn.setScaleX(1.0));

        return btn;
    }
}
