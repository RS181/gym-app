package com.example.gymapp;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.gymapp.database.DatabaseHelper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlanActivity extends  AppCompatActivity {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_plan);

        // Run database initialization off the UI thread
        executor.execute(() -> {
            DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
            // Calling getWritableDatabase() triggers onCreate() if the file doesn't exist
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            Log.d("PlanActivity", "Database opened successfully. Path: " + db.getPath());
            // DatabaseHelper.dbCheck(db,"PlanActivity");
        });





    }
}
