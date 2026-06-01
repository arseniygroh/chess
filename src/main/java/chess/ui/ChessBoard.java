package chess.ui;

import chess.bot.ChessBot;
import chess.model.*;
import chess.model.pieces.Piece;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.effect.DropShadow;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.geometry.Pos;

public class ChessBoard extends GridPane {

    private static final int BOARD_SIZE = 8;
    private static final int TILE_SIZE = 100;
    private final StackPane[][] tiles = new StackPane[8][8];
    private Position selectedPosition = null;
    private final BoardState boardState = new BoardState();
    private final ChessBot bot = new chess.bot.RandomBot();

    public ChessBoard() {

        createBoard();

        renderBoard();
        cellsLetters();
        cellsNumbers();

    }

    private void cellsNumbers() {
        String numbers = "87654321";

        for (int row = 0; row < 8; row++) {

            Label letter = new Label(
                    String.valueOf(numbers.charAt(row))
            );

            letter.setFont(Font.font(12));

            if ( row % 2 == 0) {

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

            selectedPosition = clickedPosition;

            return;
        }

        Move move = new Move(
                selectedPosition,
                clickedPosition,
                null
        );

        if (RulesEngine.isLegalMove(boardState, move)) {

            boardState.executeMove(move);

            renderBoard();

            cellsLetters();
            cellsNumbers();

            System.out.println("Хід виконано гравцем");

            if (boardState.getActiveColor() == PlayerColor.BLACK) {

                Move botMove = bot.calculateMove(boardState);

                if (botMove != null) {
                    boardState.executeMove(botMove);
                    renderBoard();
                    cellsLetters();
                    cellsNumbers();
                    System.out.println("Бот зробив хід: " + botMove.start() + " -> " + botMove.end());
                } else {
                    System.out.println("Боту нікуди ходити!!!!!!");
                }
            }


        } else {

            System.out.println("Нелегальний хід");
        }

        selectedPosition = null;
    }
    private String getImagePath(Piece piece) {
        String color =
                piece.getColor() == PlayerColor.WHITE
                        ? "white "
                        : "black ";
        switch (piece.getType()) {

            case KING:
                return "/pieces/" + color + "king.png";

            case QUEEN:
                return "/pieces/" + color + "queen.png";

            case ROOK:
                return "/pieces/" + color + "rook.png";

            case BISHOP:
                return "/pieces/" + color + "officer.png";

            case KNIGHT:
                return "/pieces/" + color + "horse.png";

            case PAWN:
               return "/pieces/" + color + "pawn.png";
            default:
                throw new IllegalStateException(
                        "Unknown piece type: " + piece.getType()
                );
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

                String imagePath = getImagePath(piece);

                Image image = new Image(
                        getClass().getResourceAsStream(imagePath)
                );
                double size =
                        piece.getType() == PieceType.PAWN
                                ? 75
                                : 100;
                tiles[row][col].getChildren().add(
                        createPiece(image, size)
                );
            }
        }


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
    private void addKings(){
        Image whiteKingImage = new Image(
                getClass().getResourceAsStream("/pieces/white king.png")
        );

        Image blackKingImage = new Image(
                getClass().getResourceAsStream("/pieces/black king.png")
        );
        tiles[0][4].getChildren().add(createPiece(blackKingImage, 120));

        tiles[7][4].getChildren().add(createPiece(whiteKingImage, 120));
    }
    private void addQueens(){
        Image whiteQueenImage = new Image(
                getClass().getResourceAsStream("/pieces/white queen.png")
        );

        Image blackQueenImage = new Image(
                getClass().getResourceAsStream("/pieces/black queen.png")
        );
        tiles[0][3].getChildren().add(createPiece(blackQueenImage, 120));

        tiles[7][3].getChildren().add(createPiece(whiteQueenImage, 120));
    }
    private void addOfficers(){
        Image whiteOfficerImage = new Image(
                getClass().getResourceAsStream("/pieces/white officer.png")
        );

        Image blackOfficerImage = new Image(
                getClass().getResourceAsStream("/pieces/black officer.png")
        );
        tiles[0][2].getChildren().add(createPiece(blackOfficerImage, 120));
        tiles[0][5].getChildren().add(createPiece(blackOfficerImage, 120));

        tiles[7][2].getChildren().add(createPiece(whiteOfficerImage, 120));
        tiles[7][5].getChildren().add(createPiece(whiteOfficerImage, 120));

    }
    private void addHorses(){
        Image whiteHorseImage = new Image(
                getClass().getResourceAsStream("/pieces/white horse.png")
        );

        Image blackHorseImage = new Image(
                getClass().getResourceAsStream("/pieces/black horse.png")
        );
        tiles[0][1].getChildren().add(createPiece(blackHorseImage, 120));
        tiles[0][6].getChildren().add(createPiece(blackHorseImage, 120));

        tiles[7][1].getChildren().add(createPiece(whiteHorseImage, 120));
        tiles[7][6].getChildren().add(createPiece(whiteHorseImage, 120));

    }
    private void addRooks() {

        Image whiteRookImage = new Image(
                getClass().getResourceAsStream("/pieces/white rook.png")
        );

        Image blackRookImage = new Image(
                getClass().getResourceAsStream("/pieces/black rook.png")
        );

        tiles[7][0].getChildren().add(
                createPiece(whiteRookImage,130)
        );

        tiles[7][7].getChildren().add(
                createPiece(whiteRookImage,130)
        );

        tiles[0][0].getChildren().add(
                createPiece(blackRookImage,130)
        );

        tiles[0][7].getChildren().add(
                createPiece(blackRookImage,130)
        );
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
    private void addPawns() {

        Image whitePawnImage = new Image(
                getClass().getResourceAsStream("/pieces/white pawn.png")
        );

        Image blackPawnImage = new Image(
                getClass().getResourceAsStream("/pieces/black pawn.png")
        );

        for (int col = 0; col < 8; col++) {

            tiles[1][col].getChildren().add(
                    createPiece(blackPawnImage,75)
            );

            tiles[6][col].getChildren().add(
                    createPiece(whitePawnImage ,75)
            );
        }
    }
}