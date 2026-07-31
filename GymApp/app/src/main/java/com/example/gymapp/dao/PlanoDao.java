package com.example.gymapp.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.gymapp.database.DatabaseHelper;
import com.example.gymapp.models.Plano;

import java.util.ArrayList;
import java.util.List;

public class PlanoDao {

    private final DatabaseHelper dbHelper;
    private final String TABLE_NAME = "plano";

    public PlanoDao(Context context){
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    // CREATE
    public  long insert(Plano plano) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nome",plano.getNome());

        return db.insert(TABLE_NAME,null,values);
    }

    // READ ALL
    public List<Plano> getAll(){
        List<Plano> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("Select id, nome, data_criacao FROM plano ORDER BY id DESC",null);
        if(cursor.moveToFirst()){
            while (cursor.moveToNext()){
                int id = cursor.getInt(0);
                String nome = cursor.getString(1);
                String data_criacao = cursor.getString(2);
                list.add(new Plano(id,nome,data_criacao));
            }
        }

        cursor.close();
        return list;
    }


    // UPDATE
    public int update(Plano plano) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nome", plano.getNome());
        return db.update(TABLE_NAME, values, "id = ?", new String[]{String.valueOf(plano.getId())});
    }

    // DELETE
    public int delete(int planoId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(TABLE_NAME, "id = ?", new String[]{String.valueOf(planoId)});
    }

}
