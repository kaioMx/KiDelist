package com.example.kidelist;

public class LojaRanking {

    private int posicao;
    private String nomeLoja;
    private String gerente;
    private int pontos;

    public LojaRanking(int posicao, String nomeLoja, String gerente, int pontos) {
        this.posicao = posicao;
        this.nomeLoja = nomeLoja;
        this.gerente = gerente;
        this.pontos = pontos;
    }

    public int getPosicao() {
        return posicao;
    }

    public String getNomeLoja() {
        return nomeLoja;
    }

    public String getGerente() {
        return gerente;
    }

    public int getPontos() {
        return pontos;
    }
}