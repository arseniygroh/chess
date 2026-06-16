package chess.ui;

import chess.network.client.ClientConnection;
import chess.network.protocol.*;
import chess.util.CredentialsManager;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.IOException;
import java.util.function.Consumer;

public class LoginMenu extends StackPane {
    private final StackPane root;
    private final Node backScreen;
    private final TextField usernameField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final CheckBox rememberMe = new CheckBox("Remember Me");
    private final Label statusLabel = new Label();
    private final Consumer<Packet> packetListener = this::handlePacket;
    private boolean redirectToLobby = true;

    public LoginMenu(StackPane root) {
        this(root, true, null);
    }

    public LoginMenu(StackPane root, boolean redirectToLobby) {
        this(root, redirectToLobby, null);
    }

    public LoginMenu(StackPane root, boolean redirectToLobby, Node backScreen) {
        this.root = root;
        this.redirectToLobby = redirectToLobby;
        this.backScreen = backScreen;
        
        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setStyle("-fx-background-color: #2b2b2b; -fx-padding: 50;");

        Label title = new Label("Chess Online");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setTextFill(Color.WHITE);

        usernameField.setPromptText("Username");
        usernameField.setMaxWidth(250);
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(250);

        rememberMe.setTextFill(Color.WHITE);
        rememberMe.setFont(Font.font("Arial", 14));

        CredentialsManager.SavedCredentials saved = CredentialsManager.loadCredentials();
        if (saved.remember()) {
            usernameField.setText(saved.username());
            passwordField.setText(saved.password());
            rememberMe.setSelected(true);
        }

        Button loginButton = createButton("Login");
        Button registerButton = createButton("Register");
        Button backButton = createButton("Back");
        backButton.setStyle("-fx-background-color: #555555; -fx-text-fill: white; -fx-font-size: 16;");

        statusLabel.setTextFill(Color.LIGHTGRAY);

        loginButton.setOnAction(e -> {
            String user = usernameField.getText();
            String pass = passwordField.getText();
            if (user.isEmpty() || pass.isEmpty()) {
                statusLabel.setText("Please fill all fields");
                return;
            }
            if (tryConnect()) {
                ClientConnection.getInstance().sendPacket(new LoginRequest(user, pass));
            }
        });

        registerButton.setOnAction(e -> {
            String user = usernameField.getText();
            String pass = passwordField.getText();
            if (user.isEmpty() || pass.isEmpty()) {
                statusLabel.setText("Please fill all fields");
                return;
            }
            if (tryConnect()) {
                ClientConnection.getInstance().sendPacket(new RegisterRequest(user, pass));
            }
        });

        backButton.setOnAction(e -> {
            ClientConnection.getInstance().removeListener(packetListener);
            if (backScreen != null) {
                root.getChildren().setAll(backScreen);
            } else {
                root.getChildren().setAll(new MainMenu(root));
            }
        });

        ClientConnection.getInstance().addListener(packetListener);

        content.getChildren().addAll(title, usernameField, passwordField, rememberMe, loginButton, registerButton, backButton, statusLabel);
        this.getChildren().add(content);
    }

    private boolean tryConnect() {
        try {
            ClientConnection.getInstance().connect(chess.GameSettings.serverAddress, chess.GameSettings.port);
            return true;
        } catch (IOException e) {
            statusLabel.setText("Connection failed: " + e.getMessage());
            return false;
        }
    }

    private void handlePacket(Packet packet) {
        if (packet instanceof AuthResponse res) {
            if (res.success()) {
                if (res.profile() != null) {
                    // Login successful
                    CredentialsManager.saveCredentials(
                            usernameField.getText(),
                            passwordField.getText(),
                            rememberMe.isSelected()
                    );
                    chess.GameSettings.currentUser = res.profile();
                    ClientConnection.getInstance().removeListener(packetListener);
                    if (redirectToLobby) {
                        root.getChildren().setAll(new LobbyMenu(root, res.profile()));
                    } else {
                        root.getChildren().setAll(new MainMenu(root));
                    }
                } else {
                    statusLabel.setText(res.message());
                }
            } else {
                statusLabel.setText(res.message());
            }
        }
    }

    private Button createButton(String text) {
        Button button = new Button(text);
        button.setPrefSize(250, 45);
        button.setStyle("-fx-background-color: #769656; -fx-text-fill: white; -fx-font-size: 18; -fx-background-radius: 5; -fx-cursor: hand;");
        return button;
    }
}
