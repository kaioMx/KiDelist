package com.example.kidelist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LojaRankingAdapter extends RecyclerView.Adapter<LojaRankingAdapter.ViewHolder> {

    private final List<LojaRanking> lista;

    public LojaRankingAdapter(List<LojaRanking> lista) {
        this.lista = lista;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_loja_ranking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LojaRanking loja = lista.get(position);

        holder.txtPosicao.setText(loja.getPosicao() + "º");
        holder.txtNomeLoja.setText(loja.getNomeLoja());
        holder.txtGerenteLoja.setText("Gerente: " + loja.getGerente());
        holder.txtPontos.setText(loja.getPontos() + " pts");
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtPosicao, txtNomeLoja, txtGerenteLoja, txtPontos;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtPosicao = itemView.findViewById(R.id.txtPosicao);
            txtNomeLoja = itemView.findViewById(R.id.txtNomeLoja);
            txtGerenteLoja = itemView.findViewById(R.id.txtGerenteLoja);
            txtPontos = itemView.findViewById(R.id.txtPontos);
        }
    }
}