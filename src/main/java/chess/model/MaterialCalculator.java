package chess.model;

import chess.model.pieces.Piece;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MaterialCalculator {
    public static List<PieceType> getCapturedPieces(BoardState board, PlayerColor colorToFind) {
        List<PieceType> standardSet = new ArrayList<>(Arrays.asList(
                PieceType.QUEEN,
                PieceType.ROOK, PieceType.ROOK,
                PieceType.BISHOP, PieceType.BISHOP,
                PieceType.KNIGHT, PieceType.KNIGHT,
                PieceType.PAWN, PieceType.PAWN, PieceType.PAWN, PieceType.PAWN,
                PieceType.PAWN, PieceType.PAWN, PieceType.PAWN, PieceType.PAWN
        ));
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board.getPieceAt(new Position(r, c));
                if (p != null && p.getType() != PieceType.KING && p.getColor() == colorToFind) {
                    standardSet.remove(p.getType());
                }
            }
        }
        return standardSet;
    }
}
