package chess.ui;

import chess.bot.ChessBot;
import chess.model.*;
import chess.model.pieces.Piece;
import javafx.application.Platform;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.effect.DropShadow;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.geometry.Pos;
import java.util.Stack;

public class ChessBoard extends GridPane {
    private static final int BOARD_SIZE = 8;
    private final StackPane[][] tiles = new StackPane[8][8];
    private int selectedRow = -1;
    private int selectedCol = -1;
    private Position selectedPosition = null;
    private BoardState boardState = new BoardState();
    private final ChessBot bot = new chess.bot.MinimaxBot(3);
    private final Image whitePawn =
            new Image(getClass().getResourceAsStream("/pieces/white pawn.png"));
    private final Image blackPawn =
            new Image(getClass().getResourceAsStream("/pieces/black pawn.png"));
    private final Image whiteRook =
            new Image(getClass().getResourceAsStream("/pieces/white rook.png"));
    private final Image blackRook =
            new Image(getClass().getResourceAsStream("/pieces/black rook.png"));
    private final Image whiteQueen =
            new Image(getClass().getResourceAsStream("/pieces/white queen.png"));
    private final Image blackQueen =
            new Image(getClass().getResourceAsStream("/pieces/black queen.png"));
    private final Image whiteKing =
            new Image(getClass().getResourceAsStream("/pieces/white king.png"));
    private final Image blackKing =
            new Image(getClass().getResourceAsStream("/pieces/black king.png"));
    private final Image whiteOfficer =
            new Image(getClass().getResourceAsStream("/pieces/white officer.png"));
    private final Image blackOfficer =
            new Image(getClass().getResourceAsStream("/pieces/black officer.png"));
    private final Image whiteHorse =
            new Image(getClass().getResourceAsStream("/pieces/white horse.png"));
    private final Image blackHorse =
            new Image(getClass().getResourceAsStream("/pieces/black horse.png"));
    private final Stack<BoardState> history = new Stack<>();
    private Runnable onTurnEnd;
    private boolean gameStarted = false;
    private Runnable onFirstAction;

    public ChessBoard() {
        this.setMaxSize(600, 600);
        createBoard();
        renderBoard();
    }

    public void setOnTurnEnd(Runnable onTurnEnd) {
        this.onTurnEnd = onTurnEnd;
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

    private void cellsNumbers() {
        String numbers = "87654321";

        for (int row = 0; row < 8; row++) {

            Label letter = new Label(
                    String.valueOf(numbers.charAt(row))
            );
            letter.setFont(Font.font(12));

            if (row % 2 == 0) {
                letter.setTextFill(
                        Color.rgb(118, 150, 86)
                );
            } else {
                letter.setTextFill(
                        Color.rgb(238, 238, 210)
                );
            }

            StackPane.setAlignment(letter, Pos.TOP_LEFT);
            letter.setTranslateX(4);
            letter.setTranslateY(2);
            tiles[row][0].getChildren().add(letter);
            letter.toFront();
        }
    }

    private void createBoard() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(100.0 / BOARD_SIZE);
            getColumnConstraints().add(column);
            RowConstraints row = new RowConstraints();
            row.setPercentHeight(100.0 / BOARD_SIZE);
            getRowConstraints().add(row);
        }

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                StackPane tile = new StackPane();
                tiles[row][col] = tile;
                tile.setMinSize(0, 0);
                Color color;
                Color hoverColor;
                if ((row + col) % 2 == 0) {
                    color = Color.rgb(238, 238, 210);
                    hoverColor = Color.rgb(244, 244, 220);
                } else {
                    color = Color.rgb(118, 150, 86);
                    hoverColor = Color.rgb(128, 160, 96);
                }

                tile.setBackground(
                        new Background(
                                new BackgroundFill(
                                        color,
                                        null,
                                        null
                                )
                        )
                );
                tile.setOnMouseEntered(event -> {
                    tile.setBackground(
                            new Background(
                                    new BackgroundFill(
                                            hoverColor,
                                            null,
                                            null
                                    )
                            )
                    );
                });
                tile.setOnMouseExited(event -> {
                    tile.setBackground(
                            new Background(
                                    new BackgroundFill(
                                            color,
                                            null,
                                            null
                                    )
                            )
                    );
                });
                final int currentRow = row;
                final int currentCol = col;

                tile.setOnMouseClicked(event -> {
                    handleClick(currentRow, currentCol);
                });

                add(tile, col, row);

            }
        }

    }

    private void handleClick(int row, int col) {
        Position clickedPosition = new Position(row, col);
        if (selectedPosition == null) {
            Piece piece = boardState.getPieceAt(clickedPosition);
            if (piece == null) {
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
            return;
        }

        Move move = new Move(
                selectedPosition,
                clickedPosition,
                null
        );

        if (RulesEngine.isLegalMove(boardState, move)) {
            history.push(boardState.copy());
            boardState.executeMove(move);
            selectedRow = -1;
            selectedCol = -1;
            clearHighlights();
            renderBoard();

            if (onTurnEnd != null) onTurnEnd.run();
            if (boardState.getActiveColor() == PlayerColor.BLACK) {
                new Thread(() -> {
                    Move botMove = bot.calculateMove(boardState);
                    Platform.runLater(() -> {
                        if (botMove != null) {
                            boardState.executeMove(botMove);
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
        selectedPosition = null;
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
                Position position = new Position(row, col);
                Piece piece = boardState.getPieceAt(position);
                if (piece == null) {
                    continue;
                }
                Image image = getImage(piece);
                double size =
                        piece.getType() == PieceType.PAWN
                                ? 75
                                : 100;
                tiles[row][col].getChildren().add(
                        createPiece(image, size)
                );
            }
        }
        cellsLetters();
        cellsNumbers();
    }

    private ImageView createPiece(Image image, double width) {
        ImageView piece = new ImageView(image);
        piece.setPreserveRatio(true);
        piece.setMouseTransparent(true);
        piece.setFitWidth(width);
        DropShadow outline = new DropShadow();
        outline.setRadius(6);
        outline.setSpread(0.2);
        outline.setColor(Color.rgb(0, 0, 0, 0.6));
        piece.setEffect(outline);
        return piece;
    }

    private void cellsLetters() {
        String letters = "abcdefgh";
        for (int col = 0; col < 8; col++) {
            Label letter = new Label(
                    String.valueOf(letters.charAt(col))
            );

            letter.setFont(Font.font(12));
            if ((7 + col) % 2 == 0) {
                letter.setTextFill(
                        Color.rgb(118, 150, 86)
                );

            } else {
                letter.setTextFill(
                        Color.rgb(238, 238, 210)
                );
            }

            StackPane.setAlignment(letter, Pos.BOTTOM_RIGHT);
            letter.setTranslateX(-4);
            letter.setTranslateY(-2);
            tiles[7][col].getChildren().add(letter);
            letter.toFront();
        }
    }

    public void restartGame() {
        this.setDisable(false);
        boardState = new BoardState();
        history.clear();
        gameStarted = false;
        renderBoard();
    }

    public void undoMove() {
        if (!history.isEmpty()) {
            BoardState previousState = history.pop();
            boardState = previousState.copy();
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
