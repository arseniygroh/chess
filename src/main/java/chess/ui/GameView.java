package chess.ui;

import chess.GameSettings;
import chess.bot.ChessBot;
import chess.bot.MinimaxBot;
import chess.bot.RandomBot;
import chess.model.PlayerColor;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
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

    private final Label whiteTimerLabel = new Label("10:00");
    private final Label blackTimerLabel = new Label("10:00");
    private int whiteSeconds = 600;
    private int blackSeconds = 600;
    private Timeline timeline;
    private final boolean isTimed;

    public GameView(StackPane root, boolean isTimed) {
        this.root = root;
        this.isTimed = isTimed;

        this.setPadding(new Insets(20));
        this.setSpacing(20);
        this.setAlignment(Pos.CENTER);
        this.setStyle("-fx-background-color: #2b2b2b;");

        ChessBot bot;

        switch (GameSettings.botDifficulty) {

            case EASY ->
                    bot = new RandomBot();

            case MEDIUM ->
                    bot = new MinimaxBot(2);

            default ->
                    bot = new MinimaxBot(3);
        }

        this.chessBoard = new ChessBoard(bot);
        chessBoard.setPrefSize(600, 600);

        VBox sidePanel = createSidePanel();

        this.getChildren().addAll(chessBoard, sidePanel);

        if (isTimed) {
            chessBoard.setOnFirstAction(() -> {
                startTimerLogic();
            });
        }
    }

    private void startTimerLogic() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateTime()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void updateTime() {
        if (chessBoard.getBoardState().getActiveColor() == PlayerColor.WHITE) {
            whiteSeconds--;
            whiteTimerLabel.setText(formatTime(whiteSeconds));
            whiteTimerLabel.setTextFill(Color.YELLOW);
            blackTimerLabel.setTextFill(Color.WHITE);
        } else {
            blackSeconds--;
            blackTimerLabel.setText(formatTime(blackSeconds));
            blackTimerLabel.setTextFill(Color.YELLOW);
            whiteTimerLabel.setTextFill(Color.WHITE);
        }

        if (whiteSeconds <= 0 || blackSeconds <= 0) {
            gameOverByTime();
        }
    }

    private String formatTime(int totalSeconds) {
        int mins = totalSeconds / 60;
        int secs = totalSeconds % 60;
        return String.format("%02d:%02d", mins, secs);
    }

    private void gameOverByTime() {
        if (timeline != null) timeline.stop();
        chessBoard.setDisable(true);
        String winner = (whiteSeconds <= 0) ? "Black (Bot)" : "White (You)";

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Time Over");
        alert.setHeaderText(null);
        alert.setContentText("Game over! Winner: " + winner);
        alert.show();
    }

    private VBox createSidePanel() {
        VBox panel = new VBox(20);
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setPrefWidth(200);

        if (isTimed) {
            VBox timersBox = new VBox(10);
            timersBox.setAlignment(Pos.CENTER);

            Label blackLabel = new Label("BLACK (BOT)");
            blackLabel.setTextFill(Color.LIGHTGRAY);
            styleTimerLabel(blackTimerLabel);

            Label whiteLabel = new Label("WHITE (YOU)");
            whiteLabel.setTextFill(Color.LIGHTGRAY);
            styleTimerLabel(whiteTimerLabel);

            timersBox.getChildren().addAll(blackLabel, blackTimerLabel, new Label(""), whiteLabel, whiteTimerLabel);
            panel.getChildren().add(timersBox);
        }

        VBox buttonsBox = new VBox(10);
        buttonsBox.setAlignment(Pos.CENTER);

        Button undoBtn = createSideButton("Undo");
        undoBtn.setOnAction(e -> chessBoard.undoMove());

        Button restartBtn = createSideButton("Restart");
        restartBtn.setOnAction(e ->{
                chessBoard.restartGame();

        if (isTimed) {
            if (timeline != null) {
                timeline.stop();
            }

            whiteSeconds = 600;
            blackSeconds = 600;

            whiteTimerLabel.setText("10:00");
            blackTimerLabel.setText("10:00");

            whiteTimerLabel.setTextFill(Color.WHITE);
            blackTimerLabel.setTextFill(Color.WHITE);
        }
    });

        Button resignBtn = createSideButton("Resign");
        resignBtn.setOnAction(e -> {
            if (timeline != null) timeline.stop();
            root.getChildren().setAll(new MainMenu(root));
        });

        buttonsBox.getChildren().addAll(undoBtn, restartBtn, resignBtn);
        panel.getChildren().add(buttonsBox);

        return panel;
    }

    private void styleTimerLabel(Label label) {
        label.setFont(Font.font("Monospaced", FontWeight.BOLD, 32));
        label.setTextFill(Color.WHITE);
        label.setStyle("-fx-background-color: #3c3f41;" +
                " -fx-padding: 10;" +
                " -fx-background-radius: 8;" +
                " -fx-border-color: #769656;" +
                " -fx-border-radius: 8;");
    }

    private Button createSideButton(String text) {
        Button btn = new Button(text);
        btn.setPrefWidth(160);
        btn.setStyle("-fx-background-color: #769656;" +
                " -fx-text-fill: white;" +
                " -fx-font-size: 16;" +
                " -fx-font-weight: bold;" +
                " -fx-cursor: hand;");
        return btn;
    }
}