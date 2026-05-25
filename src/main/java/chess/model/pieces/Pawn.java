package chess.model.pieces;

import chess.model.*;

import java.util.List;

public class Pawn extends Piece{

    public Pawn(PlayerColor color) { super(color, PieceType.PAWN); }
    @Override
    public List<Move> getPseudoLegalMoves(BoardState board, Position currentPos) {
        return List.of();
    }
}
