package chess.model;

import chess.model.pieces.Piece;
import java.util.ArrayList;
import java.util.List;

public class RulesEngine {

    public static boolean isLegalMove(BoardState board, Move move) {
        PlayerColor currentColor = board.getActiveColor();
        Piece movingPiece = board.getPieceAt(move.start());

        if (movingPiece == null || movingPiece.getColor() != currentColor) {
            return false;
        }

        List<Move> legalMoves = getStrictlyLegalMovesForPiece(board, move.start());
        return legalMoves.contains(move);
    }

    public static List<Move> getStrictlyLegalMovesForPiece(BoardState board, Position pos) {
        Piece piece = board.getPieceAt(pos);
        List<Move> legalMoves = new ArrayList<>();
        if (piece == null) return legalMoves;

        List<Move> pseudoMoves = piece.getPseudoLegalMoves(board, pos);

        for (Move testMove : pseudoMoves) {
            BoardState simulatedBoard = board.copy();
            simulatedBoard.executeMove(testMove);
            if (!isKingInCheck(simulatedBoard, piece.getColor())) {
                if (piece.getType() == PieceType.KING && Math.abs(testMove.end().col() - testMove.start().col()) == 2) {
                    if (isKingInCheck(board, piece.getColor())) {
                        continue;
                    }
                    int direction = (testMove.end().col() > testMove.start().col()) ? 1 : -1;
                    Position passingSquare = new Position(testMove.start().row(), testMove.start().col() + direction);
                    BoardState halfMoveBoard = board.copy();
                    halfMoveBoard.executeMove(new Move(testMove.start(), passingSquare, null));

                    if (isKingInCheck(halfMoveBoard, piece.getColor())) {
                        continue;
                    }
                }
                legalMoves.add(testMove);
            }
        }
        return legalMoves;
    }

    public static boolean isKingInCheck(BoardState board, PlayerColor color) {
        Position kingPos = findKingPos(board, color);
        if (kingPos == null) return false;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Position currentPos = new Position(i, j);
                Piece piece = board.getPieceAt(currentPos);
                if (piece != null && piece.getColor() != color) {
                    List<Move> enemyMoves = piece.getPseudoLegalMoves(board, currentPos);
                    for (Move move : enemyMoves) {
                        if (move.end().equals(kingPos)) return true;
                    }
                }
            }
        }
        return false;
    }

    private static Position findKingPos(BoardState board, PlayerColor color) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Position currentPos = new Position(i, j);
                Piece piece = board.getPieceAt(currentPos);
                if (piece != null && piece.getType() == PieceType.KING && piece.getColor() == color) {
                    return currentPos;
                }
            }
        }
        return null;
    }

    private static boolean hasNoLegalMovesLeft(BoardState board, PlayerColor color) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Position pos = new Position(row, col);
                Piece piece = board.getPieceAt(pos);
                if (piece != null && piece.getColor() == color) {
                    if (!getStrictlyLegalMovesForPiece(board, pos).isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static boolean isCheckMate(BoardState board) {
        PlayerColor currentColor = board.getActiveColor();
        return isKingInCheck(board, currentColor) && hasNoLegalMovesLeft(board, currentColor);
    }

    public static boolean isStaleMate(BoardState board) {
        PlayerColor currentColor = board.getActiveColor();
        return !isKingInCheck(board, currentColor) && hasNoLegalMovesLeft(board, currentColor);
    }

    public static java.util.Set<Position> getVisibleSquares(BoardState board, PlayerColor viewerColor) {
        java.util.Set<Position> visible = new java.util.HashSet<>();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Position pos = new Position(row, col);
                Piece piece = board.getPieceAt(pos);
                if (piece != null && piece.getColor() == viewerColor) {
                    visible.add(pos);
                    
                    // Use pseudo-legal moves for visibility to cover attacked squares
                    // even if moving there is currently illegal (e.g. pinned piece)
                    List<Move> pseudoMoves = piece.getPseudoLegalMoves(board, pos);
                    for (Move m : pseudoMoves) {
                        visible.add(m.end());
                    }
                    
                    // Pawn special case: they always see diagonal squares they attack
                    // Pseudo-legal moves for pawns only include diagonals if there's a capture or en passant.
                    // We need to add them even if empty for visibility.
                    if (piece.getType() == PieceType.PAWN) {
                        int offset = (viewerColor == PlayerColor.WHITE) ? -1 : 1;
                        int nextRow = row + offset;
                        if (nextRow >= 0 && nextRow < 8) {
                            if (col - 1 >= 0) visible.add(new Position(nextRow, col - 1));
                            if (col + 1 < 8) visible.add(new Position(nextRow, col + 1));
                        }
                    }
                }
            }
        }
        return visible;
    }
}
