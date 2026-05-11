package com.example.securestorage.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.securestorage.R;
import com.example.securestorage.storage.ExternalStorageManager;

public class ExternalStorageFragment extends Fragment {

    private static final String DEFAULT_FILENAME = "external_notes.txt";
    private static final int PERMISSION_REQUEST_CODE = 123;

    private EditText etFilename;
    private EditText etContent;
    private TextView tvFileContent;
    private ExternalStorageManager storageManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_external_storage, container, false);

        // Initialisation du gestionnaire de stockage
        storageManager = new ExternalStorageManager(requireContext());

        // Initialisation des vues
        etFilename = view.findViewById(R.id.etFilename);
        etContent = view.findViewById(R.id.etContent);
        tvFileContent = view.findViewById(R.id.tvFileContent);
        Button btnSave = view.findViewById(R.id.btnSave);
        Button btnLoad = view.findViewById(R.id.btnLoad);
        Button btnDelete = view.findViewById(R.id.btnDelete);
        Button btnList = view.findViewById(R.id.btnList);

        // Configuration des valeurs par défaut
        etFilename.setText(DEFAULT_FILENAME);

        // Configuration des écouteurs
        btnSave.setOnClickListener(v -> {
            if (checkPermissions()) {
                saveFile();
            }
        });

        btnLoad.setOnClickListener(v -> {
            if (checkPermissions()) {
                loadFile();
            }
        });

        btnDelete.setOnClickListener(v -> {
            if (checkPermissions()) {
                deleteFile();
            }
        });

        btnList.setOnClickListener(v -> {
            if (checkPermissions()) {
                listFiles();
            }
        });

        return view;
    }

    private boolean checkPermissions() {
        // Pour Android 10 (API 29) et supérieur, nous n'avons pas besoin de demander des permissions
        // car nous utilisons le stockage spécifique à l'application
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return true;
        }

        // Pour les versions antérieures, nous devons demander la permission WRITE_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
            return false;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Permission accordée", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Permission refusée", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveFile() {
        String filename = etFilename.getText().toString();
        String content = etContent.getText().toString();

        if (filename.isEmpty()) {
            Toast.makeText(requireContext(), "Veuillez entrer un nom de fichier", Toast.LENGTH_SHORT).show();
            return;
        }

        if (content.isEmpty()) {
            Toast.makeText(requireContext(), "Veuillez entrer du contenu", Toast.LENGTH_SHORT).show();
            return;
        }

        // Utilisation du répertoire Documents pour les fichiers texte
        boolean success = storageManager.writeToExternalStorage(filename, content, Environment.DIRECTORY_DOCUMENTS);
        if (success) {
            Toast.makeText(requireContext(), "Fichier enregistré avec succès", Toast.LENGTH_SHORT).show();
            etContent.setText("");
        } else {
            Toast.makeText(requireContext(), "Erreur lors de l'enregistrement du fichier", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadFile() {
        String filename = etFilename.getText().toString();

        if (filename.isEmpty()) {
            Toast.makeText(requireContext(), "Veuillez entrer un nom de fichier", Toast.LENGTH_SHORT).show();
            return;
        }

        String content = storageManager.readFromExternalStorage(filename, Environment.DIRECTORY_DOCUMENTS);
        if (content != null) {
            tvFileContent.setText(content);
        } else {
            tvFileContent.setText("Aucun contenu trouvé ou erreur de lecture");
            Toast.makeText(requireContext(), "Erreur lors de la lecture du fichier", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteFile() {
        String filename = etFilename.getText().toString();

        if (filename.isEmpty()) {
            Toast.makeText(requireContext(), "Veuillez entrer un nom de fichier", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success = storageManager.deleteFromExternalStorage(filename, Environment.DIRECTORY_DOCUMENTS);
        if (success) {
            Toast.makeText(requireContext(), "Fichier supprimé avec succès", Toast.LENGTH_SHORT).show();
            tvFileContent.setText("");
        } else {
            Toast.makeText(requireContext(), "Erreur lors de la suppression du fichier", Toast.LENGTH_SHORT).show();
        }
    }

    private void listFiles() {
        String[] files = storageManager.listExternalStorageFiles(Environment.DIRECTORY_DOCUMENTS);
        StringBuilder sb = new StringBuilder("Fichiers disponibles:\n");

        if (files.length == 0) {
            sb.append("Aucun fichier trouvé");
        } else {
            for (String file : files) {
                sb.append("- ").append(file).append("\n");
            }
        }

        tvFileContent.setText(sb.toString());
    }
}