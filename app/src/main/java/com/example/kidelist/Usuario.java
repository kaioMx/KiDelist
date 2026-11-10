package com.example.kidelist;

public class Usuario {

    private String id;
    private String lojaId;
    private String nome;
    private String tipo;

    public Usuario() {
    }

    public Usuario(String id, String lojaId, String nome, String tipo) {
        this.id = id;
        this.lojaId = lojaId;
        this.nome = nome;
        this.tipo = tipo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLojaId() {
        return lojaId;
    }

    public void setLojaId(String lojaId) {
        this.lojaId = lojaId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    @Override
    public String toString() {
        return nome;
    }
}