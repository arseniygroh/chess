package chess.network.protocol;

import chess.model.BoardState;
import chess.model.Move;

public record GameStateUpdate(
    String gameId, 
    BoardState boardState, 
    Move lastMove, 
    boolean isGameOver, 
    String winner
) implements Packet {
}
