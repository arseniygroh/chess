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
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.List;
import java.util.function.Consumer;

public class LeaderboardMenu extends StackPane {
    private final StackPane root;
    private final VBox rankingList = new VBox(10);
    private final Consumer<Packet> packetListener = this::handlePacket;

    public LeaderboardMenu(StackPane root) {
        this.root = root;

        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setStyle("-fx-background-color: #2b2b2b; -fx-padding: 30;");

        Label title = new Label("Global Leaderboard");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setTextFill(Color.WHITE);

        ScrollPane scrollPane = new ScrollPane(rankingList);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);
        scrollPane.setStyle("-fx-background: #333333; -fx-background-color: #333333;");
        rankingList.setPadding(new Insets(15));
        rankingList.setStyle("-fx-background-color: #333333;");

        Button backButton = new Button("Back");
        backButton.setPrefSize(200, 45);
        backButton.setStyle("-fx-background-color: #555555; -fx-text-fill: white; -fx-font-size: 18; -fx-background-radius: 5; -fx-cursor: hand;");
        backButton.setOnAction(e -> {
            ClientConnection.getInstance().removeListener(packetListener);
            LobbyMenu lobby = LobbyMenu.getInstance();
            if (lobby != null && GameSettings.currentUser != null) {
                root.getChildren().setAll(lobby);
            } else {
                root.getChildren().setAll(new MainMenu(root));
            }
        });

        content.getChildren().addAll(title, scrollPane, backButton);
        this.getChildren().add(content);

        ClientConnection.getInstance().addListener(packetListener);
        ClientConnection.getInstance().sendPacket(new LeaderboardRequest());
    }

    private void handlePacket(Packet packet) {
        if (packet instanceof LeaderboardUpdate update) {
            updateRankingList(update.rankings());
        }
    }

    private void updateRankingList(List<UserProfile> users) {
        rankingList.getChildren().clear();
        
        // Sort by Elo descending
        List<UserProfile> sorted = users.stream()
                .sorted((u1, u2) -> Integer.compare(u2.elo(), u1.elo()))
                .toList();

        for (int i = 0; i < sorted.size(); i++) {
            UserProfile user = sorted.get(i);
            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-background-color: " + (i % 2 == 0 ? "#444444" : "#3d3d3d") + "; -fx-padding: 10; -fx-background-radius: 5; -fx-cursor: hand;");

            row.setOnMouseClicked(e -> showProfilePopup(user));

            Label rankLabel = new Label("#" + (i + 1));
            rankLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
            rankLabel.setTextFill(i < 3 ? Color.GOLD : Color.WHITE);
            rankLabel.setMinWidth(40);

            Node avatar = createSmallAvatar(user.profilePicture(), user.username());

            String displayName = user.username();
            if (GameSettings.currentUser != null && GameSettings.currentUser.username().equals(user.username())) {
                displayName += " (YOU)";
            }
            Label nameLabel = new Label(displayName);
            nameLabel.setTextFill(Color.WHITE);
            nameLabel.setFont(Font.font(18));
            nameLabel.setMinWidth(150);

            Label eloLabel = new Label("Elo: " + user.elo());
            eloLabel.setTextFill(Color.LIGHTGREEN);
            eloLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

            Label statsLabel = new Label("W: " + user.wins() + " / L: " + user.losses());
            statsLabel.setTextFill(Color.LIGHTGRAY);
            statsLabel.setFont(Font.font(14));

            row.getChildren().addAll(rankLabel, avatar, nameLabel, new Region(), eloLabel, new Region(), statsLabel);
            HBox.setHgrow(row.getChildren().get(3), Priority.ALWAYS);
            HBox.setHgrow(row.getChildren().get(5), Priority.ALWAYS);
            rankingList.getChildren().add(row);
        }
    }

    private Node createSmallAvatar(String picData, String username) {
        if (picData != null && !picData.isEmpty()) {
            try {
                Image img;
                if (picData.startsWith("http")) {
                    img = new Image(picData, true);
                } else {
                    byte[] bytes = Base64.getDecoder().decode(picData);
                    img = new Image(new ByteArrayInputStream(bytes));
                }
                ImageView iv = new ImageView(img);
                iv.setFitWidth(30);
                iv.setFitHeight(30);
                iv.setPreserveRatio(true);
                Circle clip = new Circle(15, 15, 15);
                iv.setClip(clip);
                return iv;
            } catch (Exception e) {}
        }
        Circle circle = new Circle(15, Color.web("#769656"));
        Label label = new Label(username.substring(0, 1).toUpperCase());
        label.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        label.setTextFill(Color.WHITE);
        return new StackPane(circle, label);
    }

    private void showProfilePopup(UserProfile user) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Player Profile");
        DialogPane pane = dialog.getDialogPane();
        pane.setStyle("-fx-background-color: #2b2b2b;");
        
        ProfileMenu profileMenu = new ProfileMenu(root, user);
        // Remove the back button for the popup
        profileMenu.getChildren().removeIf(node -> node instanceof Button && ((Button)node).getText().equals("Back"));
        
        pane.setContent(profileMenu);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
}
