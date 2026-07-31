package com.example.gymapp;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.gymapp.database.DatabaseHelper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Run database initialization off the UI thread
        executor.execute(() -> {
            DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
            // Calling getWritableDatabase() triggers onCreate() if the file doesn't exist
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            Log.d("MainActivity", "Database opened successfully. Path: " + db.getPath());

            //DatabaseHelper.dbCheck(db,"MainActivity");

        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        /* PlanActivity button  */
        Button planListButton = findViewById(R.id.planListButton);
        planListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Going to List of Workout Plans", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this,PlanActivity.class);
                startActivity(intent);
            }
        });


        /* ExerciseActivity  button*/
        Button exerciseListButton = findViewById(R.id.exerciseListButton);
        exerciseListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Going to List of Exercise Plans", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this,ExerciseActivity.class);
                startActivity(intent);
            }
        });

    }




}