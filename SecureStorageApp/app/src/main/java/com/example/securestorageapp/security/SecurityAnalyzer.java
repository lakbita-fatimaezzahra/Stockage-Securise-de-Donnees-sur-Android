package com.example.securestorage.security;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class SecurityAnalyzer {
    private static final String TAG = "SecurityAnalyzer";
    private final Context context;

    public SecurityAnalyzer(Context context) {
        this.context = context;
    }

    /**
     * Analyse les risques de sécurité de l'application
     * @return Liste des risques détectés
     */
    public List<SecurityRisk> analyzeSecurityRisks() {
        List<SecurityRisk> risks = new ArrayList<>();

        // Vérification des sauvegardes
        checkBackupSettings(risks);

        // Vérification de StrongBox
        checkStrongBoxAvailability(risks);

        // Vérification du débogage
        checkDebuggable(risks);

        // Vérification de l'émulateur
        checkEmulator(risks);

        return risks;
    }

    /**
     * Vérifie les paramètres de sauvegarde
     * @param risks Liste des risques à mettre à jour
     */
    private void checkBackupSettings(List<SecurityRisk> risks) {
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), 0);

            boolean allowBackup = (appInfo.flags & ApplicationInfo.FLAG_ALLOW_BACKUP) != 0;
            if (allowBackup) {
                risks.add(new SecurityRisk(
                        "Sauvegardes activées",
                        "Les sauvegardes sont activées, ce qui pourrait permettre l'extraction de données sensibles.",
                        "Désactivez les sauvegardes en définissant android:allowBackup=\"false\" dans le manifeste.",
                        SecurityRisk.Severity.HIGH
                ));
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Erreur lors de la vérification des paramètres de sauvegarde: " + e.getMessage());
        }
    }

    /**
     * Vérifie la disponibilité de StrongBox
     * @param risks Liste des risques à mettre à jour
     */
    private void checkStrongBoxAvailability(List<SecurityRisk> risks) {
        KeystoreManager keystoreManager = new KeystoreManager(context);
        boolean strongBoxAvailable = keystoreManager.isStrongBoxAvailable();

        if (!strongBoxAvailable) {
            risks.add(new SecurityRisk(
                    "StrongBox non disponible",
                    "StrongBox n'est pas disponible sur cet appareil, ce qui réduit la sécurité des clés.",
                    "Utilisez des méthodes alternatives comme la dérivation de clés avec PBKDF2.",
                    SecurityRisk.Severity.MEDIUM
            ));
        }
    }

    /**
     * Vérifie si l'application est déboggable
     * @param risks Liste des risques à mettre à jour
     */
    private void checkDebuggable(List<SecurityRisk> risks) {
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), 0);

            boolean debuggable = (appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
            if (debuggable) {
                risks.add(new SecurityRisk(
                        "Application déboggable",
                        "L'application est déboggable, ce qui pourrait permettre l'extraction de données sensibles.",
                        "Désactivez le débogage en production en utilisant buildTypes dans le fichier build.gradle.",
                        SecurityRisk.Severity.HIGH
                ));
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Erreur lors de la vérification du débogage: " + e.getMessage());
        }
    }

    /**
     * Vérifie si l'application s'exécute sur un émulateur
     * @param risks Liste des risques à mettre à jour
     */
    private void checkEmulator(List<SecurityRisk> risks) {
        boolean isEmulator = Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT);

        if (isEmulator) {
            risks.add(new SecurityRisk(
                    "Exécution sur émulateur",
                    "L'application s'exécute sur un émulateur, ce qui pourrait être moins sécurisé qu'un appareil réel.",
                    "Assurez-vous que les données sensibles sont correctement protégées même sur un émulateur.",
                    SecurityRisk.Severity.LOW
            ));
        }
    }

    /**
     * Classe représentant un risque de sécurité
     */
    public static class SecurityRisk {
        private final String title;
        private final String description;
        private final String recommendation;
        private final Severity severity;

        public SecurityRisk(String title, String description, String recommendation, Severity severity) {
            this.title = title;
            this.description = description;
            this.recommendation = recommendation;
            this.severity = severity;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public String getRecommendation() {
            return recommendation;
        }

        public Severity getSeverity() {
            return severity;
        }

        public enum Severity {
            LOW, MEDIUM, HIGH
        }

        @Override
        public String toString() {
            return "Risque: " + title + " (" + severity + ")\n" +
                    "Description: " + description + "\n" +
                    "Recommandation: " + recommendation;
        }
    }
}