package chess.ui;

import chess.GameSettings;
import chess.network.client.ClientConnection;
import chess.network.protocol.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class LobbyMenu extends StackPane {
    private static LobbyMenu instance;
    private final StackPane root;
    private UserProfile myProfile;
    private final VBox playerList = new VBox(10);
    private final Label welcomeLabel = new Label();
    private final java.util.function.Consumer<Packet> packetListener = this::handlePacket;

    public static LobbyMenu getInstance() { return instance; }

    public LobbyMenu(StackPane root, UserProfile profile) {
        instance = this;
        this.root = root;
        this.myProfile = profile;

        BorderPane mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #2b2b2b; -fx-padding: 20;");

        // Top: Profile Info
        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_LEFT);
        welcomeLabel.setText("Welcome, " + profile.username() + " (Elo: " + profile.elo() + ")");
        welcomeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        welcomeLabel.setTextFill(Color.WHITE);
        
        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(e -> {
            chess.util.CredentialsManager.clearCredentials();
            GameSettings.currentUser = null;
            instance = null;
            ClientConnection.getInstance().stop();
            root.getChildren().setAll(new MainMenu(root));
        });

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> {
            ClientConnection.getInstance().removeListener(packetListener);
            root.getChildren().setAll(new MainMenu(root));
        });

        Button leaderboardButton = new Button("Leaderboard");
        leaderboardButton.setOnAction(e -> {
            root.getChildren().setAll(new LeaderboardMenu(root));
        });

        topBar.getChildren().addAll(welcomeLabel, new Region(), leaderboardButton, backButton, logoutButton);
        HBox.setHgrow(topBar.getChildren().get(1), Priority.ALWAYS);
        mainLayout.setTop(topBar);

        // Center: Player List
        VBox centerContent = new VBox(15);
        centerContent.setPadding(new Insets(20, 0, 0, 0));
        Label lobbyTitle = new Label("Online Players");
        lobbyTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        lobbyTitle.setTextFill(Color.LIGHTGRAY);

        ScrollPane scrollPane = new ScrollPane(playerList);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #333333; -fx-background-color: #333333;");
        playerList.setPadding(new Insets(10));
        playerList.setStyle("-fx-background-color: #333333;");

        centerContent.getChildren().addAll(lobbyTitle, scrollPane);
        mainLayout.setCenter(centerContent);

        this.getChildren().add(mainLayout);

        ClientConnection.getInstance().addListener(packetListener);
        ClientConnection.getInstance().sendPacket(new LobbyRequest());
    }

    public void refresh() {
        ClientConnection.getInstance().sendPacket(new LobbyRequest());
        ClientConnection.getInstance().sendPacket(new ProfileRequest());
    }

    private void handlePacket(Packet packet) {
        if (packet instanceof LobbyUpdate update) {
            updatePlayerList(update.onlineUsers());
        } else if (packet instanceof ChallengeRequest req) {
            showChallengeDialog(req);
        } else if (packet instanceof ChallengeResponse res) {
            if (!res.accepted()) {
                showDeclineAlert(res.opponentName());
                refresh(); // Re-enable buttons by refreshing list
            }
        } else if (packet instanceof GameStarted start) {
            startGame(start);
        } else if (packet instanceof AuthResponse res) {
            if (res.success() && res.profile() != null) {
                this.myProfile = res.profile();
                welcomeLabel.setText("Welcome, " + myProfile.username() + " (Elo: " + myProfile.elo() + ")");
            }
        }
    }

    private void showDeclineAlert(String opponent) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Challenge Declined");
        alert.setHeaderText(null);
        alert.setContentText(opponent + " has declined your challenge.");
        alert.show();
    }

    private void updatePlayerList(java.util.List<UserProfile> users) {
        playerList.getChildren().clear();
        for (UserProfile user : users) {
            if (user.username().equals(myProfile.username())) continue;

            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-background-color: #444444; -fx-padding: 10; -fx-background-radius: 5;");

            Label nameLabel = new Label(user.username() + " (" + user.elo() + ")");
            nameLabel.setTextFill(Color.WHITE);
            nameLabel.setFont(Font.font(16));

            Button challengeBtn = new Button("Challenge");
            challengeBtn.setStyle("-fx-background-color: #769656; -fx-text-fill: white;");
            challengeBtn.setOnAction(e -> {
                ClientConnection.getInstance().sendPacket(new ChallengeRequest(myProfile.username(), user.username()));
                challengeBtn.setDisable(true);
                challengeBtn.setText("Waiting...");
            });

            row.getChildren().addAll(nameLabel, new Region(), challengeBtn);
            HBox.setHgrow(row.getChildren().get(1), Priority.ALWAYS);
            playerList.getChildren().add(row);
        }
    }

    private void showChallengeDialog(ChallengeRequest req) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Incoming Challenge");
        alert.setHeaderText(req.challengerName() + " has challenged you to a game!");
        alert.setContentText("Do you accept?");

        ButtonType acceptBtn = new ButtonType("Accept");
        ButtonType declineBtn = new ButtonType("Decline", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(acceptBtn, declineBtn);

        alert.showAndWait().ifPresent(type -> {
            boolean accepted = (type == acceptBtn);
            ClientConnection.getInstance().sendPacket(new ChallengeResponse(req.challengerName(), myProfile.username(), accepted));
        });
    }

    private void startGame(GameStarted start) {
        ClientConnection.getInstance().removeListener(packetListener);
        GameView gameView = new GameView(root, false, 10);
        // We'll need to set up the gameView for network mode
        gameView.setNetworkGame(start.gameId(), start.assignedColor(), start.opponentName());
        root.getChildren().setAll(gameView);
    }
}
