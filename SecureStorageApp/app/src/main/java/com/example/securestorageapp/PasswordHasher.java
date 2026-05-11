package com.example.securestorage.security;

import android.util.Log;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordHasher {
    private static final String TAG = "PasswordHasher";
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;

    /**
     * Hache un mot de passe en utilisant PBKDF2
     * @param password Mot de passe à hacher
     * @return Mot de passe haché au format Base64
     */
    public static String hashPassword(String password) {
        try {
            // Génération d'un sel aléatoire
            byte[] salt = new byte[SALT_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(salt);

            // Hachage du mot de passe
            byte[] hash = pbkdf2(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);

            // Concaténation du sel et du hash
            byte[] combined = new byte[salt.length + hash.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(hash, 0, combined, salt.length, hash.length);

            // Encodage en Base64
            return Base64.getEncoder().encodeToString(combined);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            Log.e(TAG, "Erreur lors du hachage du mot de passe: " + e.getMessage());
            return null;
        }
    }

    /**
     * Vérifie un mot de passe par rapport à un hash
     * @param password Mot de passe à vérifier
     * @param storedHash Hash stocké
     * @return true si le mot de passe correspond au hash
     */
    public static boolean verifyPassword(String password, String storedHash) {
        try {
            // Décodage du hash stocké
            byte[] combined = Base64.getDecoder().decode(storedHash);

            // Extraction du sel
            byte[] salt = new byte[SALT_LENGTH];
            System.arraycopy(combined, 0, salt, 0, salt.length);

            // Extraction du hash
            byte[] hash = new byte[combined.length - salt.length];
            System.arraycopy(combined, salt.length, hash, 0, hash.length);

            // Hachage du mot de passe fourni
            byte[] testHash = pbkdf2(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);

            // Comparaison des hash
            return Arrays.equals(hash, testHash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            Log.e(TAG, "Erreur lors de la vérification du mot de passe: " + e.getMessage());
            return false;
        }
    }

    /**
     * Implémentation de PBKDF2
     * @param password Mot de passe
     * @param salt Sel
     * @param iterations Nombre d'itérations
     * @param keyLength Longueur de la clé en bits
     * @return Hash du mot de passe
     */
    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLength)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
        return factory.generateSecret(spec).getEncoded();
    }
}