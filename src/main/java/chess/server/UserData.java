package chess.server;

import chess.network.protocol.UserProfile;
import java.io.Serializable;

public class UserData implements Serializable {
    public String username;
    public String password;
    public int elo = 1200;
    public int wins = 0;
    public int losses = 0;
    public String profilePicture = "";
    public String description = "";

    public UserData(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public UserProfile toProfile() {
        return new UserProfile(username, elo, wins, losses, profilePicture, description);
    }
}
