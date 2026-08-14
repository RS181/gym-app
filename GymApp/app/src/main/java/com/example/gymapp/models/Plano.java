package com.example.gymapp.models;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.time.LocalDate;

public class Plano {
    private int id;
    private String nome;
    private  String dataCriacao;


    // Constructor for reading from DB
    public Plano(int id, String nome, String dataCriacao) {
        this.id = id;
        this.nome = nome;
        this.dataCriacao = dataCriacao;
    }

    // Constructor to add a new Plano to the DB
    @RequiresApi(api = Build.VERSION_CODES.O)
    public Plano(String nome) {
        this.nome = nome.trim();
        this.dataCriacao = LocalDate.now().toString();
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDataCriacao() {
        return dataCriacao;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return nome;
    }
}


