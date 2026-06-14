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
            row.setStyle("-fx-background-color: " + (i % 2 == 0 ? "#444444" : "#3d3d3d") + "; -fx-padding: 10; -fx-background-radius: 5;");

            Label rankLabel = new Label("#" + (i + 1));
            rankLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
            rankLabel.setTextFill(i < 3 ? Color.GOLD : Color.WHITE);
            rankLabel.setMinWidth(40);

            Label nameLabel = new Label(user.username());
            nameLabel.setTextFill(Color.WHITE);
            nameLabel.setFont(Font.font(18));
            nameLabel.setMinWidth(150);

            Label eloLabel = new Label("Elo: " + user.elo());
            eloLabel.setTextFill(Color.LIGHTGREEN);
            eloLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

            Label statsLabel = new Label("W: " + user.wins() + " / L: " + user.losses());
            statsLabel.setTextFill(Color.LIGHTGRAY);
            statsLabel.setFont(Font.font(14));

            row.getChildren().addAll(rankLabel, nameLabel, new Region(), eloLabel, new Region(), statsLabel);
            HBox.setHgrow(row.getChildren().get(2), Priority.ALWAYS);
            HBox.setHgrow(row.getChildren().get(4), Priority.ALWAYS);
            rankingList.getChildren().add(row);
        }
    }
}
