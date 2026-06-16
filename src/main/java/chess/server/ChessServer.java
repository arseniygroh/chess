package chess.server;

import chess.network.protocol.*;
import chess.model.PlayerColor;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChessServer {
    private final int port;
    private final AuthService authService = new AuthService();
    private final Map<String, ClientHandler> onlinePlayers = new ConcurrentHashMap<>();
    private final Map<String, ServerGameInstance> activeGames = new ConcurrentHashMap<>();

    public ChessServer(int port) {
        this.port = port;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Chess Server started on port " + port);
            while (true) {
                Socket socket = serverSocket.accept();
                new ClientHandler(socket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void broadcastLobbyUpdate() {
        List<UserProfile> profiles = new ArrayList<>();
        for (ClientHandler handler : onlinePlayers.values()) {
            profiles.add(handler.user.toProfile());
        }
        LobbyUpdate update = new LobbyUpdate(profiles);
        for (ClientHandler handler : onlinePlayers.values()) {
            handler.sendPacket(update);
        }
    }

    private class ClientHandler extends Thread {
        private final Socket socket;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private UserData user;
        private ServerGameInstance currentGame;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                out.flush(); // Send header
                in = new ObjectInputStream(socket.getInputStream());

                while (true) {
                    Object obj = in.readObject();
                    if (obj instanceof LoginRequest req) {
                        user = authService.login(req.username(), req.password());
                        if (user != null) {
                            onlinePlayers.put(user.username, this);
                            sendPacket(new AuthResponse(true, "Login successful", user.toProfile()));
                            broadcastLobbyUpdate();
                        } else {
                            sendPacket(new AuthResponse(false, "Invalid credentials", null));
                        }
                    } else if (obj instanceof RegisterRequest req) {
                        boolean success = authService.register(req.username(), req.password());
                        if (success) {
                            user = authService.login(req.username(), req.password());
                            onlinePlayers.put(user.username, this);
                            sendPacket(new AuthResponse(true, "Registration successful", user.toProfile()));
                            broadcastLobbyUpdate();
                        } else {
                            sendPacket(new AuthResponse(false, "Username already exists", null));
                        }
                    } else if (obj instanceof LobbyRequest) {
                        broadcastLobbyUpdate();
                    } else if (obj instanceof ProfileRequest) {
                        sendPacket(new AuthResponse(true, "Profile update", user.toProfile()));
                    } else if (obj instanceof LeaderboardRequest) {
                        sendPacket(new LeaderboardUpdate(authService.getAllProfiles()));
                    } else if (obj instanceof ChallengeRequest req) {
                        ClientHandler opponent = onlinePlayers.get(req.opponentName());
                        if (opponent != null) {
                            opponent.sendPacket(req);
                        }
                    } else if (obj instanceof ChallengeResponse res) {
                        if (res.accepted()) {
                            ServerGameInstance game = new ServerGameInstance(res.challengerName(), res.opponentName());
                            activeGames.put(game.getGameId(), game);
                            
                            ClientHandler challenger = onlinePlayers.get(res.challengerName());
                            ClientHandler opponent = onlinePlayers.get(res.opponentName());

                            if (challenger != null && opponent != null) {
                                challenger.currentGame = game;
                                opponent.currentGame = game;
                                challenger.sendPacket(new GameStarted(game.getGameId(), PlayerColor.WHITE, opponent.user.username, res.isFogOfWar()));
                                opponent.sendPacket(new GameStarted(game.getGameId(), PlayerColor.BLACK, challenger.user.username, res.isFogOfWar()));
                            }
                        } else {
                            ClientHandler challenger = onlinePlayers.get(res.challengerName());
                            if (challenger != null) challenger.sendPacket(res);
                        }
                    } else if (obj instanceof MoveRequest req) {
                        ServerGameInstance game = activeGames.get(req.gameId());
                        if (game != null) {
                            GameStateUpdate update = game.handleMoveRequest(req, user.username);
                            if (update != null) {
                                ClientHandler p1 = onlinePlayers.get(game.getWhitePlayer());
                                ClientHandler p2 = onlinePlayers.get(game.getBlackPlayer());
                                if (p1 != null) p1.sendPacket(update);
                                if (p2 != null) p2.sendPacket(update);

                                if (update.isGameOver()) {
                                    authService.updateStats(game.getWhitePlayer(), update.winner().equals(game.getWhitePlayer()));
                                    authService.updateStats(game.getBlackPlayer(), update.winner().equals(game.getBlackPlayer()));
                                    activeGames.remove(game.getGameId());
                                    broadcastLobbyUpdate();
                                }
                            }
                        }
                    } else if (obj instanceof UpdateProfileRequest req) {
                        if (user != null) {
                            authService.updateProfile(user.username, req.description(), req.profilePicture(), req.newPassword());
                            user = authService.login(user.username, req.newPassword() != null && !req.newPassword().isEmpty() ? req.newPassword() : user.password);
                            sendPacket(new AuthResponse(true, "Profile updated", user.toProfile()));
                            broadcastLobbyUpdate();
                        }
                    } else if (obj instanceof DeleteProfileRequest) {
                        if (user != null) {
                            authService.deleteAccount(user.username);
                            onlinePlayers.remove(user.username);
                            sendPacket(new AuthResponse(true, "Account deleted", null));
                            user = null;
                            broadcastLobbyUpdate();
                        }
                    } else if (obj instanceof ResignRequest req) {
                        ServerGameInstance game = activeGames.get(req.gameId());
                        if (game != null) {
                            String winner = user.username.equals(game.getWhitePlayer()) ? game.getBlackPlayer() : game.getWhitePlayer();
                            GameStateUpdate update = new GameStateUpdate(game.getGameId(), game.getBoardState().copy(), null, true, winner);
                            
                            ClientHandler p1 = onlinePlayers.get(game.getWhitePlayer());
                            ClientHandler p2 = onlinePlayers.get(game.getBlackPlayer());
                            if (p1 != null) p1.sendPacket(update);
                            if (p2 != null) p2.sendPacket(update);

                            authService.updateStats(game.getWhitePlayer(), winner.equals(game.getWhitePlayer()));
                            authService.updateStats(game.getBlackPlayer(), winner.equals(game.getBlackPlayer()));
                            activeGames.remove(game.getGameId());
                            broadcastLobbyUpdate();
                        }
                    }
                }
            } catch (Exception e) {
                if (user != null) {
                    onlinePlayers.remove(user.username);
                    broadcastLobbyUpdate();
                }
            } finally {
                try { socket.close(); } catch (IOException e) {}
            }
        }

        public void sendPacket(Packet packet) {
            try {
                out.writeObject(packet);
                out.flush();
                out.reset(); // Important for sending modified objects like BoardState
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        int port = 12345;
        String envPort = System.getenv("PORT");
        if (envPort != null) {
            port = Integer.parseInt(envPort);
        } else if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        new ChessServer(port).start();
    }
}
