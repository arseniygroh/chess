package chess.network.protocol;

public record RegisterRequest(String username, String password) implements Packet {
}
