package chess.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class GameView extends HBox {

    public GameView() {
        this.setPadding(new Insets(20));
        this.setSpacing(20);
        this.setAlignment(Pos.CENTER);
        this.setStyle("-fx-background-color: #2b2b2b;");

        ChessBoard chessBoard = new ChessBoard();

        chessBoard.setPrefSize(600, 600);
        chessBoard.setMinSize(600, 600);

        VBox sidePanel = createSidePanel();

        this.getChildren().addAll(chessBoard, sidePanel);
    }

    private VBox createSidePanel() {
        VBox panel = new VBox();
        panel.setSpacing(30);
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setPrefWidth(200);
        panel.setPadding(new Insets(20, 0, 0, 0));

        Label timerLabel = new Label("10:00");
        timerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        timerLabel.setTextFill(Color.WHITE);

        Button undoButton = createSideButton("Undo");
        Button resignButton = createSideButton("Resign");
        Button settingsButton = createSideButton("Settings");

        panel.getChildren().addAll(timerLabel, undoButton, resignButton, settingsButton);
        return panel;
    }

    private Button createSideButton(String text) {
        Button btn = new Button(text);
        btn.setPrefWidth(150);
        btn.setStyle(
                "-fx-background-color: #769656;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16;" +
                        "-fx-background-radius: 5;" +
                        "-fx-cursor: hand;"
        );
        return btn;
    }
}