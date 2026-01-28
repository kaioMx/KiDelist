package com.example.kidelist;

public class TarefaChecklist {
    private String nome;
    private int nota; // 0 = não avaliado, 1..5 = avaliado
    private boolean feito; // true = FEITO, false = PENDENTE

    public TarefaChecklist(String nome, int nota, boolean feito) {
        this.nome = nome;
        this.nota = nota;
        this.feito = feito;
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public int getNota() { return nota; }
    public void setNota(int nota) { this.nota = nota; }

    public boolean isFeito() { return feito; }
    public void setFeito(boolean feito) { this.feito = feito; }
}
