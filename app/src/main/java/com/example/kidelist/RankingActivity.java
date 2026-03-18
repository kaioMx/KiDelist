package com.example.kidelist;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RankingActivity extends AppCompatActivity {

    private Button btnSemanal, btnMensal;
    private RecyclerView rvRankingLojas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        ImageView btnVoltar = findViewById(R.id.btnVoltar);
        btnSemanal = findViewById(R.id.btnSemanal);
        btnMensal = findViewById(R.id.btnMensal);
        rvRankingLojas = findViewById(R.id.rvRankingLojas);

        btnVoltar.setOnClickListener(v -> {
            Intent intent = new Intent(RankingActivity.this, ChecklistActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        rvRankingLojas.setLayoutManager(new LinearLayoutManager(this));

        carregarRankingSemanal();

        btnSemanal.setOnClickListener(v -> {
            ativarFiltroSemanal();
            carregarRankingSemanal();
        });

        btnMensal.setOnClickListener(v -> {
            ativarFiltroMensal();
            carregarRankingMensal();
        });

        FooterNavigation.setup(this, FooterNavigation.TELA_RANKING);
    }

    private void ativarFiltroSemanal() {
        btnSemanal.setBackgroundResource(R.drawable.bg_filtro_ativo);
        btnSemanal.setTextColor(getColor(android.R.color.white));

        btnMensal.setBackgroundResource(R.drawable.bg_filtro_inativo);
        btnMensal.setTextColor(getColor(R.color.kidelist_primary));
    }

    private void ativarFiltroMensal() {
        btnMensal.setBackgroundResource(R.drawable.bg_filtro_ativo);
        btnMensal.setTextColor(getColor(android.R.color.white));

        btnSemanal.setBackgroundResource(R.drawable.bg_filtro_inativo);
        btnSemanal.setTextColor(getColor(R.color.kidelist_primary));
    }

    private void carregarRankingSemanal() {
        List<LojaRanking> lista = new ArrayList<>();

        lista.add(new LojaRanking(4, "Loja Bia", "Kaio", 870));
        lista.add(new LojaRanking(5, "Loja Elza", "Marcos", 850));
        lista.add(new LojaRanking(6, "Loja Recreio", "Fernanda", 820));
        lista.add(new LojaRanking(7, "Loja Kidelicia", "Juliana", 790));
        lista.add(new LojaRanking(8, "Loja Monte Carmelo", "Carlos", 760));

        LojaRankingAdapter adapter = new LojaRankingAdapter(lista);
        rvRankingLojas.setAdapter(adapter);
    }

    private void carregarRankingMensal() {
        List<LojaRanking> lista = new ArrayList<>();

        lista.add(new LojaRanking(4, "Loja Coromandel", "Kaio", 3420));
        lista.add(new LojaRanking(5, "Loja Patos", "Marcos", 3350));
        lista.add(new LojaRanking(6, "Loja Uberlândia", "Fernanda", 3210));
        lista.add(new LojaRanking(7, "Loja Araguari", "Juliana", 3090));
        lista.add(new LojaRanking(8, "Loja Monte Carmelo", "Carlos", 2980));

        LojaRankingAdapter adapter = new LojaRankingAdapter(lista);
        rvRankingLojas.setAdapter(adapter);
    }
}