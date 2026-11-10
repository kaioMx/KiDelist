package com.example.kidelist;

public class LojaRanking {

    private int posicao;
    private String nomeLoja;
    private String gerente;
    private double pontos;

    public LojaRanking() {
    }

    public LojaRanking(int posicao, String nomeLoja, String gerente, double pontos) {
        this.posicao = posicao;
        this.nomeLoja = nomeLoja;
        this.gerente = gerente;
        this.pontos = pontos;
    }

    public int getPosicao() {
        return posicao;
    }

    public void setPosicao(int posicao) {
        this.posicao = posicao;
    }

    public String getNomeLoja() {
        return nomeLoja;
    }

    public void setNomeLoja(String nomeLoja) {
        this.nomeLoja = nomeLoja;
    }

    public String getGerente() {
        return gerente;
    }

    public void setGerente(String gerente) {
        this.gerente = gerente;
    }

    public double getPontos() {
        return pontos;
    }

    public void setPontos(double pontos) {
        this.pontos = pontos;
    }
}