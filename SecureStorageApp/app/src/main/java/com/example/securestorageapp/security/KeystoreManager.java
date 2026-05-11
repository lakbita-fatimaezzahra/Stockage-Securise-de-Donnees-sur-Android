package com.example.securestorage.security;

import android.content.Context;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;

import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class KeystoreManager {
    private static final String TAG = "KeystoreManager";
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final String STRONGBOX_KEY_ALIAS = "secure_strongbox_key";

    private final Context context;

    public KeystoreManager(Context context) {
        this.context = context;
    }

    /**
     * Crée une MasterKey protégée par StrongBox si disponible
     * @return MasterKey créée
     */
    public MasterKey createStrongBoxMasterKey() throws GeneralSecurityException, IOException {
        // Vérification de la disponibilité de StrongBox
        boolean isStrongBoxAvailable = isStrongBoxAvailable();

        // Création de la MasterKey
        MasterKey.Builder builder = new MasterKey.Builder(context, STRONGBOX_KEY_ALIAS)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .setUserAuthenticationRequired(false);

        // Utilisation de StrongBox si disponible
        if (isStrongBoxAvailable) {
            builder.setIsStrongBoxBacked(true);
            Log.d(TAG, "Création d'une MasterKey protégée par StrongBox");
        } else {
            Log.d(TAG, "StrongBox non disponible, création d'une MasterKey standard");
        }

        return builder.build();
    }

    /**
     * Vérifie si StrongBox est disponible sur l'appareil
     * @return true si StrongBox est disponible
     */
    public boolean isStrongBoxAvailable() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            return false;
        }

        try {
            KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
                    "strongbox_test_key",
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .setIsStrongBoxBacked(true)
                    .build();

            KeyGenerator keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    ANDROID_KEYSTORE);
            keyGenerator.init(spec);
            keyGenerator.generateKey();

            // Suppression de la clé de test
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null);
            keyStore.deleteEntry("strongbox_test_key");

            return true;
        } catch (Exception e) {
            Log.d(TAG, "StrongBox non disponible: " + e.getMessage());
            return false;
        }
    }

    /**
     * Génère une clé AES-256 dans le Keystore Android
     * @param alias Alias de la clé
     * @return true si la génération a réussi
     */
    public boolean generateAesKey(String alias) {
        try {
            KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
                    alias,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build();

            KeyGenerator keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    ANDROID_KEYSTORE);
            keyGenerator.init(spec);
            keyGenerator.generateKey();

            Log.d(TAG, "Clé AES générée avec succès: " + alias);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la génération de la clé AES: " + e.getMessage());
            return false;
        }
    }

    /**
     * Récupère une clé du Keystore Android
     * @param alias Alias de la clé
     * @return Clé secrète ou null en cas d'erreur
     */
    public SecretKey getKey(String alias) {
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null);

            KeyStore.SecretKeyEntry entry = (KeyStore.SecretKeyEntry) keyStore.getEntry(
                    alias,
                    null);

            if (entry != null) {
                Log.d(TAG, "Clé récupérée avec succès: " + alias);
                return entry.getSecretKey();
            } else {
                Log.e(TAG, "Clé non trouvée: " + alias);
                return null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la récupération de la clé: " + e.getMessage());
            return null;
        }
    }

    /**
     * Supprime une clé du Keystore Android
     * @param alias Alias de la clé
     * @return true si la suppression a réussi
     */
    public boolean deleteKey(String alias) {
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null);
            keyStore.deleteEntry(alias);

            Log.d(TAG, "Clé supprimée avec succès: " + alias);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la suppression de la clé: " + e.getMessage());
            return false;
        }
    }
}