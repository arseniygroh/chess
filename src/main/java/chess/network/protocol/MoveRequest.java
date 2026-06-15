package chess.network.protocol;

import chess.model.Move;

public record MoveRequest(String gameId, Move move) implements Packet {
}
