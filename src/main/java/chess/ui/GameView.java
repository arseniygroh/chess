package chess.ui;

import chess.GameSettings;
import chess.bot.ChessBot;
import chess.bot.MinimaxBot;
import chess.bot.RandomBot;
import chess.model.PlayerColor;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.*;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.Region;
import javafx.util.Duration;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Optional;
import java.util.function.Consumer;

public class GameView extends HBox {

    private final StackPane root;
    private final ChessBoard chessBoard;
    private final VBox moveHistoryBox = new VBox(5);
    private final Label whiteTimerLabel = new Label("10:00");
    private final Label blackTimerLabel = new Label("10:00");
    private int whiteSeconds = 600;
    private int blackSeconds = 600;
    private Timeline timeline;
    private final boolean isTimed;
    private final int initialSeconds;
    private final Label turnLabel = new Label();
    private VBox reviewBox = new VBox(15);

    private int reviewIndex = 0;

    private Label reviewLabel = new Label();
    private Label historyTitle;



    public GameView(StackPane root, boolean isTimed, int minutes) {
        this.root = root;
        this.isTimed = isTimed;
        this.whiteSeconds = minutes * 60;
        this.blackSeconds = minutes * 60;
        this.initialSeconds = minutes * 60;

        this.setPadding(new Insets(20));
        this.setSpacing(60);
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
        chessBoard.setOnMovePlayed(moveText -> {

            Label moveLabel = new Label(moveText);

            moveLabel.setTextFill(Color.WHITE);

            moveHistoryBox.getChildren().add(moveLabel);

            updateTurnLabel();
        });
        chessBoard.setOnGameOver(
                this::showGameOverOverlay
        );
        chessBoard.setPrefSize(600, 600);
        updateTurnLabel();

        VBox sidePanel = createSidePanel();

        this.getChildren().addAll(chessBoard, sidePanel);

        if (isTimed) {
            chessBoard.setOnFirstAction(() -> {
                startTimerLogic();
            });
        }
        whiteTimerLabel.setText(formatTime(whiteSeconds));
        blackTimerLabel.setText(formatTime(blackSeconds));
    }
    private VBox createReviewPanel() {

        reviewBox.setAlignment(Pos.CENTER);

        reviewLabel.setTextFill(Color.WHITE);
        Label reviewTitle =
                new Label("REVIEW MODE");

        reviewTitle.setTextFill(Color.WHITE);

        reviewTitle.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        18
                )
        );

        Button prevBtn = createSideButton("◀ Previous");

        Button nextBtn = createSideButton("Next ▶");

        Button exitBtn = createSideButton("Exit Review");

        prevBtn.setOnAction(e -> {

            if (reviewIndex > 0) {

                reviewIndex--;

                chessBoard.showPosition(reviewIndex);

                updateReviewLabel();
            }
        });

        nextBtn.setOnAction(e -> {

            if (reviewIndex < chessBoard.getHistorySize() - 1) {

                reviewIndex++;

                chessBoard.showPosition(reviewIndex);

                updateReviewLabel();
            }
        });

        exitBtn.setOnAction(e -> {

            chessBoard.showPosition(
                    chessBoard.getHistorySize() - 1
            );

            moveHistoryBox.setVisible(true);
            moveHistoryBox.setManaged(true);

            historyTitle.setVisible(true);
            historyTitle.setManaged(true);

            reviewBox.setVisible(false);
            reviewBox.setManaged(false);
        });

        reviewBox.getChildren().addAll(
                reviewTitle,
                reviewLabel,
                prevBtn,
                nextBtn,
                exitBtn
        );

        reviewBox.setVisible(false);
        reviewBox.setManaged(false);

        return reviewBox;
    }
    private void updateReviewLabel() {

        reviewLabel.setText(
                "Move "
                        + reviewIndex
                        + " / "
                        + (chessBoard.getHistorySize() - 1)
        );
    }
    private void showGameOverOverlay(String winner) {

        chessBoard.setEffect(
                new GaussianBlur(8)
        );

        GameOverOverlay overlay =
                new GameOverOverlay(
                        winner,

                        () -> root.getChildren().setAll(
                                new GameView(
                                        root,
                                        isTimed,
                                        initialSeconds / 60
                                )
                        ),

                        () -> {

                            chessBoard.setEffect(null);

                            reviewIndex = 0;

                            chessBoard.showPosition(reviewIndex);

                            updateReviewLabel();

                            moveHistoryBox.setVisible(false);
                            moveHistoryBox.setManaged(false);

                            historyTitle.setVisible(false);
                            historyTitle.setManaged(false);

                            reviewBox.setVisible(true);
                            reviewBox.setManaged(true);

                            root.getChildren().removeIf(
                                    node -> node instanceof GameOverOverlay
                            );
                        },

                        () -> root.getChildren().setAll(
                                new MainMenu(root)
                        )
                );

        StackPane.setAlignment(
                overlay,
                Pos.CENTER
        );

        root.getChildren().add(
                overlay
        );
    }
    private void startTimerLogic() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateTime()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
    private void updateTurnLabel() {

        if (chessBoard.getBoardState().getActiveColor() == PlayerColor.WHITE) {

            turnLabel.setText("⚪ White to move");

            turnLabel.setTextFill(Color.WHITE);

        } else {

            turnLabel.setText("⚫ Black (Bot) to move");

            turnLabel.setTextFill(Color.LIGHTGRAY);
        }
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
        VBox panel = new VBox();
        turnLabel.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        18
                )
        );
        historyTitle = new Label("Move History");
        historyTitle.setTextFill(Color.WHITE);
        historyTitle.setFont(
                Font.font("Arial", FontWeight.BOLD, 18)
        );

        moveHistoryBox.setAlignment(Pos.TOP_LEFT);
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setPrefWidth(220);

        if (isTimed) {
            VBox timersBox = new VBox(10);
            timersBox.setAlignment(Pos.CENTER);
            timersBox.setPadding(new Insets(20, 0, 0, 0));

            Label blackLabel = new Label("BLACK (BOT)");
            blackLabel.setTextFill(Color.LIGHTGRAY);
            styleTimerLabel(blackTimerLabel);

            Label whiteLabel = new Label("WHITE (YOU)");
            whiteLabel.setTextFill(Color.LIGHTGRAY);
            styleTimerLabel(whiteTimerLabel);

            timersBox.getChildren().addAll(blackLabel, blackTimerLabel, new Label(""), whiteLabel, whiteTimerLabel);
            panel.getChildren().add(timersBox);
        }

        Region spacer = new Region();
        if (isTimed) {
            spacer.setPrefHeight(80);
        } else {
            spacer.setPrefHeight(60);
        }
        panel.getChildren().add(spacer);

        VBox buttonsBox = new VBox(15);
        buttonsBox.setAlignment(Pos.CENTER);

        Button undoBtn = createSideButton("Undo");
        undoBtn.setOnAction(e -> chessBoard.undoMove());

        Button restartBtn = createSideButton("Restart");
        restartBtn.setOnAction(e -> {
            chessBoard.restartGame();
            if (isTimed) {
                if (timeline != null) timeline.stop();
                whiteSeconds = initialSeconds;
                blackSeconds = initialSeconds;
                whiteTimerLabel.setText(formatTime(whiteSeconds));
                blackTimerLabel.setText(formatTime(blackSeconds));
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

        ScrollPane scrollPane = new ScrollPane(moveHistoryBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300);
        scrollPane.setStyle(
                "-fx-background: #2b2b2b;" +
                        "-fx-background-color: #2b2b2b;"
        );
        moveHistoryBox.setStyle(
                "-fx-background-color: #3c3f41;" +
                        "-fx-padding: 10;" +
                        "-fx-background-radius: 10;"
        );

        panel.getChildren().addAll(
                historyTitle,
                scrollPane
        );

        panel.getChildren().add(
                createReviewPanel()
        );

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