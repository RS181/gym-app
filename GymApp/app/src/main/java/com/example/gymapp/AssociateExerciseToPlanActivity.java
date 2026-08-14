package com.example.gymapp;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gymapp.dao.PlanoDao;
import com.example.gymapp.models.Plano;

import java.util.List;

public class AssociateExerciseToPlanActivity  extends AppCompatActivity {

    private PlanoDao planoDao;

    private int selectedPlanId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_associate);

        planoDao = new PlanoDao(this);
        List<Plano> planList = planoDao.getAll();

        // Instantiate and populate spinner with all available plans
        Spinner spinner = findViewById(R.id.plan_spinner);
        ArrayAdapter<Plano> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, planList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // 3. Detetar qual o elemento selecionado
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Plano selectedItem = (Plano) parent.getItemAtPosition(position);
                Toast.makeText(AssociateExerciseToPlanActivity.this, "Selected: " + selectedItem.toString(), Toast.LENGTH_SHORT).show();
                selectedPlanId = selectedItem.getId();
                System.out.println(selectedPlanId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                return;
            }
        });

    }
}
