package chess.bot;

import chess.model.BoardState;
import chess.model.PlayerColor;
import chess.model.Position;
import chess.model.RulesEngine;
import chess.model.pieces.Piece;

public class Evaluator {

    private static final int CHECKMATE_SCORE = 10000;

    public static int evaluate(BoardState board) {

        if (RulesEngine.isCheckMate(board)) {
            return board.getActiveColor() == PlayerColor.WHITE ? -CHECKMATE_SCORE : CHECKMATE_SCORE;
        }

        if (RulesEngine.isStaleMate(board)) {
            return 0;
        }

        int score = 0;

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Position pos = new Position(r, c);
                Piece piece = board.getPieceAt(pos);

                if (piece != null) {
                    int pieceValue = piece.getType().getValue();

                    if (piece.getColor() == PlayerColor.WHITE) {
                        score += pieceValue;
                    } else {
                        score -= pieceValue;
                    }
                }
            }
        }

        return score;
    }
}