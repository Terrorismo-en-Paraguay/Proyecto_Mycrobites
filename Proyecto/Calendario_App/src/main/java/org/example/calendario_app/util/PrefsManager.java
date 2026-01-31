package org.example.calendario_app.util;

import java.util.prefs.Preferences;

public class PrefsManager {
    private static final String KEY_EMAIL = "user_email";
    private static final String KEY_PASSWORD = "user_password"; // Note: Storing plain text password is not secure; for
                                                                // demo only.

    private static final Preferences prefs = Preferences.userNodeForPackage(PrefsManager.class);

    public static void saveCreds(String email, String password) {
        prefs.put(KEY_EMAIL, email);
        prefs.put(KEY_PASSWORD, password);
    }

    public static String getEmail() {
        return prefs.get(KEY_EMAIL, null);
    }

    public static String getPassword() {
        return prefs.get(KEY_PASSWORD, null);
    }

    public static void clear() {
        prefs.remove(KEY_EMAIL);
        prefs.remove(KEY_PASSWORD);
    }
}
