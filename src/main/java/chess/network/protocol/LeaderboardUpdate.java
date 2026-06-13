package chess.network.protocol;

import java.util.List;

public record LeaderboardUpdate(List<UserProfile> rankings) implements Packet {
}
