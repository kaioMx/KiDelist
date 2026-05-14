package com.example.kidelist;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RankingActivity extends AppCompatActivity {

    private Button btnSemanal, btnMensal;
    private RecyclerView rvRankingLojas;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        db = FirebaseFirestore.getInstance();

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

        ativarFiltroSemanal();
        carregarRankingFirebase();

        btnSemanal.setOnClickListener(v -> {
            ativarFiltroSemanal();
            carregarRankingFirebase();
        });

        btnMensal.setOnClickListener(v -> {
            ativarFiltroMensal();
            carregarRankingFirebase();
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

    private void carregarRankingFirebase() {

        Map<String, LojaRankingTemp> rankingLojas = new HashMap<>();

        db.collection("usuarios")
                .get()
                .addOnSuccessListener(usuariosSnapshot -> {

                    Map<String, String> usuarioLojaId = new HashMap<>();
                    Map<String, String> usuarioLojaNome = new HashMap<>();
                    Map<String, String> usuarioNome = new HashMap<>();

                    for (DocumentSnapshot userDoc : usuariosSnapshot.getDocuments()) {

                        String userId = userDoc.getId();

                        String nome = userDoc.getString("nome");
                        String lojaId = userDoc.getString("lojaId");

                        if (lojaId != null) {

                            usuarioLojaId.put(userId, lojaId);

                            usuarioLojaNome.put(userId, lojaId);

                            usuarioNome.put(
                                    userId,
                                    nome != null ? nome : "Usuário"
                            );
                        }
                    }

                    db.collection("tarefas")
                            .get()
                            .addOnSuccessListener(tarefasSnapshot -> {

                                for (DocumentSnapshot tarefaDoc : tarefasSnapshot.getDocuments()) {

                                    String userId = tarefaDoc.getString("userId");

                                    Long percentualLong = tarefaDoc.getLong("percentual");

                                    int percentual = percentualLong != null
                                            ? percentualLong.intValue()
                                            : 0;

                                    if (userId == null || !usuarioLojaId.containsKey(userId)) {
                                        continue;
                                    }

                                    String lojaId = usuarioLojaId.get(userId);

                                    String lojaNome = usuarioLojaNome.get(userId);

                                    String nomeUsuario = usuarioNome.get(userId);

                                    LojaRankingTemp temp = rankingLojas.get(lojaId);

                                    if (temp == null) {

                                        temp = new LojaRankingTemp(
                                                lojaNome,
                                                nomeUsuario
                                        );

                                        rankingLojas.put(lojaId, temp);
                                    }

                                    temp.somaPercentual += percentual;
                                    temp.quantidadeTarefas++;
                                }

                                List<LojaRanking> listaFinal = new ArrayList<>();

                                for (LojaRankingTemp temp : rankingLojas.values()) {

                                    int media = temp.quantidadeTarefas > 0
                                            ? temp.somaPercentual / temp.quantidadeTarefas
                                            : 0;

                                    listaFinal.add(
                                            new LojaRanking(
                                                    0,
                                                    temp.nomeLoja,
                                                    temp.nomeResponsavel,
                                                    media
                                            )
                                    );
                                }

                                listaFinal.sort(
                                        (a, b) -> b.getPontos() - a.getPontos()
                                );

                                for (int i = 0; i < listaFinal.size(); i++) {
                                    listaFinal.get(i).setPosicao(i + 1);
                                }

                                LojaRankingAdapter adapter =
                                        new LojaRankingAdapter(listaFinal);

                                rvRankingLojas.setAdapter(adapter);
                            });
                });
    }

    private static class LojaRankingTemp {

        String nomeLoja;
        String nomeResponsavel;

        int somaPercentual = 0;
        int quantidadeTarefas = 0;

        LojaRankingTemp(String nomeLoja, String nomeResponsavel) {
            this.nomeLoja = nomeLoja;
            this.nomeResponsavel = nomeResponsavel;
        }
    }
}