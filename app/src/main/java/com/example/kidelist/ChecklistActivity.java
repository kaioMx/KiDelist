package com.example.kidelist;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
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

        adapter = new TarefaChecklistAdapter(tarefas, () -> {

            FirebaseUser currentUser = auth.getCurrentUser();

            if (currentUser != null) {
                calcularPercentualUsuario(currentUser.getUid());
            }
        });
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
            calcularPercentualUsuario(user.getUid());
        }
    }

    private void carregarTarefas(String uid) {
        db.collection("tarefas")
                //.whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    tarefas.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        String titulo = doc.getString("titulo");
                        String descricao = doc.getString("descricao");
                        Boolean concluida = doc.getBoolean("concluida");
                        Long notaLong = doc.getLong("nota");
                        String fotoUrl = doc.getString("fotoUrl");

                        TarefaChecklist tarefa = new TarefaChecklist();

                        tarefa.setId(doc.getId());
                        tarefa.setNome(titulo != null ? titulo : "Sem título");
                        tarefa.setDescricao(descricao != null ? descricao : "");
                        tarefa.setFeito(concluida != null && concluida);
                        tarefa.setNota(notaLong != null ? notaLong.intValue() : 0);
                        tarefa.setFotoLocal(fotoUrl != null ? fotoUrl : "");
                        tarefa.setUserId(doc.getString("userId"));

                        tarefas.add(tarefa);
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao carregar tarefas", Toast.LENGTH_SHORT).show()
                );
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
                        //carregarNotaMensal(lojaId);
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

    /*private void carregarNotaMensal(String lojaId) {
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
    }*/

    private int getYYYYMM() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.YEAR) * 100 + (cal.get(Calendar.MONTH) + 1);
    }

    private void calcularPercentualUsuario(String uid) {

        db.collection("tarefas")
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(query -> {

                    int totalTarefas = query.size();

                    if (totalTarefas == 0) {
                        txtNotaMensal.setText("Nota Mensal: 0%");
                        return;
                    }

                    double percentualTotal = 0;

                    for (DocumentSnapshot doc : query.getDocuments()) {

                        Long notaLong = doc.getLong("nota");

                        int nota = notaLong != null
                                ? notaLong.intValue()
                                : 0;

                        double percentualParcial = 100.0 / totalTarefas;

                        double valorPorNota = percentualParcial / 5.0;

                        percentualTotal += valorPorNota * nota;
                    }

                    percentualTotal =
                            Math.round(percentualTotal * 100.0) / 100.0;

                    txtNotaMensal.setText(
                            "Nota Mensal: " + percentualTotal + "%"
                    );
                });
    }
}