package chess.model.pieces;

import chess.model.*;

import java.util.List;

public class Knight extends Piece {
    int[][] OFFSETS = {{2, 1}, {2, -1}, {-2, 1}, {-2, -1}, {1, 2}, {-1, 2}, {1, -2}, {-1, -2}};

    public Knight(PlayerColor color) {
        super(color, PieceType.KNIGHT);
    }

    @Override
    public List<Move> getPseudoLegalMoves(BoardState board, Position currentPos) {
        return getJumpingMoves(board, currentPos, OFFSETS);
    }
}
