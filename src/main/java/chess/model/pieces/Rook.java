package chess.model.pieces;

import chess.model.*;

import java.util.List;

public class Rook extends Piece {
    private final int[][] DIRS = {{0,1}, {0,-1}, {1,0}, {-1,0}};
    public Rook(PlayerColor color) {
        super(color, PieceType.ROOK);
    }

    @Override
    public List<Move> getPseudoLegalMoves(BoardState board, Position currentPos) {
        return getSlidingMoves(board, currentPos, DIRS);
    }
}
