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

import com.bumptech.glide.Glide;
import com.example.gymapp.dao.ExercicioDao;
import com.example.gymapp.models.Exercicio;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExerciseActivity  extends AppCompatActivity {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);

        ListView exerciseListView = findViewById(R.id.listView);

        // Get exercises from DB
        executor.execute( () ->  {
            ExercicioDao exercicioDao = new ExercicioDao(this);
            List<Exercicio> exercicioList = exercicioDao.getAll();

            // Update UI on main thread
            runOnUiThread( () -> {
                ArrayAdapter<Exercicio> adapter = new ArrayAdapter<>(
                        this,
                        R.layout.item_exercicio,
                        exercicioList
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

                        editButton.setOnClickListener( view ->{
                            Exercicio selected = exercicioList.get(position);
                            Toast.makeText(this.getContext(), "Editing exercise with ID: " + selected.getId(), Toast.LENGTH_SHORT).show();
                        });

                        Exercicio item = getItem(position);

                        String gifUrl = (item.getLinkGif().isEmpty()) ? "n/a" : item.getLinkGif();

                        if (item != null) {
                            // Load available info from exercise
                            eId.setText(String.valueOf("Id: " + item.getId()));
                            txtNome.setText(item.getNome());
                            txtVideo.setText("Vídeo: " + ((item.getLinkVideo().isEmpty()) ? "n/a" : item.getLinkVideo()));
                            txtNotas.setText("Notas: " + ((item.getNotas().isEmpty()) ? "n/a" : item.getNotas()));

                            if (gifUrl != null && !gifUrl.trim().isEmpty() && !gifUrl.equalsIgnoreCase("n/a")) {
                                imgGif.setVisibility(View.VISIBLE);

                                // GLIDE: Efficient memory-managed GIF loading
                                Glide.with(getContext())
                                        .asGif()                                    // Ensures it renders animated GIFs
                                        .load(gifUrl)                               // Accepts URL string or local asset path
                                        .override(400, 300)                         // Downscales GIF in memory to fit view size
                                        .placeholder(android.R.drawable.stat_sys_download) // Shown while loading
                                        .error(android.R.drawable.ic_dialog_alert)         // Shown if link fails or is invalid
                                        .into(imgGif);
                            } else {
                                // Hide the view if no valid GIF exists to save screen space
                                imgGif.setVisibility(View.GONE);
                            }

                            // Edit behavior
                            editButton.setOnClickListener(v -> {
                                Toast.makeText(v.getContext(), "Editing exercise with ID: " + item.getId(), Toast.LENGTH_SHORT).show();
                                createExerciseModal("Edit", Optional.of(item.getId()));
                            });

                            // Add behavior
                            deleteButton.setOnClickListener(v -> {
                                Toast.makeText(v.getContext(), "Deleting exercise with ID: " + item.getId(), Toast.LENGTH_SHORT).show();
                                exercicioDao.delete(item.getId());
                            });



                        }

                        return convertView;
                    }


                };

                exerciseListView.setAdapter(adapter);


            });
        });


        /* Add Exercise button behavior */
        Button addExerciseButton = findViewById(R.id.button);
        addExerciseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Adding new Exercise", Toast.LENGTH_SHORT).show();
                createExerciseModal("Add", Optional.empty());
            }
        });

    }



    private void createExerciseModal(String operation, Optional<Integer> id){
        // Get id (optional)
        int editId = 0;
        if(operation.equals("Edit"))
            editId = id.orElseThrow();
        /* Create modal screen */

        // Instantiate and set up the dialog constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(ExerciseActivity.this);
        builder.setTitle(operation + " exercise modal");
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




        if(operation.equals("Edit")){
            ExercicioDao exercicioDao = new ExercicioDao(ExerciseActivity.this);
            try {
                Exercicio e = exercicioDao.getOne(editId);
                dialogExerciseName.setText(e.getNome());
                dialogExerciseVideo.setText(e.getLinkVideo());
                dialogExerciseGIF.setText(e.getLinkGif());
                dialogExerciseNotes.setText(e.getNotas());
            } catch (Exception e) {
                Log.e("ExerciseActivity","Error occured in ExerciseActivity -> createExerciseModal "  + e.getMessage());
            }

        }

        AlertDialog dialog = builder.create();
        dialog.show();

        int finalEditId = editId;

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

                if(operation.equals("Add"))
                    exercicioDao.addExercise(e);
                else if (operation.equals("Edit")){
                    e.setId(finalEditId);
                    exercicioDao.update(e);
                }

                dialog.dismiss(); // Fecha o diálogo apenas agora
            }
        });
    }
}


