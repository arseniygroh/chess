package chess.bot;

import chess.model.*;
import chess.model.pieces.Piece;

import java.util.ArrayList;
import java.util.List;

public class MinimaxBot implements ChessBot {

    private final int maxDepth;

    public MinimaxBot(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    @Override
    public Move calculateMove(BoardState board) {
        long startTime = System.currentTimeMillis();

        boolean isWhiteSide = board.getActiveColor() == PlayerColor.WHITE;

        System.out.println("Бот почав думати на глибині: " + maxDepth);

        BoardState copy;

        int minOrMaxEval = isWhiteSide ? -100000 : 100000;
        Move bestMove = null;

        List<Move> legalMoves = getAllLegalMoves(board);
        if (legalMoves.isEmpty()) return null;

        for (Move move : legalMoves) {
            copy = board.copy();
            copy.executeMove(move);

            int movEval = minimax(copy, maxDepth, -100000, 100000, !isWhiteSide);

            if (!isWhiteSide && movEval < minOrMaxEval || isWhiteSide && movEval > minOrMaxEval) {
                minOrMaxEval = movEval;
                bestMove = move;
            }
        }

        if (bestMove == null) {
            bestMove = legalMoves.get(0);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        System.out.println("Час витрачений на роздуми: " + duration + " мс (" + (duration / 1000.0) + " сек)");

        return bestMove;
    }

    private int minimax(BoardState position, int depth, int alpha, int beta, boolean maximizingPlayer){
        BoardState copy;

        if (RulesEngine.isCheckMate(position)) {
            return position.getActiveColor() == PlayerColor.WHITE ? -10000 - depth : 10000 + depth;
        }

        if (depth == 0) {
            return Evaluator.evaluate(position);
        }

        if (maximizingPlayer){
            int maxEval = -100000;
            for (Move move : getAllLegalMoves(position)) {
                copy = position.copy();
                copy.executeMove(move);
                int eval = minimax(copy, depth-1, alpha, beta, false);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) break;
            }
            return maxEval;
        } else {
            int minEval = 100000;
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