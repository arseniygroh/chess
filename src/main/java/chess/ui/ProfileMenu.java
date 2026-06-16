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
import javafx.stage.FileChooser;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.Base64;
import java.util.function.Consumer;

public class ProfileMenu extends StackPane {
    private final StackPane root;
    private UserProfile profile;
    private final Consumer<Packet> packetListener = this::handlePacket;
    private String selectedPicData;
    private boolean returnToMainMenu = false;

    public ProfileMenu(StackPane root, UserProfile profile) {
        this(root, profile, true);
    }

    public ProfileMenu(StackPane root, UserProfile profile, boolean showBackButton) {
        this(root, profile, showBackButton, false);
    }

    public ProfileMenu(StackPane root, UserProfile profile, boolean showBackButton, boolean compact) {
        this(root, profile, showBackButton, compact, false);
    }

    public ProfileMenu(StackPane root, UserProfile profile, boolean showBackButton, boolean compact, boolean returnToMainMenu) {
        this.root = root;
        this.profile = profile;
        this.selectedPicData = profile.profilePicture();
        this.returnToMainMenu = returnToMainMenu;

        boolean isMyProfile = GameSettings.currentUser != null && 
                             GameSettings.currentUser.username().equals(profile.username());

        VBox content = new VBox(compact ? 10 : 20);
        content.setAlignment(Pos.CENTER);
        content.setStyle("-fx-background-color: #2b2b2b; -fx-padding: " + (compact ? "15" : "30") + ";");

        // Avatar
        double avatarSize = compact ? 35 : 50;
        Node avatarNode = createAvatar(profile.profilePicture(), profile.username(), avatarSize);
        
        Label usernameLabel = new Label(profile.username());
        usernameLabel.setFont(Font.font("Arial", FontWeight.BOLD, compact ? 22 : 32));
        usernameLabel.setTextFill(Color.WHITE);

        VBox statsBox = new VBox(compact ? 5 : 10);
        statsBox.setAlignment(Pos.CENTER);
        statsBox.setStyle("-fx-background-color: #333333; -fx-padding: " + (compact ? "10" : "20") + "; -fx-background-radius: 10;");
        statsBox.setMaxWidth(compact ? 250 : 350);

        statsBox.getChildren().addAll(
            createStatRow("Elo Rating:", String.valueOf(profile.elo()), Color.LIGHTGREEN, compact),
            createStatRow("Wins:", String.valueOf(profile.wins()), Color.WHITE, compact),
            createStatRow("Losses:", String.valueOf(profile.losses()), Color.WHITE, compact),
            createStatRow("Win Rate:", calculateWinRate(profile), Color.LIGHTBLUE, compact)
        );

        Label descLabel = new Label("Description:");
        descLabel.setTextFill(Color.LIGHTGRAY);
        descLabel.setFont(Font.font(compact ? 12 : 14));
        
        TextArea descArea = new TextArea(profile.description());
        descArea.setEditable(false);
        descArea.setWrapText(true);
        descArea.setPrefHeight(compact ? 60 : 100);
        descArea.setMaxWidth(compact ? 250 : 350);
        descArea.setStyle("-fx-control-inner-background: #333333; -fx-text-fill: white; -fx-font-size: " + (compact ? "12" : "14") + ";");

        content.getChildren().addAll(avatarNode, usernameLabel, statsBox, descLabel, descArea);

        if (isMyProfile) {
            descArea.setEditable(true);
            
            TextField passField = new TextField();
            passField.setPromptText("New Password");
            passField.setMaxWidth(compact ? 250 : 350);

            Label picStatusLabel = new Label();
            picStatusLabel.setTextFill(Color.LIGHTBLUE);
            picStatusLabel.setFont(Font.font("Arial", 12));
            updatePicStatusLabel(picStatusLabel);

            Button fileBtn = new Button("Upload File");
            fileBtn.setStyle("-fx-background-color: #4a4a4a; -fx-text-fill: white;");
            fileBtn.setOnAction(e -> {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Select Profile Picture");
                fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
                );
                File selectedFile = fileChooser.showOpenDialog(getScene().getWindow());
                if (selectedFile != null) {
                    try {
                        byte[] fileContent = Files.readAllBytes(selectedFile.toPath());
                        selectedPicData = Base64.getEncoder().encodeToString(fileContent);
                        updatePicStatusLabel(picStatusLabel);
                    } catch (Exception ex) {
                        new Alert(Alert.AlertType.ERROR, "Failed to load image: " + ex.getMessage()).show();
                    }
                }
            });

            Button urlBtn = new Button("Set URL");
            urlBtn.setStyle("-fx-background-color: #4a4a4a; -fx-text-fill: white;");
            urlBtn.setOnAction(e -> {
                TextInputDialog dialog = new TextInputDialog(selectedPicData != null && selectedPicData.startsWith("http") ? selectedPicData : "");
                dialog.setTitle("Profile Picture URL");
                dialog.setHeaderText("Enter image URL:");
                dialog.showAndWait().ifPresent(url -> {
                    if (url.trim().isEmpty()) {
                        selectedPicData = "";
                    } else {
                        selectedPicData = url.trim();
                    }
                    updatePicStatusLabel(picStatusLabel);
                });
            });

            Button clearBtn = new Button("Clear");
            clearBtn.setStyle("-fx-background-color: #603030; -fx-text-fill: white;");
            clearBtn.setOnAction(e -> {
                selectedPicData = "";
                updatePicStatusLabel(picStatusLabel);
            });

            HBox picActions = new HBox(10, fileBtn, urlBtn, clearBtn);
            picActions.setAlignment(Pos.CENTER);

            Button saveBtn = new Button("Save Changes");
            saveBtn.setStyle("-fx-background-color: #769656; -fx-text-fill: white;");
            saveBtn.setOnAction(e -> {
                ClientConnection.getInstance().sendPacket(new UpdateProfileRequest(
                    descArea.getText(),
                    selectedPicData,
                    passField.getText()
                ));
            });

            Button deleteBtn = new Button("Delete Account");
            deleteBtn.setStyle("-fx-background-color: #a04040; -fx-text-fill: white;");
            deleteBtn.setOnAction(e -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete account?", ButtonType.YES, ButtonType.NO);
                alert.showAndWait().ifPresent(type -> {
                    if (type == ButtonType.YES) {
                        ClientConnection.getInstance().sendPacket(new DeleteProfileRequest());
                    }
                });
            });

