package chess.network.protocol;

public record LoginRequest(String username, String password) implements Packet {
}
