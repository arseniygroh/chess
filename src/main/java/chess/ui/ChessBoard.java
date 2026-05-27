package chess.ui;

import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.effect.DropShadow;
public class ChessBoard extends GridPane {

    private static final int BOARD_SIZE = 8;
    private static final int TILE_SIZE = 100;
    private final StackPane[][] tiles = new StackPane[8][8];

    public ChessBoard() {

        createBoard();
        addPawns();
        addRooks();
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

                add(tile, col, row);
            }
        }
    }
    private ImageView createPiece(Image image, double size) {

        ImageView piece = new ImageView(image);

        piece.setPreserveRatio(true);

        piece.setMouseTransparent(true);

        piece.setFitWidth(size);

        DropShadow outline = new DropShadow();

        outline.setRadius(6);

        outline.setSpread(0.2);

        outline.setColor(Color.rgb(0, 0, 0, 0.6));

        piece.setEffect(outline);

        return piece;
    }
    private void addRooks() {

        Image whiteRookImage = new Image(
                getClass().getResourceAsStream("/pieces/wr.png")
        );

        Image blackRookImage = new Image(
                getClass().getResourceAsStream("/pieces/br.png")
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
    private void addPawns() {

        Image whitePawnImage = new Image(
                getClass().getResourceAsStream("/pieces/wp.png")
        );

        Image blackPawnImage = new Image(
                getClass().getResourceAsStream("/pieces/bp.png")
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