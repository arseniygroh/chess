package chess.ui;

import chess.bot.ChessBot;
import chess.model.*;
import chess.model.pieces.Piece;
import chess.network.protocol.Packet;
import javafx.application.Platform;
import javafx.scene.SnapshotParameters;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.effect.DropShadow;
import javafx.scene.control.Label;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.geometry.Pos;
import javafx.scene.media.AudioClip;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.text.FontWeight;
import chess.GameSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.Consumer;

public class ChessBoard extends GridPane {
    private static final int BOARD_SIZE = 8;
    private final StackPane[][] tiles = new StackPane[8][8];
    private int selectedRow = -1;
    private int selectedCol = -1;
    private Consumer<String> onMovePlayed;
    private Runnable onTurnEnd;
    private Consumer<String> onGameOver;
    private Position selectedPosition = null;
    private BoardState boardState = new BoardState();
    private final ChessBot bot;
    private final AudioClip moveSound =
            new AudioClip(
                    getClass()
                            .getResource("/sounds/move.mp3")
                            .toExternalForm()
            );
    private final AudioClip checkSound =
            new AudioClip(
                    getClass()
                            .getResource("/sounds/check.mp3")
                            .toExternalForm()
            );
    private final AudioClip captureSound =
            new AudioClip(
                    getClass()
                            .getResource("/sounds/take figure.mp3")
                            .toExternalForm()
            );

    private final AudioClip castleSound =
            new AudioClip(
                    getClass()
                            .getResource("/sounds/castling.mp3")
                            .toExternalForm()
            );

    private final AudioClip gameOverSound =
            new AudioClip(
                    getClass()
                            .getResource("/sounds/mate.mp3")
                            .toExternalForm()
            );
    private final Image whitePawn =
            new Image(getClass().getResourceAsStream("/pieces/alpha/wP.png"));
    private final Image blackPawn =
            new Image(getClass().getResourceAsStream("/pieces/alpha/bP.png"));
    private final Image whiteRook =
            new Image(getClass().getResourceAsStream("/pieces/alpha/wR.png"));
    private final Image blackRook =
            new Image(getClass().getResourceAsStream("/pieces/alpha/bR.png"));
    private final Image whiteQueen =
            new Image(getClass().getResourceAsStream("/pieces/alpha/wQ.png"));
    private final Image blackQueen =
            new Image(getClass().getResourceAsStream("/pieces/alpha/bQ.png"));
    private final Image whiteKing =
            new Image(getClass().getResourceAsStream("/pieces/alpha/wK.png"));
    private final Image blackKing =
            new Image(getClass().getResourceAsStream("/pieces/alpha/bK.png"));
    private final Image whiteOfficer =
            new Image(getClass().getResourceAsStream("/pieces/alpha/wB.png"));
    private final Image blackOfficer =
            new Image(getClass().getResourceAsStream("/pieces/alpha/bB.png"));
    private final Image whiteHorse =
            new Image(getClass().getResourceAsStream("/pieces/alpha/wN.png"));
    private final Image blackHorse =
            new Image(getClass().getResourceAsStream("/pieces/alpha/bN.png"));
    private final Stack<BoardState> history = new Stack<>();
    private boolean gameStarted = false;
    private Runnable onFirstAction;
    private final List<BoardState> boardHistory = new ArrayList<>();
    private int currentHistoryIndex = 0;
    private boolean flipped = false;
    private String networkGameId = null;
    private Consumer<Packet> networkListener = null;
    private Consumer<String> onBotThought;

    public void setOnBotThought(Consumer<String> onBotThought) {
        this.onBotThought = onBotThought;
    }

    public void setNetworkGame(String gameId, PlayerColor color) {
        this.networkGameId = gameId;
        if (color == PlayerColor.BLACK) {
            flipped = true;
        }
        renderBoard();
        networkListener = packet -> {
            if (packet instanceof chess.network.protocol.GameStateUpdate update) {
                if (update.gameId().equals(networkGameId)) {
                    syncWithServer(update);
                }
            }
        };
        chess.network.client.ClientConnection.getInstance().addListener(networkListener);
    }

    public void stopNetworking() {
        if (networkListener != null) {
            chess.network.client.ClientConnection.getInstance().removeListener(networkListener);
            networkListener = null;
        }
    }

