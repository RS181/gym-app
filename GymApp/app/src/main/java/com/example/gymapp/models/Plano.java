package com.example.gymapp.models;

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
    public Plano(String nome) {
        this.id = id;
        this.nome = nome.trim();
        this.dataCriacao = dataCriacao;
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
}


