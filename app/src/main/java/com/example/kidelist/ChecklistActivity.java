package com.example.kidelist;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

        cardNovaTarefa.setVisibility(View.GONE);

        db.collection("usuarios").document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    String tipo = doc.getString("tipo");

                    if ("Gerente".equals(tipo)) {
                        cardNovaTarefa.setVisibility(View.VISIBLE);
                    } else {
                        cardNovaTarefa.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    cardNovaTarefa.setVisibility(View.GONE);
                });

        carregarHeader(user.getUid());

        adapter = new TarefaChecklistAdapter(tarefas, () -> {
            FirebaseUser currentUser = auth.getCurrentUser();

            if (currentUser != null) {
                //carregarTarefas(currentUser.getUid());
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
        db.collection("usuarios")
                .document(uid)
                .get()
                .addOnSuccessListener(userDoc -> {
                    String tipo = userDoc.getString("tipo");

                    if ("Gerente".equals(tipo)) {
                        carregarTodasTarefas();
                    } else {
                        carregarTarefasUsuario(uid);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao verificar usuário", Toast.LENGTH_SHORT).show()
                );
    }

    private void carregarTodasTarefas() {
        db.collection("tarefas")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    tarefas.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {

                        TarefaChecklist tarefa = new TarefaChecklist();

                        tarefa.setId(doc.getId());

                        tarefa.setNome(doc.getString("titulo") != null
                                ? doc.getString("titulo")
                                : "Sem título");

                        tarefa.setDescricao(doc.getString("descricao") != null
                                ? doc.getString("descricao")
                                : "");

                        tarefa.setFotoLocal(doc.getString("fotoUrl") != null
                                ? doc.getString("fotoUrl")
                                : "");

                        tarefa.setData(doc.getString("data") != null
                                ? doc.getString("data")
                                : "");

                        tarefa.setHora(doc.getString("hora") != null
                                ? doc.getString("hora")
                                : "");

                        tarefa.setUserId(doc.getString("userId"));

                        tarefa.setResponsavelNome(
                                doc.getString("responsavelNome") != null
                                        ? doc.getString("responsavelNome")
                                        : ""
                        );

                        tarefa.setNota(0);
                        tarefa.setFeito(false);

                        tarefas.add(tarefa);
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao carregar todas as tarefas", Toast.LENGTH_SHORT).show()
                );
    }

    private void carregarTarefasUsuario(String uid) {
        db.collection("tarefas")
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    tarefas.clear();

                    if (queryDocumentSnapshots.isEmpty()) {
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    final int total = queryDocumentSnapshots.size();
                    final int[] carregadas = {0};

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        TarefaChecklist tarefa = new TarefaChecklist();

                        tarefa.setId(doc.getId());
                        tarefa.setNome(doc.getString("titulo") != null ? doc.getString("titulo") : "Sem título");
                        tarefa.setDescricao(doc.getString("descricao") != null ? doc.getString("descricao") : "");
                        tarefa.setFotoLocal(doc.getString("fotoUrl") != null ? doc.getString("fotoUrl") : "");
                        tarefa.setData(doc.getString("data") != null ? doc.getString("data") : "");
                        tarefa.setHora(doc.getString("hora") != null ? doc.getString("hora") : "");
                        tarefa.setUserId(uid);
                        tarefa.setNota(0);
                        tarefa.setFeito(false);

                        db.collection("tarefas")
                                .document(doc.getId())
                                .collection("notas")
                                .document(uid)
                                .get()
                                .addOnSuccessListener(notaDoc -> {
                                    if (notaDoc.exists()) {
                                        Long notaLong = notaDoc.getLong("nota");
                                        Boolean concluida = notaDoc.getBoolean("concluida");

                                        tarefa.setNota(notaLong != null ? notaLong.intValue() : 0);
                                        tarefa.setFeito(concluida != null && concluida);
                                    }

                                    tarefas.add(tarefa);
                                    carregadas[0]++;

                                    if (carregadas[0] == total) {
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao carregar tarefas", Toast.LENGTH_SHORT).show()
                );
    }

    /*private void carregarTarefas(String uid) {
        db.collection("tarefas")
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    tarefas.clear();

                    if (queryDocumentSnapshots.isEmpty()) {
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    final int total = queryDocumentSnapshots.size();
                    final int[] carregadas = {0};

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {

                        String data = doc.getString("data");
                        String hora = doc.getString("hora");

                        TarefaChecklist tarefa = new TarefaChecklist();

                        tarefa.setId(doc.getId());
                        tarefa.setNome(doc.getString("titulo") != null ? doc.getString("titulo") : "Sem título");
                        tarefa.setDescricao(doc.getString("descricao") != null ? doc.getString("descricao") : "");
                        tarefa.setFotoLocal(doc.getString("fotoUrl") != null ? doc.getString("fotoUrl") : "");
                        tarefa.setData(data != null ? data : "");
                        tarefa.setHora(hora != null ? hora : "");
                        tarefa.setUserId(uid);
                        tarefa.setNota(0);
                        tarefa.setFeito(false);

                        db.collection("tarefas")
                                .document(doc.getId())
                                .collection("notas")
                                .document(uid)
                                .get()
                                .addOnSuccessListener(notaDoc -> {

                                    if (notaDoc.exists()) {
                                        Long notaLong = notaDoc.getLong("nota");
                                        Boolean concluida = notaDoc.getBoolean("concluida");

                                        tarefa.setNota(notaLong != null ? notaLong.intValue() : 0);
                                        tarefa.setFeito(concluida != null && concluida);
                                    }

                                    tarefas.add(tarefa);
                                    carregadas[0]++;

                                    if (carregadas[0] == total) {
                                        adapter.notifyDataSetChanged();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    tarefas.add(tarefa);
                                    carregadas[0]++;

                                    if (carregadas[0] == total) {
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao carregar tarefas", Toast.LENGTH_SHORT).show()
                );
    }*/

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
                .get()
                .addOnSuccessListener(query -> {

                    final double[] somaNotas = {0};
                    final int[] tarefasComNota = {0};
                    final int[] tarefasProcessadas = {0};

                    int totalTarefasGlobais = query.size();

                    if (totalTarefasGlobais == 0) {
                        txtNotaMensal.setText("Nota Mensal: 0%");
                        return;
                    }

                    for (DocumentSnapshot tarefaDoc : query.getDocuments()) {

                        db.collection("tarefas")
                                .document(tarefaDoc.getId())
                                .collection("notas")
                                .document(uid)
                                .get()
                                .addOnSuccessListener(notaDoc -> {

                                    if (notaDoc.exists()) {
                                        Long notaLong = notaDoc.getLong("nota");

                                        int nota = notaLong != null ? notaLong.intValue() : 0;

                                        somaNotas[0] += nota;
                                        tarefasComNota[0]++;
                                    }

                                    tarefasProcessadas[0]++;

                                    if (tarefasProcessadas[0] == totalTarefasGlobais) {

                                        double percentualTotal = 0;

                                        if (tarefasComNota[0] > 0) {
                                            double mediaNotas = somaNotas[0] / tarefasComNota[0];
                                            percentualTotal = (mediaNotas / 5.0) * 100.0;
                                        }

                                        percentualTotal = Math.round(percentualTotal * 100.0) / 100.0;

                                        txtNotaMensal.setText("Nota Mensal: " + percentualTotal + "%");
                                    }
                                });
                    }
                });
    }
}