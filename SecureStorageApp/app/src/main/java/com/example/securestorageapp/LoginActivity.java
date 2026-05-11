package com.example.securestorage;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.securestorage.database.DatabaseManager;
import com.example.securestorage.database.User;
import com.example.securestorage.security.KeyDerivationManager;
import com.example.securestorage.security.PasswordHasher;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;
    private Button btnRegister;

    private DatabaseManager databaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialisation du gestionnaire de base de données
        databaseManager = new DatabaseManager(this);

        // Initialisation des vues
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        // Configuration des écouteurs
        btnLogin.setOnClickListener(v -> login());
        btnRegister.setOnClickListener(v -> register());
    }

    private void login() {
        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        // Récupération de l'utilisateur
        databaseManager.getUserByUsername(username, new DatabaseManager.DatabaseCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    // Vérification du mot de passe
                    boolean passwordValid = PasswordHasher.verifyPassword(password, user.getPassword());

                    if (passwordValid) {
                        // Connexion réussie
                        Toast.makeText(LoginActivity.this, "Connexion réussie", Toast.LENGTH_SHORT).show();

                        // Ouverture de l'activité principale
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("USER_ID", user.getId());
                        intent.putExtra("USERNAME", user.getUsername());
                        startActivity(intent);
                        finish();
                    } else {
                        // Mot de passe incorrect
                        Toast.makeText(LoginActivity.this, "Mot de passe incorrect", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Utilisateur non trouvé
                    Toast.makeText(LoginActivity.this, "Utilisateur non trouvé", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(LoginActivity.this, "Erreur lors de la connexion: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void register() {
        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        // Vérification de la force du mot de passe
        if (password.length() < 8) {
            Toast.makeText(this, "Le mot de passe doit contenir au moins 8 caractères", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hachage du mot de passe
        String hashedPassword = PasswordHasher.hashPassword(password);

        // Création de l'utilisateur
        User user = new User(username, username + "@example.com", hashedPassword);

        // Insertion de l'utilisateur dans la base de données
        databaseManager.insertUser(user, new DatabaseManager.DatabaseCallback<Long>() {
            @Override
            public void onSuccess(Long userId) {
                Toast.makeText(LoginActivity.this, "Inscription réussie", Toast.LENGTH_SHORT).show();

                // Connexion automatique
                login();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(LoginActivity.this, "Erreur lors de l'inscription: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}