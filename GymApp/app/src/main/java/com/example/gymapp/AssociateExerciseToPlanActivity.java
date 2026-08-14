package com.example.gymapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import com.bumptech.glide.Glide;
import com.example.gymapp.dao.ExercicioDao;
import com.example.gymapp.dao.PlanoDao;
import com.example.gymapp.models.Exercicio;
import com.example.gymapp.models.Plano;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AssociateExerciseToPlanActivity extends AppCompatActivity {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private PlanoDao planoDao;
    private ExercicioDao exercicioDao;

    private int selectedPlanId = -1;
    private final Set<Integer> selectedExerciseIds = new HashSet<>();

    // Lists to manage exercise search filtering
    private final List<Exercicio> fullExerciseList = new ArrayList<>();
    private final List<Exercicio> displayedExerciseList = new ArrayList<>();

    private ArrayAdapter<Exercicio> exerciseAdapter;
    private SearchView searchView;
    private Spinner spinner;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_associate);

        planoDao = new PlanoDao(this);
        exercicioDao = new ExercicioDao(this);

        spinner = findViewById(R.id.plan_spinner);
        ListView exerciseListView = findViewById(R.id.listView);
        searchView = findViewById(R.id.searchView);
        Button addExerciseToPlanButton = findViewById(R.id.add_exercise_to_plan);

        // 1. Setup Exercise ListView Adapter with Glide
        exerciseAdapter = new ArrayAdapter<Exercicio>(
                this,
                R.layout.item_exercicio,
                displayedExerciseList
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
                CheckBox checkboxExercise = convertView.findViewById(R.id.checkboxExercise);

                // Hide edit/delete buttons if not needed in the association screen
                if (editButton != null) editButton.setVisibility(View.GONE);
                if (deleteButton != null) deleteButton.setVisibility(View.GONE);

                // Make check box visible
                checkboxExercise.setVisibility(View.VISIBLE);

                Exercicio item = getItem(position);

                if (item != null) {
                    if (eId != null) eId.setText("Id: " + item.getId());
                    if (txtNome != null) txtNome.setText(item.getNome());
                    if (txtVideo != null) {
                        txtVideo.setText("Vídeo: " + (item.getLinkVideo().isEmpty() ? "n/a" : item.getLinkVideo()));
                    }
                    if (txtNotas != null) {
                        txtNotas.setText("Notas: " + (item.getNotas().isEmpty() ? "n/a" : item.getNotas()));
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

                    // --- CHECKBOX STATE MANAGEMENT ---
                    if (checkboxExercise != null) {
                        checkboxExercise.setVisibility(View.VISIBLE);

                        // Clear listener first to prevent recycling side effects
                        checkboxExercise.setOnCheckedChangeListener(null);

                        // Set checked status based on our Set
                        checkboxExercise.setChecked(selectedExerciseIds.contains(item.getId()));

                        // Attach listener
                        checkboxExercise.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            if (isChecked)
                                selectedExerciseIds.add(item.getId());
                            else
                                selectedExerciseIds.remove(item.getId());
                        });
                    }
                }

                return convertView;
            }
        };

        if (exerciseListView != null) {
            exerciseListView.setAdapter(exerciseAdapter);
        }

        // 2. Setup SearchView Listener
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

        // 3. Spinner Item Selected Listener
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Plano selectedItem = (Plano) parent.getItemAtPosition(position);
                if (selectedItem != null) {
                    selectedPlanId = selectedItem.getId();
                    Toast.makeText(AssociateExerciseToPlanActivity.this, "Selected [" + selectedItem.getId() + "] = " + selectedItem.getNome(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // 4. Associate Action Button
        if (addExerciseToPlanButton != null) {
            addExerciseToPlanButton.setOnClickListener(v -> {
                if (selectedPlanId == -1) {
                    Toast.makeText(this, "Please select a plan first", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(this, "Associating exercises with Id = " + selectedExerciseIds + " to Plan ID: " + selectedPlanId, Toast.LENGTH_SHORT).show();
                for( Integer eid : selectedExerciseIds ) {
                    try {
                        exercicioDao.addExercicioToPlano(selectedPlanId,eid);
                    } catch (Exception e){
                        Log.e("AssociateExerciseToPlanActivity" ,"FAILED to insert exercise " + eid + " to plan " + selectedPlanId + " -> Reason: " + e.getMessage());
                    }
                }
            });

        }

        // 5. Load Data asynchronously
        loadInitialData();
    }

    /**
     * Loads both plans (for spinner) and exercises (for listview) on a background thread.
     */
    private void loadInitialData() {
        executor.execute(() -> {
            List<Plano> planList = planoDao.getAll();
            List<Exercicio> exerciseList = exercicioDao.getAll();

            runOnUiThread(() -> {
                // Populate Spinner
                ArrayAdapter<Plano> spinnerAdapter = new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        planList
                );
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(spinnerAdapter);

                // Populate Exercises List
                fullExerciseList.clear();
                fullExerciseList.addAll(exerciseList);

                String currentQuery = searchView != null && searchView.getQuery() != null
                        ? searchView.getQuery().toString()
                        : "";
                filterExercises(currentQuery);
            });
        });
    }

    /**
     * Filters fullExerciseList and updates displayedExerciseList.
     */
    private void filterExercises(String query) {
        displayedExerciseList.clear();

        if (query == null || query.trim().isEmpty()) {
            displayedExerciseList.addAll(fullExerciseList);
        } else {
            String filterPattern = query.toLowerCase().trim();
            for (Exercicio item : fullExerciseList) {
                if (item.getNome() != null && item.getNome().toLowerCase().contains(filterPattern)) {
                    displayedExerciseList.add(item);
                }
            }
        }

        if (exerciseAdapter != null) {
            exerciseAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}