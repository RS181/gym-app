package com.example.gymapp;

import android.annotation.SuppressLint;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.gymapp.dao.PlanoDao;
import com.example.gymapp.database.DatabaseHelper;
import com.example.gymapp.models.Exercicio;
import com.example.gymapp.models.Plano;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlanActivity extends  AppCompatActivity {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private PlanoDao planoDao;

    // Lists to manage search filtering
    private final List<Plano> fullList = new ArrayList<>();
    private final List<Plano> displayedList = new ArrayList<>();

    private ArrayAdapter<Plano> adapter;
    private SearchView searchView;

    @SuppressLint("MissingInflatedId")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan);

        planoDao = new PlanoDao(this);

        System.out.println("List of plans: " + planoDao.getAll());

        ListView planListView = findViewById(R.id.listView_2);
        searchView = findViewById(R.id.searchView_2);
        Button addPlanButton = findViewById(R.id.button_2);


        // 1. Setup Adapter
        adapter = new ArrayAdapter<Plano>(
                this,
                R.layout.item_plano,
                displayedList
        ) {
            @SuppressLint("SetTextI18n")
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext())
                            .inflate(R.layout.item_plano, parent, false);
                }
                TextView pId = convertView.findViewById(R.id.planId);
                TextView txtNome = convertView.findViewById(R.id.planName);
                TextView dataCriacao = convertView.findViewById(R.id.dataCriacao);
                Button editButton = convertView.findViewById(R.id.editPlan);
                Button deleteButton = convertView.findViewById(R.id.deletePlan);

                Plano item = getItem(position);

                if (item !=null){
                    pId.setText("Id: " + item.getId());
                    txtNome.setText(item.getNome());
                    dataCriacao.setText(item.getDataCriacao());

                    // Edit Button Action
                    editButton.setOnClickListener(v -> {
                        Toast.makeText(v.getContext(), "Editing plan with ID: " + item.getId(), Toast.LENGTH_SHORT).show();
                        createPlanModal("Edit", Optional.of(item.getId()));
                    });

                    // Delete Button Action
                    deleteButton.setOnClickListener(v -> {
                        executor.execute(() -> {
                            planoDao.delete(item.getId());
                            loadPlanosFromDb(); // Refresh UI after delete
                        });
                        Toast.makeText(v.getContext(), "Deleted exercise ID: " + item.getId(), Toast.LENGTH_SHORT).show();
                    });
                }

                return convertView;
            }
        };

        planListView.setAdapter(adapter);

        // 2. Setup SearchView Listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterPlano(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterPlano(newText);
                return true;
            }
        });

        // 3. Setup Add Button
        addPlanButton.setOnClickListener(v -> {
            Toast.makeText(getApplicationContext(), "Creating a new workout Plan", Toast.LENGTH_SHORT).show();
            createPlanModal("Add", Optional.empty());
        });

        // 4. Initial load of plan
        loadPlanosFromDb();
    }

    public void loadPlanosFromDb() {
        executor.execute(() -> {
            List<Plano> listFromDb = planoDao.getAll();

            runOnUiThread(() -> {
                fullList.clear();
                fullList.addAll(listFromDb);
                // Apply current search query if user typed something
                filterPlano(searchView.getQuery().toString());
            });
        });
    }

    /**
     * Filters fullList based on exercise name and updates the displayedList.
     */
    private void filterPlano(String query) {
        displayedList.clear();

        if (query == null || query.trim().isEmpty()) {
            displayedList.addAll(fullList);
        } else {
            String filterPattern = query.toLowerCase().trim();
            for (Plano item : fullList) {
                if (item.getNome().toLowerCase().contains(filterPattern)) {
                    displayedList.add(item);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createPlanModal(String operation, Optional<Integer> id) {
        int editId = operation.equals("Edit") ? id.orElseThrow() : 0;

        AlertDialog.Builder builder = new AlertDialog.Builder(PlanActivity.this);
        builder.setTitle(operation + " Plan modal");
        builder.setMessage("Fill in plan details ");
        builder.setCancelable(false);

        LayoutInflater inflater = PlanActivity.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_plan, null);
        builder.setView(dialogView);
        builder.setPositiveButton("Confirm", null);
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        final EditText dialogPlanName = dialogView.findViewById(R.id.dialogPlanName);
        final EditText dialogPlanDate = dialogView.findViewById(R.id.dialogDataCriacao);

        if (operation.equals("Edit")) {
            executor.execute(() -> {
                try {
                    Plano p = planoDao.getOne(editId);
                    runOnUiThread(() -> {
                        dialogPlanName.setText(p.getNome());
                        dialogPlanDate.setVisibility(View.VISIBLE);
                        dialogPlanDate.setText(p.getDataCriacao());
                    });
                } catch (Exception e) {
                    Log.e("PlanActivity", "Error occurred in createPlanModal: " + e.getMessage());
                }
            });
        }

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = dialogPlanName.getText().toString().trim();

            if (name.isEmpty()) {
                dialogPlanName.setError("Plan name is required");
                return;
            }

            Plano p = new Plano(name);

            executor.execute(() -> {
                if (operation.equals("Add")) {
                    planoDao.insert(p);
                } else if (operation.equals("Edit")) {
                    p.setId(editId);
                    planoDao.update(p);
                }

                // Refresh list on screen after DB write
                loadPlanosFromDb();
            });
            dialog.dismiss();
        });
    }

}