    public String getNetworkGameId() {
        return networkGameId;
    }

    public void resign() {
        if (GameSettings.isNetworkGame && networkGameId != null) {
            chess.network.client.ClientConnection.getInstance().sendPacket(
                    new chess.network.protocol.ResignRequest(networkGameId)
            );
        }
    }

    public synchronized void syncWithServer(chess.network.protocol.GameStateUpdate update) {
        this.boardState = update.boardState();
        boardHistory.add(boardState.copy());
        currentHistoryIndex = boardHistory.size() - 1;

        if (update.lastMove() != null) {
            String moveText = toChessNotation(update.lastMove().start()) + "-" + toChessNotation(update.lastMove().end());
            PlayerColor movingColor = boardState.getActiveColor() == PlayerColor.WHITE ? PlayerColor.BLACK : PlayerColor.WHITE;
            if (onMovePlayed != null) {
                onMovePlayed.accept((movingColor == PlayerColor.BLACK ? "⚪ " : "⚫ ") + moveText);
            }
            // Play sound based on move type
            moveSound.play();
        }

        renderBoard();
        if (onTurnEnd != null) onTurnEnd.run();

        if (update.isGameOver()) {
            this.setDisable(true);
            gameOverSound.play();
            if (onGameOver != null) {
                onGameOver.accept(update.winner());
            }
        }
    }

    public ChessBoard(ChessBot bot) {
        this.bot = bot;
        this.setMaxSize(600, 600);
        this.setAlignment(Pos.CENTER);
        boardHistory.add(boardState.copy());
        createBoard();
        cellsLetters();
        cellsNumbers();
        renderBoard();
    }

    public void setOnGameOver(
            Consumer<String> onGameOver
    ) {
        this.onGameOver = onGameOver;
    }

    public void setOnTurnEnd(Runnable onTurnEnd) {
        this.onTurnEnd = onTurnEnd;
    }

    private String toChessNotation(
            Position pos
    ) {
        char file = (char) ('a' + pos.col());

        int rank = 8 - pos.row();

        return "" + file + rank;
    }

    private Image getImage(Piece piece) {
        switch (piece.getType()) {
            case PAWN:
                return piece.getColor() == PlayerColor.WHITE
                        ? whitePawn
                        : blackPawn;
            case ROOK:
                return piece.getColor() == PlayerColor.WHITE
                        ? whiteRook
                        : blackRook;
            case KING:
                return piece.getColor() == PlayerColor.WHITE
                        ? whiteKing
                        : blackKing;
            case QUEEN:
                return piece.getColor() == PlayerColor.WHITE
                        ? whiteQueen
                        : blackQueen;
            case KNIGHT:
                return piece.getColor() == PlayerColor.WHITE
                        ? whiteHorse
                        : blackHorse;
            case BISHOP:
                return piece.getColor() == PlayerColor.WHITE
                        ? whiteOfficer
                        : blackOfficer;
        }
        throw new IllegalStateException();
    }

    public void setOnMovePlayed(
            Consumer<String> onMovePlayed
    ) {
        this.onMovePlayed = onMovePlayed;
    }

    private void cellsNumbers() {
        String numbers = "87654321";
        for (int i = 0; i < 8; i++) {
            Label leftNum = createCoordLabel(String.valueOf(numbers.charAt(i)));
            Label rightNum = createCoordLabel(String.valueOf(numbers.charAt(i)));

            add(leftNum, 0, i + 1);
            add(rightNum, 9, i + 1);
        }
    }


    private Label createCoordLabel(String text) {
        Label label = new Label(text.toUpperCase());
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        label.setTextFill(Color.rgb(170, 162, 153));
        label.setAlignment(Pos.CENTER);
        label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        return label;
    }

