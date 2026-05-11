package com.example.securestorage.database;

import android.content.Context;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DatabaseManager {
    private static final String TAG = "DatabaseManager";
    private static final String PASSPHRASE_PREF_NAME = "secure_db_passphrase";
    private static final String PASSPHRASE_KEY = "db_passphrase";
    private static final int PASSPHRASE_LENGTH = 32;

    private final Context context;
    private final Executor executor;
    private AppDatabase database;
    private char[] passphrase;

    public DatabaseManager(Context context) {
        this.context = context;
        this.executor = Executors.newSingleThreadExecutor();
        initializeDatabase();
    }

    /**
     * Initialise la base de données avec une phrase secrète
     */
    private void initializeDatabase() {
        try {
            // Récupération ou génération de la phrase secrète
            passphrase = getOrCreatePassphrase();

            // Initialisation de la base de données
            database = AppDatabase.getInstance(context, passphrase);

            Log.d(TAG, "Base de données initialisée avec succès");
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'initialisation de la base de données: " + e.getMessage());
        }
    }

    /**
     * Récupère la phrase secrète existante ou en crée une nouvelle
     * @return Phrase secrète
     */
    private char[] getOrCreatePassphrase() throws GeneralSecurityException, IOException {
        // Création d'une MasterKey pour EncryptedSharedPreferences
        MasterKey masterKey = new MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();

        // Création des SharedPreferences chiffrées
        EncryptedSharedPreferences encryptedPrefs = (EncryptedSharedPreferences) EncryptedSharedPreferences.create(
                context,
                PASSPHRASE_PREF_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );

        // Récupération de la phrase secrète existante ou génération d'une nouvelle
        String storedPassphrase = encryptedPrefs.getString(PASSPHRASE_KEY, null);
        if (storedPassphrase == null) {
            // Génération d'une nouvelle phrase secrète
            char[] newPassphrase = AppDatabase.generateRandomPassphrase(PASSPHRASE_LENGTH);
            storedPassphrase = new String(newPassphrase);

            // Stockage de la phrase secrète
            encryptedPrefs.edit().putString(PASSPHRASE_KEY, storedPassphrase).apply();

            Log.d(TAG, "Nouvelle phrase secrète générée et stockée");
            return newPassphrase;
        } else {
            Log.d(TAG, "Phrase secrète existante récupérée");
            return storedPassphrase.toCharArray();
        }
    }

    /**
     * Insère un utilisateur dans la base de données
     * @param user Utilisateur à insérer
     * @param callback Callback pour le résultat
     */
    public void insertUser(User user, DatabaseCallback<Long> callback) {
        executor.execute(() -> {
            try {
                long userId = database.userDao().insert(user);
                callback.onSuccess(userId);
            } catch (Exception e) {
                Log.e(TAG, "Erreur lors de l'insertion de l'utilisateur: " + e.getMessage());
                callback.onError(e);
            }
        });
    }

    /**
     * Récupère un utilisateur par son nom d'utilisateur
     * @param username Nom d'utilisateur
     * @param callback Callback pour le résultat
     */
    public void getUserByUsername(String username, DatabaseCallback<User> callback) {
        executor.execute(() -> {
            try {
                User user = database.userDao().getUserByUsername(username);
                callback.onSuccess(user);
            } catch (Exception e) {
                Log.e(TAG, "Erreur lors de la récupération de l'utilisateur: " + e.getMessage());
                callback.onError(e);
            }
        });
    }

    /**
     * Récupère tous les utilisateurs
     * @param callback Callback pour le résultat
     */
    public void getAllUsers(DatabaseCallback<List<User>> callback) {
        executor.execute(() -> {
            try {
                List<User> users = database.userDao().getAllUsers();
                callback.onSuccess(users);
            } catch (Exception e) {
                Log.e(TAG, "Erreur lors de la récupération des utilisateurs: " + e.getMessage());
                callback.onError(e);
            }
        });
    }

    /**
     * Insère une note dans la base de données
     * @param note Note à insérer
     * @param callback Callback pour le résultat
     */
    public void insertNote(Note note, DatabaseCallback<Long> callback) {
        executor.execute(() -> {
            try {
                long noteId = database.noteDao().insert(note);
                callback.onSuccess(noteId);
            } catch (Exception e) {
                Log.e(TAG, "Erreur lors de l'insertion de la note: " + e.getMessage());
                callback.onError(e);
            }
        });
    }

    /**
     * Récupère toutes les notes d'un utilisateur
     * @param userId ID de l'utilisateur
     * @param callback Callback pour le résultat
     */
    public void getNotesByUserId(int userId, DatabaseCallback<List<Note>> callback) {
        executor.execute(() -> {
            try {
                List<Note> notes = database.noteDao().getNotesByUserId(userId);
                callback.onSuccess(notes);
            } catch (Exception e) {
                Log.e(TAG, "Erreur lors de la récupération des notes: " + e.getMessage());
                callback.onError(e);
            }
        });
    }

    /**
     * Interface de callback pour les opérations de base de données
     * @param <T> Type de résultat
     */
    public interface DatabaseCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }
}