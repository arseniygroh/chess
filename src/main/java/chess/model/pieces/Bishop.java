package chess.model.pieces;

import chess.model.*;

import java.util.List;

public class Bishop extends Piece{
    private static final int[][] DIRS = {{1,1}, {1,-1}, {-1,1}, {-1,-1}};

    public Bishop(PlayerColor color) {
        super(color, PieceType.BISHOP);
    }

    @Override
    public List<Move> getPseudoLegalMoves(BoardState board, Position pos) {
        return getSlidingMoves(board, pos, DIRS);
    }
}
