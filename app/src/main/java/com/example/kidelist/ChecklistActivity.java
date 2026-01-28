package com.example.kidelist;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class ChecklistActivity extends AppCompatActivity {

    private final List<TarefaChecklist> tarefas = new ArrayList<>();
    private TarefaChecklistAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checklist);

        RecyclerView rv = findViewById(R.id.rvTarefas);
        MaterialButton btnSalvar = findViewById(R.id.btnSalvar);
        MaterialCardView cardNovaTarefa = findViewById(R.id.cardNovaTarefa);

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
}
