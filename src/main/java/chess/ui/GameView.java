package chess.ui;

import chess.GameSettings;
import chess.bot.ChessBot;
import chess.bot.MinimaxBot;
import chess.bot.RandomBot;
import chess.model.MaterialCalculator;
import chess.model.PieceType;
import chess.model.PlayerColor;
import java.util.List;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.scene.control.*;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
    private ScrollPane historyScrollPane;

    private final HBox whiteGraveyard = new HBox(2);
    private final HBox blackGraveyard = new HBox(2);
    private final Label whiteAdvantage = new Label();
    private final Label blackAdvantage = new Label();
    private final Label botThoughtsLabel = new Label("Waiting for your move...");

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
        chessBoard.setOnBotThought(thought -> {
            botThoughtsLabel.setText("\"" + thought + "\"");
        });
        chessBoard.setOnMovePlayed(moveText -> {

            Label moveLabel = new Label(moveText);

            moveLabel.setTextFill(Color.WHITE);

            moveHistoryBox.getChildren().add(moveLabel);
            updateTurnLabel();
            updateMaterial();
        });
        chessBoard.setOnGameOver(winner -> {

            if (timeline != null) {
                timeline.stop();
            }

            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(e -> showGameOverOverlay(winner));
            pause.play();
        });

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

            historyScrollPane.setVisible(true);
            historyScrollPane.setManaged(true);

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

                        () -> {
                            if (GameSettings.isNetworkGame) {
                                LobbyMenu lobby = LobbyMenu.getInstance();
                                if (lobby != null) {
                                    lobby.refresh();
                                    root.getChildren().setAll(lobby);
                                } else {
                                    root.getChildren().setAll(new MainMenu(root));
                                }
                            } else {
                                root.getChildren().setAll(new GameView(root, isTimed, initialSeconds / 60));
                            }
                        },

                        () -> {
                            chessBoard.setEffect(null);
                            reviewIndex = 0;
                            chessBoard.showPosition(reviewIndex);
                            updateReviewLabel();
                            moveHistoryBox.setVisible(false);
                            moveHistoryBox.setManaged(false);
                            historyTitle.setVisible(false);
                            historyTitle.setManaged(false);
                            historyScrollPane.setVisible(false);
                            historyScrollPane.setManaged(false);
                            reviewBox.setVisible(true);
                            reviewBox.setManaged(true);
                            root.getChildren().removeIf(node -> node instanceof GameOverOverlay);},

                        () -> {
                            chessBoard.stopNetworking();
                            root.getChildren().setAll(new MainMenu(root));
                        });

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

            turnLabel.setText("⚫ Black to move");

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
        String winner = (whiteSeconds <= 0) ? "Black" : "White";

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Time Over");
        alert.setHeaderText(null);
        alert.setContentText("Game over! Winner: " + winner);
        alert.show();
    }

    private VBox createSidePanel() {
        VBox panel = new VBox();
        turnLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setPrefWidth(220);

        // --- Bot Thought Box ---
        if (GameSettings.isBotGame && !GameSettings.isNetworkGame) {
            VBox thoughtBox = new VBox(5);
            thoughtBox.setAlignment(Pos.CENTER);
            thoughtBox.setStyle(
                "-fx-background-color: #3c3f41; " +
                "-fx-padding: 15; " +
                "-fx-background-radius: 15 15 15 0; " +
                "-fx-border-color: #769656; " +
                "-fx-border-width: 2;"
            );
            VBox.setMargin(thoughtBox, new Insets(0, 0, 20, 0));

            Label botName = new Label("ChessBot's Brain:");
            botName.setTextFill(Color.web("#769656"));
            botName.setFont(Font.font("Arial", FontWeight.BOLD, 12));

            botThoughtsLabel.setTextFill(Color.WHITE);
            botThoughtsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            botThoughtsLabel.setWrapText(true);
            botThoughtsLabel.setMaxWidth(200);
            botThoughtsLabel.setMinHeight(Region.USE_PREF_SIZE);

            thoughtBox.getChildren().addAll(botName, botThoughtsLabel);
            panel.getChildren().add(thoughtBox);
        }
        // ----------------------------

        historyTitle = new Label("Move History");
        historyTitle.setTextFill(Color.WHITE);
        historyTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        moveHistoryBox.setAlignment(Pos.TOP_LEFT);

        whiteAdvantage.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        whiteAdvantage.setTextFill(Color.web("#a3a3a3"));
        blackAdvantage.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        blackAdvantage.setTextFill(Color.web("#a3a3a3"));

        VBox blackProfile = new VBox(5);
        blackProfile.setAlignment(Pos.CENTER);
        blackProfile.setPadding(new Insets(20, 0, 10, 0));

        Label blackLabel = new Label("BLACK");
        blackLabel.setTextFill(Color.LIGHTGRAY);

        HBox blackGraveyardContainer = new HBox(5, blackGraveyard, blackAdvantage);
        blackGraveyardContainer.setAlignment(Pos.CENTER);
        blackGraveyardContainer.setMinHeight(25);

        blackProfile.getChildren().addAll(blackLabel, blackGraveyardContainer);
        if (isTimed) {
            styleTimerLabel(blackTimerLabel);
            blackProfile.getChildren().add(blackTimerLabel);
        }

        VBox whiteProfile = new VBox(5);
        whiteProfile.setAlignment(Pos.CENTER);
        whiteProfile.setPadding(new Insets(10, 0, 10, 0));

        Label whiteLabel = new Label("WHITE");
        whiteLabel.setTextFill(Color.LIGHTGRAY);

        HBox whiteGraveyardContainer = new HBox(5, whiteGraveyard, whiteAdvantage);
        whiteGraveyardContainer.setAlignment(Pos.CENTER);
        whiteGraveyardContainer.setMinHeight(25);

        whiteProfile.getChildren().addAll(whiteLabel, whiteGraveyardContainer);
        if (isTimed) {
            styleTimerLabel(whiteTimerLabel);
            whiteProfile.getChildren().add(whiteTimerLabel);
        }

        panel.getChildren().addAll(blackProfile, whiteProfile);

        Region historySpacer = new Region();
        Region spacer = new Region();
        if (isTimed) {
            spacer.setPrefHeight(80);
            historySpacer.setPrefHeight(60);
        } else {
            spacer.setPrefHeight(60);
            historySpacer.setPrefHeight(40);
        }
        panel.getChildren().add(spacer);

        VBox buttonsBox = new VBox(15);
        buttonsBox.setAlignment(Pos.CENTER);

        Button undoBtn = createSideButton("Undo");
        undoBtn.setDisable(GameSettings.isNetworkGame);
        undoBtn.setOnAction(e ->
        {
            updateMaterial();
            chessBoard.undoMove();
        });

        Button restartBtn = createSideButton("Restart");
        restartBtn.setDisable(GameSettings.isNetworkGame);
        restartBtn.setOnAction(e -> {
            moveHistoryBox.getChildren().clear();
            chessBoard.restartGame();
            updateMaterial();
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
            if (GameSettings.isNetworkGame) {
                chessBoard.resign();
            } else {
                if (timeline != null) {
                    timeline.stop();
                }

                chessBoard.setDisable(true);

                PlayerColor activeColor = chessBoard.getBoardState().getActiveColor();
                String winnerName;

                if (GameSettings.isBotGame) {
                    winnerName = (activeColor == PlayerColor.WHITE) ? "Black (Bot)" : "White (You)";
                } else {
                    winnerName = (activeColor == PlayerColor.WHITE) ? "Black" : "White";
                }
                showGameOverOverlay(winnerName);
            }
        });

        buttonsBox.getChildren().addAll(undoBtn, restartBtn, resignBtn);
        panel.getChildren().add(buttonsBox);

        panel.getChildren().add(historySpacer);

        this.historyScrollPane = new ScrollPane(moveHistoryBox);
        historyScrollPane.setFitToWidth(true);
        historyScrollPane.setPrefHeight(160);
        historyScrollPane.setStyle("-fx-background: #2b2b2b; -fx-background-color: #2b2b2b;");

        moveHistoryBox.setStyle(
                "-fx-background-color: #3c3f41;" +
                        "-fx-padding: 10;" +
                        "-fx-background-radius: 10;"
        );

        panel.getChildren().addAll(historyTitle, historyScrollPane);

        panel.getChildren().add(createReviewPanel());

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

    public void updateMaterial() {
        blackGraveyard.getChildren().clear();
        whiteGraveyard.getChildren().clear();
        List<PieceType> deadBlackPieces = MaterialCalculator.getCapturedPieces(chessBoard.getBoardState(), PlayerColor.BLACK);
        List<PieceType> deadWhitePieces = MaterialCalculator.getCapturedPieces(chessBoard.getBoardState(), PlayerColor.WHITE);
        deadBlackPieces.sort((p1, p2) -> Integer.compare(p2.getValue(), p1.getValue()));
        deadWhitePieces.sort((p1, p2) -> Integer.compare(p2.getValue(), p1.getValue()));

        for (PieceType type : deadBlackPieces) {
            ImageView img = new ImageView(getMiniImage(type, PlayerColor.BLACK));
            img.setFitWidth(20);
            img.setPreserveRatio(true);
            whiteGraveyard.getChildren().add(img);
        }
        for (PieceType type : deadWhitePieces) {
            ImageView img = new ImageView(getMiniImage(type, PlayerColor.WHITE));
            img.setFitWidth(20);
            img.setPreserveRatio(true);
            blackGraveyard.getChildren().add(img);
        }
        int advantage = chess.model.MaterialCalculator.getMaterialAdvantage(chessBoard.getBoardState());
        whiteAdvantage.setText(advantage > 0 ? "+" + advantage : "");
        blackAdvantage.setText(advantage < 0 ? "+" + Math.abs(advantage) : "");
    }

    private Image getMiniImage(PieceType type, PlayerColor color) {
        String colorStr = color == PlayerColor.WHITE ? "white" : "black";
        String pieceStr = type.toString().toLowerCase();
        if (pieceStr.equals("bishop")) pieceStr = "officer";
        if (pieceStr.equals("knight")) pieceStr = "horse";

        return new Image(getClass().getResourceAsStream("/pieces/" + colorStr + " " + pieceStr + ".png"));
    }

    public void setNetworkGame(String gameId, PlayerColor assignedColor, String opponentName) {
        GameSettings.isNetworkGame = true;
        GameSettings.isBotGame = false;
        GameSettings.playerColor = assignedColor;
        this.chessBoard.setNetworkGame(gameId, assignedColor);
    }
}

