package com.example.gymapp.models;

import androidx.annotation.NonNull;

public class Exercicio {
    private int id;
    private String nome;
    private String linkVideo;
    private String linkGif;
    private String notas;

    // Constructor for reading from DB
    public Exercicio(int id, String nome, String linkGif, String linkVideo, String notas) {
        this.id = id;
        this.nome = nome;
        this.linkGif = linkGif;
        this.linkVideo = linkVideo;
        this.notas = notas;
    }

    // Constructor to add a new Exercicio to the DB
    public Exercicio(String nome, String linkGif, String linkVideo, String notas) {
        this.nome = nome.trim();
        this.linkGif = linkGif.trim();
        this.linkVideo = linkVideo.trim();
        this.notas = notas.trim();
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getLinkVideo() {
        return linkVideo;
    }

    public String getLinkGif() {
        return linkGif;
    }

    public String getNotas() {
        return notas;
    }

    public void setId(int id) {
        this.id = id;
    }

    private String formatValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "N/A";
        }
        return value.trim();
    }

    @Override
    public String toString() {
        return "Exercicio{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", linkVideo='" + linkVideo + '\'' +
                ", linkGif='" + linkGif + '\'' +
                ", notas='" + notas + '\'' +
                '}';
    }
    public String getDetailsFormatted() {
        return "linkVideo=" + formatValue(linkVideo)  +
                "\nlinkGif=" + formatValue(linkGif) +
                "\nnotas=" + formatValue(notas);
    }

}
