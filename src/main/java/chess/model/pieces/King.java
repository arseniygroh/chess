package chess.model.pieces;

import chess.model.*;

import java.util.ArrayList;
import java.util.List;

public class King extends Piece {
    private static final int[][] OFFSETS = {{1,1}, {1,-1}, {-1,1}, {-1,-1}, {1,0}, {-1,0}, {0,1}, {0,-1}};
    public King(PlayerColor color) {
        super(color, PieceType.KING);
    }

    @Override
    public List<Move> getPseudoLegalMoves(BoardState board, Position currentPos) {
        List<Move> moves = new ArrayList<>(getJumpingMoves(board, currentPos, OFFSETS));
        int row = currentPos.row();
        boolean isWhite = this.getColor() == PlayerColor.WHITE;

        if (currentPos.col() == 4 && ((isWhite && row == 7) || (!isWhite && row == 0))) {
            boolean canKingside = isWhite ? board.canWhiteCastleKingside() : board.canBlackCastleKingside();
            if (canKingside) {
                if (board.getPieceAt(new Position(row, 5)) == null &&
                        board.getPieceAt(new Position(row, 6)) == null) {
                    moves.add(new Move(currentPos, new Position(row, 6), null));
                }
            }
            boolean canQueenside = isWhite ? board.canWhiteCastleQueenside() : board.canBlackCastleQueenside();
            if (canQueenside) {
                if (board.getPieceAt(new Position(row, 1)) == null &&
                        board.getPieceAt(new Position(row, 2)) == null &&
                        board.getPieceAt(new Position(row, 3)) == null) {
                    moves.add(new Move(currentPos, new Position(row, 2), null));
                }
            }
        }
        return moves;
    }
}
