package chess.network.protocol;

import java.io.Serializable;

public record UserProfile(String username, int elo, int wins, int losses) implements Serializable {
}
