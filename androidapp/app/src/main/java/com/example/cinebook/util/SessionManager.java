package com.example.cinebook.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.cinebook.model.User;

/**
 * Cuva Basic Auth kredencijale i podatke o ulogovanom korisniku lokalno (SharedPreferences),
 * da se korisnik ne bi prijavljivao svaki put kad otvori aplikaciju.
 */
public class SessionManager {

    private static final String PREFS_NAME = "cinebook_session";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_ROLE = "role";

    private static final String KEY_USER_ID = "user_id";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(String username, String password, String role) {
        prefs.edit()
                .putString(KEY_USERNAME, username)
                .putString(KEY_PASSWORD, password)
                .putString(KEY_ROLE, role)
                .apply();
    }

    public void saveUserId(long userId) {
        prefs.edit().putLong(KEY_USER_ID, userId).apply();
    }

    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, -1);
    }

    public void updateRole(String role) {
        prefs.edit().putString(KEY_ROLE, role).apply();
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, null);
    }

    public String getPassword() {
        return prefs.getString(KEY_PASSWORD, null);
    }

    public String getRole() {
        return prefs.getString(KEY_ROLE, "USER");
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(getRole());
    }

    public boolean isLoggedIn() {
        return getUsername() != null && getPassword() != null;
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
