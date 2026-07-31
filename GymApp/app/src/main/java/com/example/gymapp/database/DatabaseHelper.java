package com.example.gymapp.database;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Details
    private static final String DATABASE_NAME = "gymplan.db";
    private static final  String SQL_FILENAME = "gymdb.sql";
    private static final int DATABASE_VERSION = 1;
    private Context context;

    // Singleton pattern to prevent multiple connection to the DB
    private static DatabaseHelper instance;
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null){
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context.getApplicationContext();
    }

    // Activates only once when the DB file is created for the first time
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DatabaseHelper","Creating database from gymdb.sql");
        executeSqlFile(db);
    }

    // Helper method to parse and run the gymdb.sql script
    private void executeSqlFile(SQLiteDatabase db) {
        try {
            InputStream is = context.getAssets().open(SQL_FILENAME);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder statement = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty() || line.startsWith("--") || line.startsWith("//"))
                    continue;

                if (line.contains("--"))
                    line = line.substring(0, line.indexOf("--")).trim();

                statement.append(line).append(" ");

                // Execute as soon as a statement ending with ';' is detected
                if (line.endsWith(";")) {
                    String sql = statement.toString().trim();
                    Log.d("DatabaseHelper", "Executing SQL: " + sql);

                    try {
                        db.execSQL(sql);
                    } catch (SQLException e) {
                        Log.e("DatabaseHelper","SQL ERROR in statement: " + sql + " | Error: " + e.getMessage());
                    }
                    statement.setLength(0); // Reset for next statement
                }
            }
            reader.close();
            Log.d("DatabaseHelper", "Successfully executed all statements in " + SQL_FILENAME);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error reading or executing " + SQL_FILENAME + ": " + e.getMessage(), e);
        }
    }

    // IMPORTANT: if any upgrade is made all data is lost
    // to keep data intact, it is necessary to adjust the function bellow
    // to allow for the upgrade without losing any data
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w("DatabaseHelper", "Upgrading DB from v" + oldVersion + " to v" + newVersion + ". Wiping old data.");
        // Drop existing tables
        db.execSQL("DROP TABLE IF EXISTS plano_exercicio");
        db.execSQL("DROP TABLE IF EXISTS exercicio");
        db.execSQL("DROP TABLE IF EXISTS plano");

        // Re-create the database from schema.sql
        onCreate(db);
    }



    public static void dbCheck(SQLiteDatabase db,String activityName){
        // 1. Get the list of table names created our .sql file
        Cursor cursor = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' AND name NOT LIKE 'android_%'",
                null
        );

        Log.d("DB_CHECK_" + activityName , "=== TABLES IN THE DATABASE ===");
        if (cursor.moveToFirst()) {
            do {
                String tableName = cursor.getString(0);
                Log.d("DB_CHECK_" + activityName, "Found Table: " + tableName);
            } while (cursor.moveToNext());
        } else {
            Log.e("DB_CHECK_" + activityName, "No tables found!");
        }
        cursor.close();
    }





}
