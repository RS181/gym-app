package com.example.gymapp;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gymapp.dao.ExercicioDao;
import com.example.gymapp.database.DatabaseHelper;
import com.example.gymapp.models.Exercicio;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExerciseActivity  extends AppCompatActivity {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);

        // Run database initialization off the UI thread
        executor.execute(() -> {
            DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
            // Calling getWritableDatabase() triggers onCreate() if the file doesn't exist
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            Log.d("ExerciseActivity", "Database opened successfully. Path: " + db.getPath());

            // DatabaseHelper.dbCheck(db,"ExerciseActivity");
        });



        /* Add Exercise button behavior */
        Button addExerciseButton = findViewById(R.id.button);
        addExerciseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Adding new Exercise", Toast.LENGTH_SHORT).show();

                /* Create modal screen */

                // Instantiate and set up the dialog constructor
                AlertDialog.Builder builder = new AlertDialog.Builder(ExerciseActivity.this);
                builder.setTitle("Add exercise modal");
                builder.setMessage("Fill in exercise details ");
                builder.setCancelable(false);


                // Inflate custom XML
                LayoutInflater inflater = ExerciseActivity.this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog_add_exercise,null);
                builder.setView(dialogView);
                builder.setPositiveButton("Confirm", null);
                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());


                // Get the elements from dialog
                final EditText dialogExerciseName = dialogView.findViewById(R.id.dialogExerciseName);
                final EditText dialogExerciseVideo = dialogView.findViewById(R.id.dialogExerciseVideo);
                final EditText dialogExerciseGIF = dialogView.findViewById(R.id.dialogExerciseGIF);
                final EditText dialogExerciseNotes = dialogView.findViewById(R.id.dialogExerciseNotes);


                AlertDialog dialog = builder.create();
                dialog.show();

                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String name = dialogExerciseName.getText().toString().trim();
                        String video = dialogExerciseVideo.getText().toString().trim();
                        String gif = dialogExerciseGIF.getText().toString().trim();
                        String notes = dialogExerciseNotes.getText().toString().trim();

                        if(name.isEmpty()) {
                            dialogExerciseName.setError("Exercise name is required");
                            return;
                        }
                        else if (!video.isEmpty()){
                            if (!Validator.check_URL(video)) {
                                dialogExerciseVideo.setError("Invalid URL for video");
                                return;
                            }
                        }
                        else if(!gif.isEmpty()){
                            if(!Validator.check_URL(gif)) {
                                dialogExerciseNotes.setError("Invalid URL for GIF");
                                return;
                            }
                        }

                        Exercicio e = new Exercicio(name,gif,video,notes);
                        ExercicioDao exercicioDao = new ExercicioDao(ExerciseActivity.this);
                        //exercicioDao.addExercise(e);
                        System.out.println(Arrays.toString(exercicioDao.getAll().toArray()));

                        dialog.dismiss(); // Fecha o diálogo apenas agora
                    }
                });
            }
        });

    }
}

