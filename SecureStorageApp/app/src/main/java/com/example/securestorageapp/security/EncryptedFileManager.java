package com.example.securestorage.security;

import android.content.Context;
import android.util.Log;

import androidx.security.crypto.EncryptedFile;
import androidx.security.crypto.MasterKey;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

public class EncryptedFileManager {
    private static final String TAG = "EncryptedFileManager";
    private final Context context;
    private MasterKey masterKey;

    public EncryptedFileManager(Context context) {
        this.context = context;
        initializeMasterKey();
    }

    /**
     * Initialise la clé maître pour le chiffrement
     */
    private void initializeMasterKey() {
        try {
            // Création d'une MasterKey avec AES-256-GCM
            masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .setUserAuthenticationRequired(false)  // Ne pas exiger l'authentification de l'utilisateur
                    .build();

            Log.d(TAG, "MasterKey initialisée avec succès");
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "Erreur lors de l'initialisation de la MasterKey: " + e.getMessage());
        }
    }

    /**
     * Écrit des données dans un fichier chiffré
     * @param filename Nom du fichier
     * @param content Contenu à écrire
     * @return true si l'écriture a réussi
     */
    public boolean writeToEncryptedFile(String filename, String content) {
        if (masterKey == null) {
            Log.e(TAG, "MasterKey non initialisée");
            return false;
        }

        try {
            // Création du fichier
            File file = new File(context.getFilesDir(), filename);

            // Création du fichier chiffré
            EncryptedFile encryptedFile = new EncryptedFile.Builder(
                    context,
                    file,
                    masterKey,
                    EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build();

            // Écriture des données
            try (OutputStream outputStream = encryptedFile.openFileOutput()) {
                outputStream.write(content.getBytes(StandardCharsets.UTF_8));
            }

            Log.d(TAG, "Fichier chiffré écrit avec succès: " + filename);
            return true;
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "Erreur lors de l'écriture du fichier chiffré: " + e.getMessage());
            return false;
        }
    }

    /**
     * Lit des données depuis un fichier chiffré
     * @param filename Nom du fichier
     * @return Contenu du fichier ou null en cas d'erreur
     */
    public String readFromEncryptedFile(String filename) {
        if (masterKey == null) {
            Log.e(TAG, "MasterKey non initialisée");
            return null;
        }

        try {
            // Création du fichier
            File file = new File(context.getFilesDir(), filename);

            if (!file.exists()) {
                Log.e(TAG, "Le fichier n'existe pas: " + filename);
                return null;
            }

            // Création du fichier chiffré
            EncryptedFile encryptedFile = new EncryptedFile.Builder(
                    context,
                    file,
                    masterKey,
                    EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build();

            // Lecture des données
            try (InputStream inputStream = encryptedFile.openFileInput();
                 ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }

                String content = byteArrayOutputStream.toString(StandardCharsets.UTF_8.name());
                Log.d(TAG, "Fichier chiffré lu avec succès: " + filename);
                return content;
            }
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "Erreur lors de la lecture du fichier chiffré: " + e.getMessage());
            return null;
        }
    }

    /**
     * Supprime un fichier chiffré
     * @param filename Nom du fichier
     * @return true si la suppression a réussi
     */
    public boolean deleteEncryptedFile(String filename) {
        File file = new File(context.getFilesDir(), filename);
        boolean result = file.delete();

        if (result) {
            Log.d(TAG, "Fichier chiffré supprimé avec succès: " + filename);
        } else {
            Log.e(TAG, "Erreur lors de la suppression du fichier chiffré: " + filename);
        }

        return result;
    }

    /**
     * Liste tous les fichiers du répertoire
     * @return Tableau des noms de fichiers
     */
    public String[] listFiles() {
        return context.fileList();
    }
}