            content.getChildren().addAll(passField, picStatusLabel, picActions, saveBtn, deleteBtn);
        }

        if (showBackButton) {
            Button backButton = new Button("Back");
            backButton.setPrefSize(200, 45);
            backButton.setStyle("-fx-background-color: #555555; -fx-text-fill: white; -fx-font-size: 16; -fx-background-radius: 10; -fx-cursor: hand;");
            backButton.setOnAction(e -> {
                ClientConnection.getInstance().removeListener(packetListener);
                if (returnToMainMenu) {
                    root.getChildren().setAll(new MainMenu(root));
                } else {
                    LobbyMenu lobby = LobbyMenu.getInstance();
                    if (lobby != null && GameSettings.currentUser != null) {
                        root.getChildren().setAll(lobby);
                    } else {
                        root.getChildren().setAll(new MainMenu(root));
                    }
                }
            });

            content.getChildren().add(backButton);
        }
        this.getChildren().add(content);

        ClientConnection.getInstance().addListener(packetListener);
    }

    private void updatePicStatusLabel(Label label) {
        if (selectedPicData == null || selectedPicData.trim().isEmpty() || selectedPicData.equals("null")) {
            label.setText("No images have been added");
        } else if (selectedPicData.startsWith("http")) {
            label.setText("Image linked via URL");
        } else {
            label.setText("Custom image uploaded");
        }
    }

    private Node createAvatar(String picData, String username, double radius) {
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
                iv.setFitWidth(radius * 2);
                iv.setFitHeight(radius * 2);
                iv.setPreserveRatio(true);
                Circle clip = new Circle(radius, radius, radius);
                iv.setClip(clip);
                return iv;
            } catch (Exception e) {
                // Fallback to initial
            }
        }
        Circle circle = new Circle(radius, Color.web("#769656"));
        Label label = new Label(username.substring(0, 1).toUpperCase());
        label.setFont(Font.font("Arial", FontWeight.BOLD, radius * 0.8));
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
                    
                    // Close the dialog/popup if we are in one
                    if (getScene() != null && getScene().getWindow() != null) {
                        javafx.stage.Window window = getScene().getWindow();
                        if (window instanceof javafx.stage.Stage stage) {
                            // Check if this is a secondary stage (like a dialog)
                            if (stage != root.getScene().getWindow()) {
                                stage.close();
                            }
                        }
                    }

                    root.getChildren().setAll(new MainMenu(root));
                } else {
                    // Updated
                    GameSettings.currentUser = res.profile();
                    this.profile = res.profile();
                    
                    // If we are in a dialog, we might want to just close it or refresh content
                    // For now, let's refresh the whole view ONLY if it's the main profile page
                    if (getScene() != null && getScene().getWindow() != null) {
                        javafx.stage.Window window = getScene().getWindow();
                        if (window instanceof javafx.stage.Stage stage && stage == root.getScene().getWindow()) {
                             // Preserve flags when refreshing
                             boolean showBack = false;
                             for (Node node : ((VBox)this.getChildren().get(0)).getChildren()) {
                                 if (node instanceof Button b && "Back".equals(b.getText())) {
                                     showBack = true;
                                     break;
                                 }
                             }
                             boolean isCompact = ((VBox)this.getChildren().get(0)).getSpacing() == 10;
                             ClientConnection.getInstance().removeListener(packetListener);
                             root.getChildren().setAll(new ProfileMenu(root, res.profile(), showBack, isCompact, this.returnToMainMenu));
                        } else {
                            // It's a popup, just close it to reflect changes in lobby/leaderboard
                            ((javafx.stage.Stage)window).close();
                        }
                    }
                }
            }
        }
    }

    private HBox createStatRow(String label, String value, Color valueColor, boolean compact) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        
        Label lbl = new Label(label);
        lbl.setTextFill(Color.LIGHTGRAY);
        lbl.setFont(Font.font("Arial", compact ? 13 : 16));
        
        Label val = new Label(value);
        val.setTextFill(valueColor);
        val.setFont(Font.font("Arial", FontWeight.BOLD, compact ? 14 : 18));
        
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
