package chess.ui;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class MusicManager {

    private static MediaPlayer player;

    public static void initialize() {

        Media music = new Media(
                MusicManager.class
                        .getResource("/sounds/background.mp3")
                        .toExternalForm()
        );

        player = new MediaPlayer(music);

        player.setCycleCount(
                MediaPlayer.INDEFINITE
        );

        player.setVolume(0.15);

        player.play();
    }

    public static MediaPlayer getPlayer() {
        return player;
    }
}
