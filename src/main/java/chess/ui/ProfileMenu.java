package chess.ui;

import chess.GameSettings;
import chess.network.client.ClientConnection;
import chess.network.protocol.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.shape.Circle;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.function.Consumer;

public class ProfileMenu extends StackPane {
    private final StackPane root;
    private UserProfile profile;
    private final Consumer<Packet> packetListener = this::handlePacket;

    public ProfileMenu(StackPane root, UserProfile profile) {
        this(root, profile, true);
    }

    public ProfileMenu(StackPane root, UserProfile profile, boolean showBackButton) {
        this.root = root;
        this.profile = profile;

        boolean isMyProfile = GameSettings.currentUser != null && 
                             GameSettings.currentUser.username().equals(profile.username());

        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setStyle("-fx-background-color: #2b2b2b; -fx-padding: 30;");

        // Avatar
        Node avatarNode = createAvatar(profile.profilePicture(), profile.username());
        
        Label usernameLabel = new Label(profile.username());
        usernameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        usernameLabel.setTextFill(Color.WHITE);

        VBox statsBox = new VBox(10);
        statsBox.setAlignment(Pos.CENTER);
        statsBox.setStyle("-fx-background-color: #333333; -fx-padding: 20; -fx-background-radius: 10;");
        statsBox.setMaxWidth(350);

        statsBox.getChildren().addAll(
            createStatRow("Elo Rating:", String.valueOf(profile.elo()), Color.LIGHTGREEN),
            createStatRow("Wins:", String.valueOf(profile.wins()), Color.WHITE),
            createStatRow("Losses:", String.valueOf(profile.losses()), Color.WHITE),
            createStatRow("Win Rate:", calculateWinRate(profile), Color.LIGHTBLUE)
        );

        Label descLabel = new Label("Description:");
        descLabel.setTextFill(Color.LIGHTGRAY);
        TextArea descArea = new TextArea(profile.description());
        descArea.setEditable(false);
        descArea.setWrapText(true);
        descArea.setPrefHeight(100);
        descArea.setMaxWidth(350);
        descArea.setStyle("-fx-control-inner-background: #333333; -fx-text-fill: white;");

        content.getChildren().addAll(avatarNode, usernameLabel, statsBox, descLabel, descArea);

        if (isMyProfile) {
            descArea.setEditable(true);
            
            TextField passField = new TextField();
            passField.setPromptText("New Password (leave empty to keep)");
            passField.setMaxWidth(350);

            TextField picField = new TextField();
            picField.setPromptText("Profile Picture URL or Base64");
            picField.setMaxWidth(350);

            Button saveBtn = new Button("Save Changes");
            saveBtn.setStyle("-fx-background-color: #769656; -fx-text-fill: white;");
            saveBtn.setOnAction(e -> {
                ClientConnection.getInstance().sendPacket(new UpdateProfileRequest(
                    descArea.getText(),
                    picField.getText(),
                    passField.getText()
                ));
            });

            Button deleteBtn = new Button("Delete Account");
            deleteBtn.setStyle("-fx-background-color: #a04040; -fx-text-fill: white;");
            deleteBtn.setOnAction(e -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete your account? This action cannot be undone.", ButtonType.YES, ButtonType.NO);
                alert.showAndWait().ifPresent(type -> {
                    if (type == ButtonType.YES) {
                        ClientConnection.getInstance().sendPacket(new DeleteProfileRequest());
                    }
                });
            });

            content.getChildren().addAll(passField, picField, saveBtn, deleteBtn);
        }

        if (showBackButton) {
            Button backButton = new Button("Back");
            backButton.setPrefSize(200, 45);
            backButton.setStyle("-fx-background-color: #555555; -fx-text-fill: white; -fx-font-size: 16; -fx-background-radius: 10; -fx-cursor: hand;");
            backButton.setOnAction(e -> {
                ClientConnection.getInstance().removeListener(packetListener);
                LobbyMenu lobby = LobbyMenu.getInstance();
                if (lobby != null && GameSettings.currentUser != null) {
                    root.getChildren().setAll(lobby);
                } else {
                    root.getChildren().setAll(new MainMenu(root));
                }
            });

            content.getChildren().add(backButton);
        }
        this.getChildren().add(content);

        ClientConnection.getInstance().addListener(packetListener);
    }

    private Node createAvatar(String picData, String username) {
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
                iv.setFitWidth(100);
                iv.setFitHeight(100);
                iv.setPreserveRatio(true);
                Circle clip = new Circle(50, 50, 50);
                iv.setClip(clip);
                return iv;
            } catch (Exception e) {
                // Fallback to initial
            }
        }
        Circle circle = new Circle(50, Color.web("#769656"));
        Label label = new Label(username.substring(0, 1).toUpperCase());
        label.setFont(Font.font("Arial", FontWeight.BOLD, 40));
        label.setTextFill(Color.WHITE);
        return new StackPane(circle, label);
    }

    private void handlePacket(Packet packet) {
        if (packet instanceof AuthResponse res) {
            if (res.success()) {
                if (res.profile() == null) {
                    // Deleted
                    GameSettings.currentUser = null;
                    ClientConnection.getInstance().removeListener(packetListener);
                    root.getChildren().setAll(new MainMenu(root));
                } else {
                    // Updated
                    GameSettings.currentUser = res.profile();
                    this.profile = res.profile();
                    // Refresh UI
                    root.getChildren().setAll(new ProfileMenu(root, res.profile()));
                }
            }
        }
    }

    private HBox createStatRow(String label, String value, Color valueColor) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        
        Label lbl = new Label(label);
        lbl.setTextFill(Color.LIGHTGRAY);
        lbl.setFont(Font.font("Arial", 16));
        
        Label val = new Label(value);
        val.setTextFill(valueColor);
        val.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        row.getChildren().addAll(lbl, spacer, val);
        return row;
    }

    private String calculateWinRate(UserProfile profile) {
        int total = profile.wins() + profile.losses();
        if (total == 0) return "0%";
        double rate = (double) profile.wins() / total * 100;
        return String.format("%.1f%%", rate);
    }
}
