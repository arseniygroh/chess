package chess.util;

import java.io.*;
import java.util.Properties;

public class CredentialsManager {
    private static final String FILE_NAME = "credentials.properties";
    private static final String KEY_USER = "username";
    private static final String KEY_PASS = "password";
    private static final String KEY_REMEMBER = "remember";

    public static void saveCredentials(String username, String password, boolean remember) {
        Properties props = new Properties();
        if (remember) {
            props.setProperty(KEY_USER, username);
            props.setProperty(KEY_PASS, password);
            props.setProperty(KEY_REMEMBER, "true");
        } else {
            props.setProperty(KEY_REMEMBER, "false");
        }

        try (OutputStream output = new FileOutputStream(FILE_NAME)) {
            props.store(output, null);
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    public static SavedCredentials loadCredentials() {
        Properties props = new Properties();
        try (InputStream input = new FileInputStream(FILE_NAME)) {
            props.load(input);
            boolean remember = Boolean.parseBoolean(props.getProperty(KEY_REMEMBER, "false"));
            if (remember) {
                return new SavedCredentials(
                    props.getProperty(KEY_USER, ""),
                    props.getProperty(KEY_PASS, ""),
                    true
                );
            }
        } catch (IOException io) {
            // File might not exist yet
        }
        return new SavedCredentials("", "", false);
    }

    public static void clearCredentials() {
        saveCredentials("", "", false);
    }

    public record SavedCredentials(String username, String password, boolean remember) {}
}
