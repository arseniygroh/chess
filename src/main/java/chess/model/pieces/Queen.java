package chess.model.pieces;

import chess.model.*;
import java.util.List;

public class Queen extends Piece {
    private final int[][] DIRS = {{0,1}, {0,-1}, {1,0}, {-1,0}, {1,1}, {1,-1}, {-1,1}, {-1,-1}};
    public Queen(PlayerColor color) {
        super(color, PieceType.QUEEN);
    }

    @Override
    public List<Move> getPseudoLegalMoves(BoardState board, Position currentPos) {
        return getSlidingMoves(board, currentPos, DIRS);
    }
}
