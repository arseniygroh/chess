package chess.server;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class AuthService {
    private static final String ACCOUNTS_FILE = "accounts.dat";
    private Map<String, UserData> accounts = new HashMap<>();

    public AuthService() {
        loadAccounts();
    }

    public synchronized boolean register(String username, String password) {
        if (accounts.containsKey(username)) return false;
        accounts.put(username, new UserData(username, password));
        saveAccounts();
        return true;
    }

    public synchronized UserData login(String username, String password) {
        UserData data = accounts.get(username);
        if (data != null && data.password.equals(password)) {
            return data;
        }
        return null;
    }

    public synchronized void updateStats(String username, boolean won) {
        UserData data = accounts.get(username);
        if (data != null) {
            if (won) {
                data.wins++;
                data.elo += 20;
            } else {
                data.losses++;
                data.elo = Math.max(0, data.elo - 20);
            }
            saveAccounts();
        }
    }

    public synchronized void updateProfile(String username, String description, String profilePicture, String newPassword) {
        UserData data = accounts.get(username);
        if (data != null) {
            if (description != null) data.description = description;
            if (profilePicture != null) data.profilePicture = profilePicture;
            if (newPassword != null && !newPassword.isEmpty()) data.password = newPassword;
            saveAccounts();
        }
    }

    public synchronized void deleteAccount(String username) {
        accounts.remove(username);
        saveAccounts();
    }

    public synchronized java.util.List<chess.network.protocol.UserProfile> getAllProfiles() {
        return accounts.values().stream()
                .map(UserData::toProfile)
                .toList();
    }

    @SuppressWarnings("unchecked")
    private void loadAccounts() {
        File file = new File(ACCOUNTS_FILE);
        if (!file.exists()) return;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            accounts = (Map<String, UserData>) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveAccounts() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ACCOUNTS_FILE))) {
            oos.writeObject(accounts);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
