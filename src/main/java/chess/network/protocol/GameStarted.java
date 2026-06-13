package chess.network.protocol;

import chess.model.PlayerColor;

public record GameStarted(String gameId, PlayerColor assignedColor, String opponentName) implements Packet {
}
