package chess.network.protocol;

public record ResignRequest(String gameId) implements Packet {
}
