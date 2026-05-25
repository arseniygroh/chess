package chess.model.pieces;

import chess.model.*;

import java.util.ArrayList;
import java.util.List;

public class Pawn extends Piece{

    public Pawn(PlayerColor color) { super(color, PieceType.PAWN); }
    @Override
    public List<Move> getPseudoLegalMoves(BoardState board, Position currentPos) {
        List<Move> moves = new ArrayList<>();

        int offset = this.getColor() == PlayerColor.WHITE ? -1 : 1;
        int newRow = currentPos.row() + offset;

        if (newRow >= 0 && newRow < 8 && board.getPieceAt(new Position(newRow, currentPos.col())) == null) {
            boolean isPromotion = newRow == 7 || newRow == 0;
            if (isPromotion) {
                moves.add(new Move(currentPos, new Position(newRow, currentPos.col()), PieceType.QUEEN));
                moves.add(new Move(currentPos, new Position(newRow, currentPos.col()), PieceType.ROOK));
                moves.add(new Move(currentPos, new Position(newRow, currentPos.col()), PieceType.KNIGHT));
                moves.add(new Move(currentPos, new Position(newRow, currentPos.col()), PieceType.BISHOP));
            } else moves.add(new Move(currentPos, new Position(newRow, currentPos.col()), null));

            boolean isStartingRow = (this.getColor() == PlayerColor.WHITE && currentPos.row() == 6) ||
                    (this.getColor() == PlayerColor.BLACK && currentPos.row() == 1);

            if (isStartingRow) {
                int doubleRow = currentPos.row() + (offset * 2);
                if (board.getPieceAt(new Position(doubleRow, currentPos.col())) == null) {
                    moves.add(new Move(currentPos, new Position(doubleRow, currentPos.col()), null));
                }
            }
        }
        List<Position> capturePositions = getCapturePositions(board, currentPos, offset);

        for (Position target : capturePositions) {
            boolean isPromotion = target.row() == 7 || target.row() == 0;
            if (isPromotion) {
                moves.add(new Move(currentPos, target, PieceType.QUEEN));
                moves.add(new Move(currentPos, target, PieceType.ROOK));
                moves.add(new Move(currentPos, target, PieceType.KNIGHT));
                moves.add(new Move(currentPos, target, PieceType.BISHOP));
            } else {
                moves.add(new Move(currentPos, target, null));
            }
        }
        return moves;
    }

    private List<Position> getCapturePositions(BoardState board, Position currentPos, int offset) {
        List<Position> positions = new ArrayList<>();
        Position left = null;
        Position right = null;
        if (currentPos.col() - 1 >= 0) {
            left = new Position(currentPos.row() + offset, currentPos.col() - 1);
        }
        if (currentPos.col() + 1 < 8) {
            right = new Position(currentPos.row() + offset, currentPos.col() + 1);
        }
        if (left != null) {
            boolean canCaptureLeft = board.getPieceAt(left) != null && board.getPieceAt(left).getColor() != this.getColor();
            if (canCaptureLeft) positions.add(left);
        }
        if (right != null) {
            boolean canCaptureRight = board.getPieceAt(right) != null && board.getPieceAt(right).getColor() != this.getColor();
            if (canCaptureRight) positions.add(right);
        }
        return positions;
    }
}
