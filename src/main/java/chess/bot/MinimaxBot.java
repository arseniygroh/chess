package chess.bot;

import chess.model.*;
import chess.model.pieces.Piece;

import java.util.ArrayList;
import java.util.List;

public class MinimaxBot implements ChessBot{

    @Override
    public Move calculateMove(BoardState board) {
        long startTime = System.currentTimeMillis();
        System.out.println("Бот почав думати");

        BoardState copy;
        int minEval = 10000;
        Move bestMove = null;
        for (Move move : getAllLegalMoves(board)) {
            copy = board.copy();
            copy.executeMove(move);
            int movEval = minimax(copy, 3, -10000, 10000, true);
            if (movEval < minEval) {
                minEval = movEval;
                bestMove = move;
            }
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        System.out.println("Час витрачений на роздуми: " + duration + " мс (" + (duration / 1000.0) + " сек)");

        return bestMove;
    }

    private int minimax(BoardState position, int depth, int alpha, int beta, boolean maximizingPlayer){
        BoardState copy;
        if(depth == 0 || RulesEngine.isCheckMate(position))
            return Evaluator.evaluate(position);
        if (maximizingPlayer){
            int maxEval = -10000;
            for (Move move : getAllLegalMoves(position)) {
                copy = position.copy();
                copy.executeMove(move);
                int eval = minimax(copy, depth-1, alpha, beta, false);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) break;
            }
            return maxEval;
        }else{
            int minEval = 10000;
            for (Move move : getAllLegalMoves(position)) {
                copy = position.copy();
                copy.executeMove(move);
                int eval = minimax(copy, depth-1, alpha, beta, true);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) break;
            }
            return minEval;
        }
    }

    private List<Move> getAllLegalMoves(BoardState board) {
        List<Move> allMoves = new ArrayList<>();
        PlayerColor currentColor = board.getActiveColor();

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Position pos = new Position(r, c);
                Piece piece = board.getPieceAt(pos);

                if (piece != null && piece.getColor() == currentColor) {
                    allMoves.addAll(RulesEngine.getStrictlyLegalMovesForPiece(board, pos));
                }
            }
        }
        return allMoves;
    }
}

