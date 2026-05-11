package com.example.securestorage.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SupportFactory;

import java.security.SecureRandom;

@Database(entities = {User.class, Note.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "secure_app_db";
    private static AppDatabase instance;

    public abstract UserDao userDao();
    public abstract NoteDao noteDao();

    /**
     * Obtient une instance de la base de données chiffrée
     * @param context Contexte de l'application
     * @param passphrase Phrase secrète pour le chiffrement
     * @return Instance de la base de données
     */
    public static synchronized AppDatabase getInstance(Context context, char[] passphrase) {
        if (instance == null) {
            // Conversion de la phrase secrète en bytes pour SQLCipher
            byte[] passphraseBytes = SQLiteDatabase.getBytes(passphrase);

            // Création de la factory SQLCipher
            SupportFactory factory = new SupportFactory(passphraseBytes);

            // Construction de la base de données avec SQLCipher
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, DATABASE_NAME)
                    .openHelperFactory(factory)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }

    /**
     * Génère une phrase secrète aléatoire
     * @param length Longueur de la phrase secrète
     * @return Phrase secrète générée
     */
    public static char[] generateRandomPassphrase(int length) {
        String allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()";
        SecureRandom random = new SecureRandom();
        char[] passphrase = new char[length];

        for (int i = 0; i < length; i++) {
            passphrase[i] = allowedChars.charAt(random.nextInt(allowedChars.length()));
        }

        return passphrase;
    }
}