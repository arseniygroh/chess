package chess.network.protocol;

public record UpdateProfileRequest(String description, String profilePicture, String newPassword) implements Packet {
}
