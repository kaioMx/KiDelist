package com.example.kidelist;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ChecklistActivity extends AppCompatActivity {

    private final List<TarefaChecklist> tarefas = new ArrayList<>();
    private TarefaChecklistAdapter adapter;

    private TextView txtTitulo, txtGerente, txtNotaMensal;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checklist);

        FooterNavigation.setup(this, FooterNavigation.TELA_TAREFAS);

        RecyclerView rv = findViewById(R.id.rvTarefas);
        MaterialCardView cardNovaTarefa = findViewById(R.id.cardNovaTarefa);

        txtTitulo = findViewById(R.id.txtTitulo);
        txtGerente = findViewById(R.id.txtGerente);
        txtNotaMensal = findViewById(R.id.txtNotaMensal);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            finish();
            return;
        }

        carregarHeader(user.getUid());

        adapter = new TarefaChecklistAdapter(tarefas);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        cardNovaTarefa.setOnClickListener(v -> {
            Intent intent = new Intent(ChecklistActivity.this, CriarTarefaActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            carregarTarefas(user.getUid());
        }
    }

    private void carregarTarefas(String uid) {
        db.collection("tarefas")
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    tarefas.clear();

                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        String titulo = doc.getString("titulo");
                        Boolean concluida = doc.getBoolean("concluida");

                        TarefaChecklist tarefa = new TarefaChecklist();
                        tarefa.setNome(titulo != null ? titulo : "Sem título");
                        tarefa.setFeito(concluida != null && concluida);
                        tarefa.setNota(0);

                        tarefas.add(tarefa);
                    }

                    adapter.notifyDataSetChanged();
                });
    }

    private void carregarHeader(String uid) {
        db.collection("usuarios").document(uid).get()
                .addOnSuccessListener(userDoc -> {
                    if (!userDoc.exists()) return;

                    String nome = userDoc.getString("nome");
                    String lojaId = userDoc.getString("lojaId");

                    if (nome != null) txtGerente.setText(nome);

                    if (lojaId != null) {
                        carregarNomeLoja(lojaId);
                        carregarNotaMensal(lojaId);
                    }
                });
    }

    private void carregarNomeLoja(String lojaId) {
        db.collection("lojas").document(lojaId).get()
                .addOnSuccessListener(lojaDoc -> {
                    String nomeLoja = lojaDoc.getString("nome");
                    if (nomeLoja == null) nomeLoja = "Loja";

                    txtTitulo.setText("CheckList - " + nomeLoja);
                });
    }

    private void carregarNotaMensal(String lojaId) {
        int yyyymm = getYYYYMM();
        String docId = lojaId + "_" + yyyymm;

        db.collection("rankingMensal").document(docId).get()
                .addOnSuccessListener(rankDoc -> {
                    double pontosTotal = 0;
                    if (rankDoc.exists() && rankDoc.get("pontosTotal") != null) {
                        Number n = (Number) rankDoc.get("pontosTotal");
                        pontosTotal = n.doubleValue();
                    }

                    txtNotaMensal.setText("Pontos mês: " + (int) pontosTotal);
                });
    }

    private int getYYYYMM() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.YEAR) * 100 + (cal.get(Calendar.MONTH) + 1);
    }
}