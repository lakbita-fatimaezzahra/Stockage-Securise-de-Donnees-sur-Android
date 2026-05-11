package com.example.securestorage.storage;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class InternalStorageManager {
    private static final String TAG = "InternalStorageManager";
    private final Context context;

    public InternalStorageManager(Context context) {
        this.context = context;
    }

    /**
     * Écrit des données dans un fichier du stockage interne
     * @param filename Nom du fichier
     * @param content Contenu à écrire
     * @return true si l'écriture a réussi
     */
    public boolean writeToInternalStorage(String filename, String content) {
        try (FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE)) {
            fos.write(content.getBytes());
            Log.d(TAG, "Fichier écrit avec succès: " + filename);
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Erreur lors de l'écriture du fichier: " + e.getMessage());
            return false;
        }
    }

    /**
     * Lit des données depuis un fichier du stockage interne
     * @param filename Nom du fichier
     * @return Contenu du fichier ou null en cas d'erreur
     */
    public String readFromInternalStorage(String filename) {
        try (FileInputStream fis = context.openFileInput(filename);
             InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader br = new BufferedReader(isr)) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }

            Log.d(TAG, "Fichier lu avec succès: " + filename);
            return sb.toString();
        } catch (IOException e) {
            Log.e(TAG, "Erreur lors de la lecture du fichier: " + e.getMessage());
            return null;
        }
    }

    /**
     * Supprime un fichier du stockage interne
     * @param filename Nom du fichier
     * @return true si la suppression a réussi
     */
    public boolean deleteFromInternalStorage(String filename) {
        boolean result = context.deleteFile(filename);
        if (result) {
            Log.d(TAG, "Fichier supprimé avec succès: " + filename);
        } else {
            Log.e(TAG, "Erreur lors de la suppression du fichier: " + filename);
        }
        return result;
    }

    /**
     * Liste tous les fichiers du stockage interne
     * @return Tableau des noms de fichiers
     */
    public String[] listInternalStorageFiles() {
        return context.fileList();
    }
}