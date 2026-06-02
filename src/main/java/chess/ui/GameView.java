package chess.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class GameView extends HBox {

    private final StackPane root;
    private final ChessBoard chessBoard;

    public GameView(StackPane root) {
        this.root = root;

        this.setPadding(new Insets(20));
        this.setSpacing(20);
        this.setAlignment(Pos.CENTER);
        this.setStyle("-fx-background-color: #2b2b2b;");

        this.chessBoard = new ChessBoard();
        chessBoard.setPrefSize(600, 600);

        VBox sidePanel = createSidePanel();

        this.getChildren().addAll(chessBoard, sidePanel);
    }

    private VBox createSidePanel() {
        VBox panel = new VBox();
        panel.setSpacing(15);
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setPrefWidth(200);

        Label timerLabel = new Label("10:00");
        timerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 30));
        timerLabel.setTextFill(Color.WHITE);

        Button undoBtn = createSideButton("Undo");
        undoBtn.setOnAction(e -> chessBoard.undoMove());

        Button restartBtn = createSideButton("Restart");
        restartBtn.setOnAction(e -> chessBoard.restartGame());

        Button resignBtn = createSideButton("Resign");
        resignBtn.setOnAction(e -> {
            root.getChildren().setAll(new MainMenu(root));
        });

        panel.getChildren().addAll(timerLabel, undoBtn, restartBtn, resignBtn);
        return panel;
    }

    private Button createSideButton(String text) {
        Button btn = new Button(text);
        btn.setPrefWidth(150);
        btn.setStyle("-fx-background-color: #769656;" +
                " -fx-text-fill: white;" +
                " -fx-font-size: 16;" +
                " -fx-cursor: hand;");
        return btn;
    }
}