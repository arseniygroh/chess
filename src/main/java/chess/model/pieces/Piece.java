package chess.model.pieces;

import chess.model.*;

import java.util.ArrayList;
import java.util.List;

public abstract class Piece {
    private final PlayerColor color;
    private final PieceType type;

    public Piece(PlayerColor color, PieceType type) {
        this.color = color;
        this.type = type;
    }

    public PlayerColor getColor() {
        return color;
    }

    public PieceType getType() {
        return type;
    }

    public List<Move> getSlidingMoves(BoardState board, Position currentPos, int[][] directions) {
        List<Move> moves = new ArrayList<>();

        for (int[] direction : directions) {
            int r = currentPos.row() + direction[0];
            int c = currentPos.col() + direction[1];

            while (r >= 0 && r < 8 && c >= 0 && c < 8) {
                Position targetPos = new Position(r, c);
                Piece pieceAtTarget = board.getPieceAt(targetPos);

                if (pieceAtTarget == null) {
                    moves.add(new Move(currentPos, targetPos, null));
                } else {
                    if (pieceAtTarget.getColor() != this.getColor()) {
                        moves.add(new Move(currentPos, targetPos, null));
                    }
                    break;
                }
                r += direction[0];
                c += direction[1];
            }
        }
        return moves;
    }

    public abstract List<Move> getPseudoLegalMoves(BoardState board, Position currentPos);
}
