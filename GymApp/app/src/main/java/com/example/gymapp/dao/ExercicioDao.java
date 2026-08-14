package com.example.gymapp.dao;

import com.example.gymapp.database.DatabaseHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.gymapp.models.Exercicio;

import java.util.ArrayList;
import java.util.List;

public class ExercicioDao {

    private final DatabaseHelper dbHelper;
    private final String TABLE_NAME = "exercicio";

    public ExercicioDao(Context context){
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    // CREATE
    public  long addExercise(Exercicio exercicio) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nome",exercicio.getNome());
        values.put("link_video",exercicio.getLinkVideo());
        values.put("link_gif",exercicio.getLinkGif());
        values.put("notas",exercicio.getNotas());
        return db.insert(TABLE_NAME,null,values);
    }

    // LINK EXERCISE TO PLAN (plano_exercicio junction table)
    public long addExercicioToPlano(int planoId, int exercicioId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("plano_id", planoId);
        values.put("exercicio_id", exercicioId);

        return db.insertOrThrow("plano_exercicio", null, values);
    }


    // READ ALL Exercises
    public List<Exercicio> getAll(){
        List<Exercicio> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT id, nome, link_video, link_gif, notas FROM exercicio ORDER BY id DESC";

        try (Cursor cursor = db.rawQuery(query, null)) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String nome = cursor.getString(1);
                String linkVideo = cursor.getString(2);
                String linkGif = cursor.getString(3);
                String notas = cursor.getString(4);

                list.add(new Exercicio(id, nome, linkGif, linkVideo, notas));
            }
        } catch (Exception e) {
            Log.e("ExercicioDao", "Error fetching all exercises: " + e.getMessage());
        }
        return list;
    }


    // GET ONE
    public Exercicio getOne(int id){
        Exercicio exercicio = null;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT id, nome, link_video, link_gif, notas FROM exercicio WHERE id = ?";
        String[] selectionArgs = new String[]{ String.valueOf(id) };

        Cursor cursor = db.rawQuery(query,selectionArgs);
        if(cursor.moveToFirst()){
            String nome = cursor.getString(1);
            String linkVideo = cursor.getString(2);
            String linkGif = cursor.getString(3);
            String notas = cursor.getString(4);
            exercicio = new Exercicio(id,nome,linkGif,linkVideo,notas);
        }
        return exercicio;
    }

    // READ EXERCISES FOR A SPECIFIC PLAN (JOIN query)
    public List<Exercicio> getExerciciosByPlanoId(int planoId) {
        List<Exercicio> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT e.* FROM exercicio e " +
                "JOIN plano_exercicio pe ON e.id = pe.exercicio_id " +
                "WHERE pe.plano_id = ?";

        // Using try-with-resources auto-closes the cursor safely
        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(planoId)})) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String nome = cursor.getString(cursor.getColumnIndexOrThrow("nome"));
                String video = cursor.getString(cursor.getColumnIndexOrThrow("link_video"));
                String gif = cursor.getString(cursor.getColumnIndexOrThrow("link_gif"));
                String notas = cursor.getString(cursor.getColumnIndexOrThrow("notas"));

                list.add(new Exercicio(id, nome, video, gif, notas));
            }
        } catch (Exception e) {
            Log.e("ExercicioDao", "Error fetching exercises for plan " + planoId + ": " + e.getMessage());
        }
        return list;
    }


    // UPDATE
    public void update(Exercicio exercicio) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String sql = "UPDATE " + TABLE_NAME + " SET nome = ?, link_video = ?, link_gif = ?, notas = ? WHERE id = ?";
        try {
            db.execSQL(sql, new Object[]{
                    exercicio.getNome(),
                    exercicio.getLinkVideo(),
                    exercicio.getLinkGif(),
                    exercicio.getNotas(),
                    exercicio.getId()
            });
        } catch (Exception e) {
            Log.e("ExercicioDao", "Error in ExercicioDao -> update : " + e.getMessage());
        }
    }

    // DELETE
    public int delete(int exercicioId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(TABLE_NAME, "id = ?", new String[]{String.valueOf(exercicioId)});
    }

}
