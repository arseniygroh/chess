package chess.network.client;

import chess.network.protocol.Packet;
import javafx.application.Platform;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ClientConnection {
    private static ClientConnection instance;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private boolean running = false;
    private final List<Consumer<Packet>> listeners = new ArrayList<>();

    private ClientConnection() {}

    public static synchronized ClientConnection getInstance() {
        if (instance == null) instance = new ClientConnection();
        return instance;
    }

    public void connect(String host, int port) throws IOException {
        if (running) return;
        socket = new Socket(host, port);
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush(); // Send header to prevent deadlock
        in = new ObjectInputStream(socket.getInputStream());
        running = true;
        startListening();
    }

    private void startListening() {
        new Thread(() -> {
            try {
                while (running) {
                    Object obj = in.readObject();
                    if (obj instanceof Packet packet) {
                        synchronized (listeners) {
                            for (Consumer<Packet> listener : new ArrayList<>(listeners)) {
                                Platform.runLater(() -> listener.accept(packet));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                if (running) {
                    e.printStackTrace();
                    stop();
                }
            }
        }).start();
    }

    public void addListener(Consumer<Packet> listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void removeListener(Consumer<Packet> listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    public void sendPacket(Packet packet) {
        try {
            out.writeObject(packet);
            out.flush();
            out.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        running = false;
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {}
    }
}
