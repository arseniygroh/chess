package chess.network.protocol;

public record ChallengeRequest(String challengerName, String opponentName, boolean isFogOfWar) implements Packet {
}
