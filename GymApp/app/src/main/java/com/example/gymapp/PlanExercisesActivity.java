package com.example.gymapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import com.bumptech.glide.Glide;
import com.example.gymapp.dao.ExercicioDao;
import com.example.gymapp.models.Exercicio;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlanExercisesActivity extends AppCompatActivity {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private ExercicioDao exercicioDao;

    private int planoId = -1;
    private String planoNome = "";

    // Lists to manage search filtering
    private final List<Exercicio> fullList = new ArrayList<>();
    private final List<Exercicio> displayedList = new ArrayList<>();

    private ArrayAdapter<Exercicio> adapter;
    private SearchView searchView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_exercises);

        exercicioDao = new ExercicioDao(this);

        // 1. Get Plan Data from Intent
        Intent intent = getIntent();
        if (intent != null) {
            planoId = intent.getIntExtra("PLANO_ID", -1);
            planoNome = intent.getStringExtra("PLANO_NAME");
        }

        if (planoId == -1) {
            Toast.makeText(this, "Error: Invalid Plan ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TextView titleTextView = findViewById(R.id.planTitleTextView);
        if (titleTextView != null && planoNome != null && !planoNome.isEmpty()) {
            titleTextView.setText("Plan: " + planoNome);
        }

        ListView exerciseListView = findViewById(R.id.listView);
        searchView = findViewById(R.id.searchView);

        // 2. Setup Adapter
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

                // Hide edit/delete actions here if this is a view-only list
                if (editButton != null) editButton.setVisibility(View.GONE);
                if (deleteButton != null) deleteButton.setVisibility(View.GONE);

                Exercicio item = getItem(position);

                if (item != null) {
                    if (eId != null) eId.setText("Id: " + item.getId());
                    if (txtNome != null) txtNome.setText(item.getNome());
                    if (txtVideo != null) {
                        txtVideo.setText("Vídeo: " + (item.getLinkVideo() == null || item.getLinkVideo().isEmpty() ? "n/a" : item.getLinkVideo()));
                    }
                    if (txtNotas != null) {
                        txtNotas.setText("Notas: " + (item.getNotas() == null || item.getNotas().isEmpty() ? "n/a" : item.getNotas()));
                    }

                    String gifUrl = item.getLinkGif() == null || item.getLinkGif().isEmpty() ? "n/a" : item.getLinkGif();
                    if (imgGif != null) {
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
                    }
                }

                return convertView;
            }
        };

        if (exerciseListView != null) {
            exerciseListView.setAdapter(adapter);
        }

        // 3. Setup SearchView Listener
        if (searchView != null) {
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
        }

        // 4. Initial load of exercises for this specific plan
        loadExercisesFromDb();
    }

    /**
     * Reads all exercises linked to planoId from the DB on a background thread.
     */
    private void loadExercisesFromDb() {
        executor.execute(() -> {
            List<Exercicio> listFromDb = exercicioDao.getExerciciosByPlanoId(planoId);

            runOnUiThread(() -> {
                fullList.clear();
                fullList.addAll(listFromDb);

                String query = searchView != null && searchView.getQuery() != null
                        ? searchView.getQuery().toString()
                        : "";
                filterExercises(query);
            });
        });
    }

    /**
     * Filters fullList based on exercise name and updates displayedList.
     */
    private void filterExercises(String query) {
        displayedList.clear();

        if (query == null || query.trim().isEmpty()) {
            displayedList.addAll(fullList);
        } else {
            String filterPattern = query.toLowerCase().trim();
            for (Exercicio item : fullList) {
                if (item.getNome() != null && item.getNome().toLowerCase().contains(filterPattern)) {
                    displayedList.add(item);
                }
            }
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}