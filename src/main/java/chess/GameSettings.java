package chess;

import chess.bot.BotDifficulty;
import chess.model.PlayerColor;

public class GameSettings {

    public static BotDifficulty botDifficulty =
            BotDifficulty.MEDIUM;
    public static boolean isBotGame = true;
    public static boolean isNetworkGame = false;
    public static boolean isHost = false;
    public static String serverAddress = "localhost";
    public static int port = 12345;
    public static PlayerColor playerColor = PlayerColor.WHITE;
}
