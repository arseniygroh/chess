package chess.network.protocol;

public record AuthResponse(boolean success, String message, UserProfile profile) implements Packet {
}
