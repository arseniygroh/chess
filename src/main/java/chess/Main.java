package chess;

import chess.network.client.ClientConnection;
import chess.network.protocol.*;
import chess.ui.GameView;
import chess.ui.LobbyMenu;
import chess.ui.MainMenu;
import chess.ui.MusicManager;
import chess.util.CredentialsManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;


public class Main extends Application {

    private static final int WINDOW_WIDTH = 950;
    private static final int WINDOW_HEIGHT = 800;
    private static final int MIN_WIDTH = 400;
    private static final int MIN_HEIGHT = 400;

    private StackPane root;

    @Override
    public void start(Stage stage) {
        root = new StackPane();
        MusicManager.initialize();
        root.getChildren().add(
                new MainMenu(root)
        );

        Scene scene = new Scene(
                root,
                WINDOW_WIDTH,
                WINDOW_HEIGHT
        );

        Image appIcon = new Image(getClass().getResourceAsStream("/icon.png"));
        stage.getIcons().add(appIcon);
        stage.setTitle("Chess");
        stage.setScene(scene);
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        stage.show();

        setupGlobalListeners();
        tryAutoLogin();
    }

    private void setupGlobalListeners() {
        ClientConnection.getInstance().addListener(packet -> {
            if (packet instanceof ChallengeRequest req) {
                // Only show challenge if not currently in a game
                if (root.getChildren().isEmpty() || !(root.getChildren().get(0) instanceof GameView)) {
                    showChallengeDialog(req);
                }
            } else if (packet instanceof ChallengeResponse res) {
                if (!res.accepted()) {
                    showDeclineAlert(res.opponentName());
                }
            } else if (packet instanceof GameStarted start) {
                startGame(start);
            }
        });
    }

    private void showDeclineAlert(String opponent) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Challenge Declined");
            alert.setHeaderText(null);
            alert.setContentText(opponent + " has declined your challenge.");
            alert.show();
        });
    }

    private void showChallengeDialog(ChallengeRequest req) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Incoming Challenge");
        String mode = req.isFogOfWar() ? "FOG OF WAR" : "Standard";
        alert.setHeaderText(req.challengerName() + " has challenged you to a " + mode + " game!");
        alert.setContentText("Do you accept?");

        ButtonType acceptBtn = new ButtonType("Accept");
        ButtonType declineBtn = new ButtonType("Decline", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(acceptBtn, declineBtn);

        alert.showAndWait().ifPresent(type -> {
            boolean accepted = (type == acceptBtn);
            if (accepted) {
                GameSettings.isFogOfWar = req.isFogOfWar();
            }
            ClientConnection.getInstance().sendPacket(new ChallengeResponse(req.challengerName(), GameSettings.currentUser.username(), accepted, req.isFogOfWar()));
        });
    }

    private void startGame(GameStarted start) {
        GameSettings.isFogOfWar = start.isFogOfWar();
        GameSettings.isNetworkGame = true;
        GameSettings.isBotGame = false;
        GameSettings.playerColor = start.assignedColor();

        Platform.runLater(() -> {
            GameView gameView = new GameView(root, false, 10);
            gameView.setNetworkGame(start.gameId(), start.assignedColor(), start.opponentName());
            root.getChildren().setAll(gameView);
        });
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