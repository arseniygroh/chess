package chess.network.protocol;

import java.util.List;

public record LobbyUpdate(List<UserProfile> onlineUsers) implements Packet {
}