    private void createBoard() {
        this.getColumnConstraints().clear();
        this.getRowConstraints().clear();

        for (int i = 0; i < 10; i++) {
            ColumnConstraints column = new ColumnConstraints();
            if (i == 0 || i == 9) {
                column.setPrefWidth(45);
            } else {
                column.setPercentWidth(100.0 / 8);
            }
            getColumnConstraints().add(column);
        }

        for (int i = 0; i < 10; i++) {
            RowConstraints row = new RowConstraints();
            if (i == 0 || i == 9) {
                row.setPrefHeight(45);
            } else {
                row.setPercentHeight(100.0 / 8);
            }
            getRowConstraints().add(row);
        }

        this.setStyle(
                "-fx-background-color: #262421; " +
                        "-fx-padding: 42; " +
                        "-fx-border-color: #3c3934; " +
                        "-fx-border-width: 4; " +
                        "-fx-border-radius: 8; " +
                        "-fx-background-radius: 12;"
        );

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                StackPane tile = new StackPane();
                tiles[row][col] = tile;
                tile.setMinSize(0, 0);

                Color color = ((row + col) % 2 == 0) ? Color.rgb(238, 238, 210) : Color.rgb(118, 150, 86);
                Color hoverColor = ((row + col) % 2 == 0) ? Color.rgb(244, 244, 220) : Color.rgb(128, 160, 96);

                tile.setBackground(new Background(new BackgroundFill(color, null, null)));

                tile.setOnMouseEntered(e -> tile.setBackground(new Background(new BackgroundFill(hoverColor, null, null))));
                tile.setOnMouseExited(e -> tile.setBackground(new Background(new BackgroundFill(color, null, null))));

                final int currentRow = row;
                final int currentCol = col;

                tile.setOnDragDetected(event -> {
                    int boardRow = flipped ? 7 - currentRow : currentRow;
                    int boardCol = flipped ? 7 - currentCol : currentCol;
                    Piece piece = boardState.getPieceAt(new Position(boardRow, boardCol));

                    if (piece != null && piece.getColor() == boardState.getActiveColor()) {
                        if (!gameStarted) {
                            gameStarted = true;
                            if (onFirstAction != null) onFirstAction.run();
                        }

                        handleClick(currentRow, currentCol);
                        Dragboard db = tile.startDragAndDrop(TransferMode.MOVE);
                        ClipboardContent content = new ClipboardContent();
                        content.putString(currentRow + "," + currentCol);
                        db.setContent(content);

                        Image rawImage = getImage(piece);
                        ImageView dragIcon = new ImageView(rawImage);
                        double size = piece.getType() == PieceType.PAWN ? 75 : 100;
                        dragIcon.setFitWidth(size);
                        dragIcon.setPreserveRatio(true);

                        SnapshotParameters params = new SnapshotParameters();
                        params.setFill(Color.TRANSPARENT);
                        Image dragSnapshot = dragIcon.snapshot(params, null);
                        db.setDragView(dragSnapshot, dragSnapshot.getWidth() / 2, dragSnapshot.getHeight() / 2);
                        event.consume();
                    }
                });

                tile.setOnDragOver(event -> {
                    if (event.getGestureSource() != tile && event.getDragboard().hasString()) {
                        event.acceptTransferModes(TransferMode.MOVE);
                    }
                    event.consume();
                });

                tile.setOnDragDropped(event -> {
                    Dragboard db = event.getDragboard();
                    if (db.hasString()) {
                        handleClick(currentRow, currentCol);
                        event.setDropCompleted(true);
                    }
                    event.consume();
                });

                tile.setOnDragDone(event -> {
                    clearHighlights();
                    event.consume();
                });

                tile.setOnMouseClicked(event -> {
                    handleClick(currentRow, currentCol);
                });

                add(tile, col + 1, row + 1);
            }
        }

        Region innerFrame = new Region();
        innerFrame.setMouseTransparent(true);
        innerFrame.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        innerFrame.setStyle(
                "-fx-border-color: #4a4743; " +
                        "-fx-border-width: 3; " +
                        "-fx-border-style: solid;" +
                        "-fx-border-insets: -3;"
        );
        this.add(innerFrame, 1, 1, 8, 8);
        innerFrame.toFront();

    }

    private void highlightAvailableMoves(Position pos) {
        clearHighlights();
        highlightSelectedTile();
        List<Move> legalMoves = RulesEngine.getStrictlyLegalMovesForPiece(boardState, pos);
        for (Move move : legalMoves) {
            int r = flipped
                    ? 7 - move.end().row()
                    : move.end().row();

            int c = flipped
                    ? 7 - move.end().col()
                    : move.end().col();

            StackPane tile = tiles[r][c];
            boolean isCapture = boardState.getPieceAt(move.end()) != null;
            Circle marker = new Circle();
            marker.setMouseTransparent(true);

            if (isCapture) {
                marker.setRadius(24);
                marker.setFill(Color.TRANSPARENT);
                marker.setStroke(Color.rgb(0, 0, 0, 0.2));
                marker.setStrokeWidth(6);
            } else {
                marker.setRadius(12);
                marker.setFill(Color.rgb(0, 0, 0, 0.2));
            }
            tile.getChildren().add(marker);
            marker.toFront();
        }
    }

    private void attemptMove(Move move) {
        if (RulesEngine.isLegalMove(boardState, move)) {
            history.push(boardState.copy());
            Piece targetPiece = boardState.getPieceAt(move.end());
            Piece movingPiece = boardState.getPieceAt(move.start());

            boolean isCastle =
                    movingPiece.getType() == PieceType.KING
                            && Math.abs(
                            move.end().col()
                                    - move.start().col()
                    ) == 2;
            PlayerColor movingColor =
                    boardState.getActiveColor();
            boardState.executeMove(move);

            if (!GameSettings.isBotGame) {
                flipped = !flipped;
            }
            boardHistory.add(boardState.copy());
            currentHistoryIndex = boardHistory.size() - 1;
            if (onMovePlayed != null) {

                String moveText =
                        toChessNotation(move.start())
                                + "-"
                                + toChessNotation(move.end());

                onMovePlayed.accept(
                        (movingColor == PlayerColor.BLACK
                                ? "⚪ "
                                : "⚫ ")
                                + moveText
                );
            }

            PlayerColor enemyColor =
                    boardState.getActiveColor();

            boolean isCheck =
                    RulesEngine.isKingInCheck(
                            boardState,
                            enemyColor
                    );

            if (RulesEngine.isCheckMate(boardState)) {

                this.setDisable(true);

                gameOverSound.play();

                if (onGameOver != null) {

                    onGameOver.accept("White");
                }
            } else if (isCheck) {
                checkSound.play();
            } else if (isCastle) {
                castleSound.play();
            } else if (targetPiece != null) {
                captureSound.play();
            } else {
                moveSound.play();
            }
            selectedRow = -1;
            selectedCol = -1;
            clearHighlights();
            renderBoard();

            if (onTurnEnd != null) onTurnEnd.run();

            if (
                    GameSettings.isBotGame
                            &&
                            boardState.getActiveColor() == PlayerColor.BLACK
            ) {
                new Thread(() -> {
                    Move botMove = bot.calculateMove(boardState);
                    Platform.runLater(() -> {
                        if (botMove != null) {

                            Piece targetPieceBot =
                                    boardState.getPieceAt(botMove.end());

                            Piece movingPieceBot =
                                    boardState.getPieceAt(botMove.start());

                            boolean isCastleBot =
                                    movingPieceBot != null
                                            && movingPieceBot.getType() == PieceType.KING
                                            && Math.abs(
                                            botMove.end().col()
                                                    - botMove.start().col()
                                    ) == 2;
                            PlayerColor movingColorBot =
                                    boardState.getActiveColor();
                            boardState.executeMove(botMove);
                            boardHistory.add(boardState.copy());
                            currentHistoryIndex = boardHistory.size() - 1;
                            if (onMovePlayed != null) {

                                String moveText =
                                        toChessNotation(botMove.start())
                                                + "-"
                                                + toChessNotation(botMove.end());

                                onMovePlayed.accept(
                                        (movingColorBot == PlayerColor.BLACK
                                                ? "⚪ "
                                                : "⚫ ")
                                                + moveText
                                );
                            }

                            PlayerColor enemyColorBot =
                                    boardState.getActiveColor();

                            boolean isCheckBot =
                                    RulesEngine.isKingInCheck(
                                            boardState,
                                            enemyColorBot
                                    );

                            if (RulesEngine.isCheckMate(boardState)) {

                                this.setDisable(true);

                                gameOverSound.play();

                                if (onGameOver != null) {

                                    onGameOver.accept("Black");
                                }
                            } else if (isCheckBot) {
                                checkSound.play();
                            } else if (isCastleBot) {
                                castleSound.play();
                            } else if (targetPieceBot != null) {
                                captureSound.play();
                            } else {
                                moveSound.play();
                            }

                            if (onBotThought != null) {
                                int score = chess.bot.Evaluator.evaluate(boardState);
                                String phrase = chess.bot.TrashTalker.getPhrase(score, movingColorBot == PlayerColor.WHITE);
                                onBotThought.accept(phrase);
                            }

                            renderBoard();
                            if (onTurnEnd != null) onTurnEnd.run();
                            System.out.println("Бот зробив хід: " + botMove.start() + " -> " + botMove.end());
                        } else {
                            System.out.println("Боту нікуди ходити (можливо, Мат або Пат)");
                        }
                    });
                }).start();
            }
        } else {
            System.out.println("Нелегальний хід");

        }
    }

    public int getHistorySize() {
        return boardHistory.size();
    }

    public int getCurrentHistoryIndex() {
        return currentHistoryIndex;
    }

    public void nextPosition() {
        showPosition(currentHistoryIndex + 1);
    }

    public void previousPosition() {
        showPosition(currentHistoryIndex - 1);
    }

    public void flipBoard() {
        flipped = !flipped;
        renderBoard();
    }

    public void showPosition(int index) {

        if (index < 0 || index >= boardHistory.size()) {
            return;
        }

        currentHistoryIndex = index;

        boardState = boardHistory.get(index).copy();
        this.setDisable(false);
        renderBoard();
    }

    private void handleClick(int row, int col) {
        int boardRow =
                flipped ? 7 - row : row;

        int boardCol =
                flipped ? 7 - col : col;

        Position clickedPosition =
                new Position(boardRow, boardCol);
        if (selectedPosition == null) {
            Piece piece = boardState.getPieceAt(clickedPosition);
            if (piece == null) {
                return;
            }
            if (piece.getColor() != boardState.getActiveColor()) {
                return;
            }

            if (GameSettings.isNetworkGame && piece.getColor() != GameSettings.playerColor) {
                return;
            }

            if (!gameStarted) {
                gameStarted = true;
                if (onFirstAction != null) {
                    onFirstAction.run();
                }
            }
            selectedPosition = clickedPosition;
            selectedRow = row;
            selectedCol = col;
            highlightSelectedTile();
            highlightAvailableMoves(selectedPosition);
            return;
        }

        Piece clickedPiece = boardState.getPieceAt(clickedPosition);
        if (selectedPosition != null &&
                clickedPiece != null &&
                clickedPiece.getColor() == boardState.getActiveColor()) {

            selectedPosition = clickedPosition;
            selectedRow = row;
            selectedCol = col;

            highlightSelectedTile();
            highlightAvailableMoves(selectedPosition);
            return;
        }
        Piece movingPiece = boardState.getPieceAt(selectedPosition);
        if (movingPiece == null) {
            selectedPosition = null;
            return;
        }
        boolean isPromotion =
                movingPiece.getType() == PieceType.PAWN &&
                        (clickedPosition.row() == 0 || clickedPosition.row() == 7);

        if (isPromotion) {
            Move testMove = new Move(selectedPosition, clickedPosition, PieceType.QUEEN);
            if (RulesEngine.isLegalMove(boardState, testMove)) {
                showPromotionMenu(selectedPosition, clickedPosition, movingPiece.getColor());
                return;
            }
        }

        Move move = new Move(
                selectedPosition,
                clickedPosition,
                null
        );
        if (GameSettings.isNetworkGame) {
            chess.network.client.ClientConnection.getInstance().sendPacket(
                    new chess.network.protocol.MoveRequest(networkGameId, move)
            );
        } else {
            attemptMove(move);
        }
        selectedPosition = null;
    }


    private void showPromotionMenu(Position start, Position end, PlayerColor color) {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6);");
        HBox menuBox = new HBox(15);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        menuBox.setStyle(
                "-fx-background-color: #f3f3f3; " +
                        "-fx-padding: 20; " +
                        "-fx-background-radius: 10; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 15, 0, 0, 5);"
        );

        PieceType[] options = {PieceType.QUEEN, PieceType.KNIGHT, PieceType.ROOK, PieceType.BISHOP};

        for (PieceType type : options) {
            Image pieceImage = getPromotionImage(type, color);
            ImageView pieceView = createPiece(pieceImage, 80);
            pieceView.setMouseTransparent(false);
            pieceView.setOnMouseEntered(e -> pieceView.setStyle("-fx-cursor: hand; -fx-scale-x: 1.1; -fx-scale-y: 1.1;"));
            pieceView.setOnMouseExited(e -> pieceView.setStyle("-fx-cursor: default; -fx-scale-x: 1.0; -fx-scale-y: 1.0;"));

            pieceView.setOnMouseClicked(e -> {
                this.getChildren().remove(overlay);
                Move finalMove = new Move(start, end, type);
                if (GameSettings.isNetworkGame) {
                    chess.network.client.ClientConnection.getInstance().sendPacket(
                            new chess.network.protocol.MoveRequest(networkGameId, finalMove)
                    );
                } else {
                    attemptMove(finalMove);
                }
                selectedPosition = null;
            });

            menuBox.getChildren().add(pieceView);
        }

        overlay.getChildren().add(menuBox);
        this.add(overlay, 0, 0, 10, 10);
        overlay.toFront();
    }

    private Image getPromotionImage(PieceType type, PlayerColor color) {
        boolean isWhite = (color == PlayerColor.WHITE);
        return switch (type) {
            case QUEEN -> isWhite ? whiteQueen : blackQueen;
            case ROOK -> isWhite ? whiteRook : blackRook;
            case BISHOP -> isWhite ? whiteOfficer : blackOfficer;
            case KNIGHT -> isWhite ? whiteHorse : blackHorse;
            default -> null;
        };
    }

    private void highlightSelectedTile() {

        clearHighlights();

        if (selectedRow == -1 || selectedCol == -1) {
            return;
        }

        tiles[selectedRow][selectedCol].setBackground(
                new Background(
                        new BackgroundFill(
                                Color.rgb(246, 246, 105),
                                null,
                                null
                        )
                )
        );
    }

    private void clearHighlights() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Color color;
                if ((row + col) % 2 == 0) {
                    color = Color.rgb(238, 238, 210);
                } else {
                    color = Color.rgb(118, 150, 86);
                }
                tiles[row][col].setBackground(
                        new Background(
                                new BackgroundFill(
                                        color,
                                        null,
                                        null
                                )
                        )
                );
                tiles[row][col].getChildren().removeIf(node -> node instanceof Circle);
            }
        }
    }

    private void renderBoard() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                tiles[row][col].getChildren().clear();
            }
        }

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int boardRow =
                        flipped ? 7 - row : row;

                int boardCol =
                        flipped ? 7 - col : col;

                Position position =
                        new Position(boardRow, boardCol);

                Piece piece =
                        boardState.getPieceAt(position);
                if (piece != null) {
                    Image image = getImage(piece);
                    double size = 55;
                    tiles[row][col].getChildren().add(createPiece(image, size));
                }
            }
        }
    }

    private ImageView createPiece(Image image, double width) {
        ImageView piece = new ImageView(image);
        piece.setPreserveRatio(true);
        piece.setMouseTransparent(true);
        piece.setFitWidth(width);
        piece.setSmooth(true);
        DropShadow outline = new DropShadow();
        outline.setRadius(6);
        outline.setSpread(0.2);
        outline.setColor(Color.rgb(0, 0, 0, 0.6));
        piece.setEffect(outline);
        return piece;
    }

    private void cellsLetters() {
        String letters = "abcdefgh";
        for (int i = 0; i < 8; i++) {
            Label topLet = createCoordLabel(String.valueOf(letters.charAt(i)));
            Label botLet = createCoordLabel(String.valueOf(letters.charAt(i)));
            add(topLet, i + 1, 0);
            add(botLet, i + 1, 9);
        }
    }

    public void restartGame() {
        this.setDisable(false);
        boardState = new BoardState();
        flipped = false;
        boardHistory.clear();
        boardHistory.add(boardState.copy());
        currentHistoryIndex = 0;
        history.clear();
        selectedPosition = null;
        selectedRow = -1;
        selectedCol = -1;
        gameStarted = false;
        this.setDisable(false);
        renderBoard();
    }

    public void undoMove() {
        if (!history.isEmpty()) {
            BoardState previousState = history.pop();
            boardState = previousState.copy();
            selectedPosition = null;
            selectedRow = -1;
            selectedCol = -1;
            this.setDisable(false);
            renderBoard();
        }
    }

    public void setOnFirstAction(Runnable onFirstAction) {
        this.onFirstAction = onFirstAction;
    }

    public BoardState getBoardState() {
        return boardState;
    }
}
