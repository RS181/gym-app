package com.example.gymapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import com.bumptech.glide.Glide;
import com.example.gymapp.dao.ExercicioDao;
import com.example.gymapp.models.Exercicio;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExerciseActivity extends AppCompatActivity {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private ExercicioDao exercicioDao;

    // Lists to manage search filtering
    private final List<Exercicio> fullList = new ArrayList<>();
    private final List<Exercicio> displayedList = new ArrayList<>();

    private ArrayAdapter<Exercicio> adapter;
    private SearchView searchView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);

        exercicioDao = new ExercicioDao(this);

        ListView exerciseListView = findViewById(R.id.listView);
        searchView = findViewById(R.id.searchView);
        Button addExerciseButton = findViewById(R.id.button);

        // 1. Setup Adapter
        adapter = new ArrayAdapter<Exercicio>(
                this,
                R.layout.item_exercicio,
                displayedList
        ) {
            @SuppressLint("SetTextI18n")
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext())
                            .inflate(R.layout.item_exercicio, parent, false);
                }

                TextView eId = convertView.findViewById(R.id.exerciseId);
                TextView txtNome = convertView.findViewById(R.id.textNome);
                TextView txtVideo = convertView.findViewById(R.id.textVideo);
                ImageView imgGif = convertView.findViewById(R.id.gifImage);
                TextView txtNotas = convertView.findViewById(R.id.textNotas);
                Button editButton = convertView.findViewById(R.id.editExercicio);
                Button deleteButton = convertView.findViewById(R.id.deleteExercicio);

                Exercicio item = getItem(position);

                if (item != null) {
                    String gifUrl = item.getLinkGif() == null || item.getLinkGif().isEmpty() ? "n/a" : item.getLinkGif();

                    eId.setText("Id: " + item.getId());
                    txtNome.setText(item.getNome());
                    txtVideo.setText("Vídeo: " + (item.getLinkVideo().isEmpty() ? "n/a" : item.getLinkVideo()));
                    txtNotas.setText("Notas: " + (item.getNotas().isEmpty() ? "n/a" : item.getNotas()));

                    if (!gifUrl.trim().isEmpty() && !gifUrl.equalsIgnoreCase("n/a")) {
                        imgGif.setVisibility(View.VISIBLE);
                        Glide.with(getContext())
                                .asGif()
                                .load(gifUrl)
                                .override(400, 300)
                                .placeholder(android.R.drawable.stat_sys_download)
                                .error(android.R.drawable.ic_dialog_alert)
                                .into(imgGif);
                    } else {
                        imgGif.setVisibility(View.GONE);
                    }

                    // Edit Button Action
                    editButton.setOnClickListener(v -> {
                        Toast.makeText(v.getContext(), "Editing exercise with ID: " + item.getId(), Toast.LENGTH_SHORT).show();
                        createExerciseModal("Edit", Optional.of(item.getId()));
                    });

                    // Delete Button Action
                    deleteButton.setOnClickListener(v -> {
                        executor.execute(() -> {
                            exercicioDao.delete(item.getId());
                            loadExercisesFromDb(); // Refresh UI after delete
                        });
                        Toast.makeText(v.getContext(), "Deleted exercise ID: " + item.getId(), Toast.LENGTH_SHORT).show();
                    });
                }

                return convertView;
            }
        };

        exerciseListView.setAdapter(adapter);

        // 2. Setup SearchView Listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterExercises(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterExercises(newText);
                return true;
            }
        });

        // 3. Setup Add Button
        addExerciseButton.setOnClickListener(v -> {
            Toast.makeText(getApplicationContext(), "Adding new Exercise", Toast.LENGTH_SHORT).show();
            createExerciseModal("Add", Optional.empty());
        });

        // 4. Initial load of exercises
        loadExercisesFromDb();
    }

    /**
     * Reads all exercises from the DB on a background thread and updates the UI lists.
     */
    private void loadExercisesFromDb() {
        executor.execute(() -> {
            List<Exercicio> listFromDb = exercicioDao.getAll();

            runOnUiThread(() -> {
                fullList.clear();
                fullList.addAll(listFromDb);
                // Apply current search query if user typed something
                filterExercises(searchView.getQuery().toString());
            });
        });
    }

    /**
     * Filters fullList based on exercise name and updates the displayedList.
     */
    private void filterExercises(String query) {
        displayedList.clear();

        if (query == null || query.trim().isEmpty()) {
            displayedList.addAll(fullList);
        } else {
            String filterPattern = query.toLowerCase().trim();
            for (Exercicio item : fullList) {
                if (item.getNome().toLowerCase().contains(filterPattern)) {
                    displayedList.add(item);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void createExerciseModal(String operation, Optional<Integer> id) {
        int editId = operation.equals("Edit") ? id.orElseThrow() : 0;

        AlertDialog.Builder builder = new AlertDialog.Builder(ExerciseActivity.this);
        builder.setTitle(operation + " exercise modal");
        builder.setMessage("Fill in exercise details ");
        builder.setCancelable(false);

        LayoutInflater inflater = ExerciseActivity.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_exercise, null);
        builder.setView(dialogView);
        builder.setPositiveButton("Confirm", null);
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        final EditText dialogExerciseName = dialogView.findViewById(R.id.dialogExerciseName);
        final EditText dialogExerciseVideo = dialogView.findViewById(R.id.dialogExerciseVideo);
        final EditText dialogExerciseGIF = dialogView.findViewById(R.id.dialogExerciseGIF);
        final EditText dialogExerciseNotes = dialogView.findViewById(R.id.dialogExerciseNotes);

        if (operation.equals("Edit")) {
            executor.execute(() -> {
                try {
                    Exercicio e = exercicioDao.getOne(editId);
                    runOnUiThread(() -> {
                        dialogExerciseName.setText(e.getNome());
                        dialogExerciseVideo.setText(e.getLinkVideo());
                        dialogExerciseGIF.setText(e.getLinkGif());
                        dialogExerciseNotes.setText(e.getNotas());
                    });
                } catch (Exception e) {
                    Log.e("ExerciseActivity", "Error occurred in createExerciseModal: " + e.getMessage());
                }
            });
        }

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = dialogExerciseName.getText().toString().trim();
            String video = dialogExerciseVideo.getText().toString().trim();
            String gif = dialogExerciseGIF.getText().toString().trim();
            String notes = dialogExerciseNotes.getText().toString().trim();

            if (name.isEmpty()) {
                dialogExerciseName.setError("Exercise name is required");
                return;
            } else if (!video.isEmpty() && !Validator.check_URL(video)) {
                dialogExerciseVideo.setError("Invalid URL for video");
                return;
            } else if (!gif.isEmpty() && !Validator.check_URL(gif)) {
                dialogExerciseGIF.setError("Invalid URL for GIF");
                return;
            }

            Exercicio e = new Exercicio(name, gif, video, notes);

            executor.execute(() -> {
                if (operation.equals("Add")) {
                    exercicioDao.addExercise(e);
                } else if (operation.equals("Edit")) {
                    e.setId(editId);
                    exercicioDao.update(e);
                }

                // Refresh list on screen after DB write
                loadExercisesFromDb();
            });

            dialog.dismiss();
        });
    }
}