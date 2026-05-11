package com.example.securestorage.storage;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class ExternalStorageManager {
    private static final String TAG = "ExternalStorageManager";
    private final Context context;

    public ExternalStorageManager(Context context) {
        this.context = context;
    }

    /**
     * Vérifie si le stockage externe est disponible en lecture/écriture
     * @return true si le stockage externe est disponible
     */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * Vérifie si le stockage externe est au moins disponible en lecture
     * @return true si le stockage externe est disponible en lecture
     */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    /**
     * Obtient le répertoire de l'application dans le stockage externe
     * @param type Type de répertoire (null pour le répertoire racine)
     * @return Fichier représentant le répertoire
     */
    public File getExternalStorageDir(String type) {
        // Pour Android 10 (API 29) et supérieur, nous utilisons le répertoire spécifique à l'application
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return context.getExternalFilesDir(type);
        } else {
            // Pour les versions antérieures, nous pouvons utiliser le répertoire public
            // Mais c'est déconseillé et nécessite des permissions
            if (type == null) {
                return Environment.getExternalStorageDirectory();
            } else {
                return Environment.getExternalStoragePublicDirectory(type);
            }
        }
    }

    /**
     * Écrit des données dans un fichier du stockage externe
     * @param filename Nom du fichier
     * @param content Contenu à écrire
     * @param type Type de répertoire (null pour le répertoire racine)
     * @return true si l'écriture a réussi
     */
    public boolean writeToExternalStorage(String filename, String content, String type) {
        if (!isExternalStorageWritable()) {
            Log.e(TAG, "Le stockage externe n'est pas accessible en écriture");
            return false;
        }

        File dir = getExternalStorageDir(type);
        if (!dir.exists() && !dir.mkdirs()) {
            Log.e(TAG, "Impossible de créer le répertoire: " + dir.getAbsolutePath());
            return false;
        }

        File file = new File(dir, filename);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(content.getBytes());
            Log.d(TAG, "Fichier écrit avec succès: " + file.getAbsolutePath());
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Erreur lors de l'écriture du fichier: " + e.getMessage());
            return false;
        }
    }

    /**
     * Lit des données depuis un fichier du stockage externe
     * @param filename Nom du fichier
     * @param type Type de répertoire (null pour le répertoire racine)
     * @return Contenu du fichier ou null en cas d'erreur
     */
    public String readFromExternalStorage(String filename, String type) {
        if (!isExternalStorageReadable()) {
            Log.e(TAG, "Le stockage externe n'est pas accessible en lecture");
            return null;
        }

        File dir = getExternalStorageDir(type);
        File file = new File(dir, filename);

        try (FileInputStream fis = new FileInputStream(file);
             InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader br = new BufferedReader(isr)) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }

            Log.d(TAG, "Fichier lu avec succès: " + file.getAbsolutePath());
            return sb.toString();
        } catch (IOException e) {
            Log.e(TAG, "Erreur lors de la lecture du fichier: " + e.getMessage());
            return null;
        }
    }

    /**
     * Supprime un fichier du stockage externe
     * @param filename Nom du fichier
     * @param type Type de répertoire (null pour le répertoire racine)
     * @return true si la suppression a réussi
     */
    public boolean deleteFromExternalStorage(String filename, String type) {
        File dir = getExternalStorageDir(type);
        File file = new File(dir, filename);
        boolean result = file.delete();
        if (result) {
            Log.d(TAG, "Fichier supprimé avec succès: " + file.getAbsolutePath());
        } else {
            Log.e(TAG, "Erreur lors de la suppression du fichier: " + file.getAbsolutePath());
        }
        return result;
    }

    /**
     * Liste tous les fichiers du répertoire spécifié dans le stockage externe
     * @param type Type de répertoire (null pour le répertoire racine)
     * @return Tableau des noms de fichiers
     */
    public String[] listExternalStorageFiles(String type) {
        File dir = getExternalStorageDir(type);
        if (dir.exists() && dir.isDirectory()) {
            return dir.list();
        }
        return new String[0];
    }
}