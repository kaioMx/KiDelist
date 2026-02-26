package com.example.kidelist;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
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

        RecyclerView rv = findViewById(R.id.rvTarefas);
        MaterialButton btnSalvar = findViewById(R.id.btnSalvar);
        MaterialCardView cardNovaTarefa = findViewById(R.id.cardNovaTarefa);

        //teste do login
        txtTitulo = findViewById(R.id.txtTitulo);
        txtGerente = findViewById(R.id.txtGerente);
        txtNotaMensal = findViewById(R.id.txtNotaMensal);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            finish(); // ou volta pro login
            return;
        }

        carregarHeader(user.getUid());

        // Dados fake (exemplo)
        // tarefas.add(new TarefaChecklist("Nome_Tarefa", 3, true));
        //tarefas.add(new TarefaChecklist("Nome_Tarefa", 0, false));
        // tarefas.add(new TarefaChecklist("Nome_Tarefa", 3, true));

        adapter = new TarefaChecklistAdapter(tarefas);

        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        cardNovaTarefa.setOnClickListener(v -> {
            tarefas.add(0, new TarefaChecklist("Nova tarefa", 0, false));
            adapter.notifyItemInserted(0);
            rv.scrollToPosition(0);
        });

        btnSalvar.setOnClickListener(v -> {
            // Aqui você envia pro banco/api/etc.
            Toast.makeText(this, "Salvo! (" + tarefas.size() + " tarefas)", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
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

                    // Se você quer “percentual”, precisa definir uma regra.
                    // Por enquanto vou mostrar pontos:
                    txtNotaMensal.setText("Pontos mês: " + (int) pontosTotal);
                });
    }

    private int getYYYYMM() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.YEAR) * 100 + (cal.get(Calendar.MONTH) + 1);
    }
}
