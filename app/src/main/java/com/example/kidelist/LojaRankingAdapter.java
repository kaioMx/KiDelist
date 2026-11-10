package com.example.kidelist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

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

        DecimalFormatSymbols simbolos =
                new DecimalFormatSymbols(new Locale("pt", "BR"));

        DecimalFormat formato = new DecimalFormat("0.00", simbolos);

        holder.txtPosicao.setText(loja.getPosicao() + "º");
        holder.txtNomeLoja.setText(loja.getNomeLoja());
        holder.txtGerenteLoja.setText("Gerente: " + loja.getGerente());
        holder.txtPontos.setText(formato.format(loja.getPontos()) + " / 5");
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public void atualizarLista(List<LojaRanking> novaLista) {
        if (this.lista == novaLista) {
            notifyDataSetChanged();
            return;
        }

        this.lista.clear();
        this.lista.addAll(novaLista);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtPosicao;
        TextView txtNomeLoja;
        TextView txtGerenteLoja;
        TextView txtPontos;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtPosicao = itemView.findViewById(R.id.txtPosicao);
            txtNomeLoja = itemView.findViewById(R.id.txtNomeLoja);
            txtGerenteLoja = itemView.findViewById(R.id.txtGerenteLoja);
            txtPontos = itemView.findViewById(R.id.txtPontos);
        }
    }
}