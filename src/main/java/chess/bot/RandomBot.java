package chess.bot;

import chess.model.*;
import chess.model.pieces.Piece;
import chess.ui.ChessBoard;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomBot implements ChessBot {

    private final Random random = new Random();

    @Override
    public Move calculateMove(BoardState boardState, boolean isWhiteSide) {
        List<Move> allLegalMoves = getAllLegalMoves(boardState);

        if (allLegalMoves.isEmpty()) {
            return null;
        }

        int randomIndex = random.nextInt(allLegalMoves.size());
        return allLegalMoves.get(randomIndex);
    }

    private List<Move> getAllLegalMoves(BoardState boardState) {
        List<Move> moves = new ArrayList<>();
        PlayerColor currentColor = boardState.getActiveColor();

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Position pos = new Position(r, c);
                Piece piece = boardState.getPieceAt(pos);

                if (piece != null && piece.getColor() == currentColor) {
                    moves.addAll(RulesEngine.getStrictlyLegalMovesForPiece(boardState, pos));
                }
            }
        }
        return moves;
    }
}