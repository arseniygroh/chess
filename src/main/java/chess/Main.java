package chess;

import chess.network.client.ClientConnection;
import chess.network.protocol.AuthResponse;
import chess.network.protocol.LoginRequest;
import chess.network.protocol.Packet;
import chess.ui.MainMenu;
import chess.ui.MusicManager;
import chess.util.CredentialsManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;


public class Main extends Application {

    private static final int WINDOW_WIDTH = 950;
    private static final int WINDOW_HEIGHT = 800;
    private static final int MIN_WIDTH = 400;
    private static final int MIN_HEIGHT = 400;

    @Override
    public void start(Stage stage) {
        StackPane root = new StackPane();
        MusicManager.initialize();
        root.getChildren().add(
                new MainMenu(root)
        );

        Scene scene = new Scene(
                root,
                WINDOW_WIDTH,
                WINDOW_HEIGHT
        );

        stage.setTitle("Chess");
        stage.setScene(scene);
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        stage.show();

        tryAutoLogin();
    }

    private void tryAutoLogin() {
        CredentialsManager.SavedCredentials saved = CredentialsManager.loadCredentials();
        if (saved.remember() && !saved.username().isEmpty() && !saved.password().isEmpty()) {
            new Thread(() -> {
                try {
                    ClientConnection client = ClientConnection.getInstance();
                    client.connect(GameSettings.serverAddress, GameSettings.port);

                    client.addListener(new java.util.function.Consumer<Packet>() {
                        @Override
                        public void accept(Packet packet) {
                            if (packet instanceof AuthResponse res) {
                                if (res.success() && res.profile() != null) {
                                    GameSettings.currentUser = res.profile();
                                }
                                client.removeListener(this);
                            }
                        }
                    });

                    client.sendPacket(new LoginRequest(saved.username(), saved.password()));
                } catch (IOException e) {
                    System.err.println("Auto-login failed: " + e.getMessage());
                }
            }).start();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}