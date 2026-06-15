package chess.network.protocol;

public record ChallengeResponse(String challengerName, String opponentName, boolean accepted) implements Packet {
}
