package chess.model.pieces;

import chess.model.*;

import java.util.List;

public class King extends Piece {
    private static final int[][] OFFSETS = {{1,1}, {1,-1}, {-1,1}, {-1,-1}, {1,0}, {-1,0}, {0,1}, {0,-1}};
    public King(PlayerColor color) {
        super(color, PieceType.KING);
    }

    @Override
    public List<Move> getPseudoLegalMoves(BoardState board, Position currentPos) {
        return getJumpingMoves(board, currentPos, OFFSETS);
    }
}
