package com.example.securestorage.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.securestorage.R;
import com.example.securestorage.storage.InternalStorageManager;

public class InternalStorageFragment extends Fragment {

    private static final String DEFAULT_FILENAME = "secure_notes.txt";

    private EditText etFilename;
    private EditText etContent;
    private TextView tvFileContent;
    private InternalStorageManager storageManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_internal_storage, container, false);

        // Initialisation du gestionnaire de stockage
        storageManager = new InternalStorageManager(requireContext());

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
        btnSave.setOnClickListener(v -> saveFile());
        btnLoad.setOnClickListener(v -> loadFile());
        btnDelete.setOnClickListener(v -> deleteFile());
        btnList.setOnClickListener(v -> listFiles());

        return view;
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

        boolean success = storageManager.writeToInternalStorage(filename, content);
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

        String content = storageManager.readFromInternalStorage(filename);
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

        boolean success = storageManager.deleteFromInternalStorage(filename);
        if (success) {
            Toast.makeText(requireContext(), "Fichier supprimé avec succès", Toast.LENGTH_SHORT).show();
            tvFileContent.setText("");
        } else {
            Toast.makeText(requireContext(), "Erreur lors de la suppression du fichier", Toast.LENGTH_SHORT).show();
        }
    }

    private void listFiles() {
        String[] files = storageManager.listInternalStorageFiles();
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