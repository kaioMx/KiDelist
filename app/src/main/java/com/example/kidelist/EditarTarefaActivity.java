package com.example.kidelist;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EditarTarefaActivity extends AppCompatActivity {

    private EditText etTitulo, etDescricao;
    private Button btnAtualizarTarefa;
    private ImageView btnVoltar;

    private boolean modoEdicao = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_tarefa);

        FooterNavigation.setup(this, FooterNavigation.TELA_TAREFAS);

        etTitulo = findViewById(R.id.etTitulo);
        etDescricao = findViewById(R.id.etDescricao);
        btnAtualizarTarefa = findViewById(R.id.btnAtualizarTarefa);
        btnVoltar = findViewById(R.id.btnVoltar);

        btnVoltar.setOnClickListener(v -> finish());

        carregarDadosDaTarefa();
        bloquearCampos();

        btnAtualizarTarefa.setText("Editar tarefa");

        btnAtualizarTarefa.setOnClickListener(v -> {
            if (!modoEdicao) {
                liberarCampos();
                btnAtualizarTarefa.setText("Salvar alterações");
                modoEdicao = true;
            } else {
                salvarAlteracoes();
            }
        });
    }

    private void carregarDadosDaTarefa() {
        String nome = getIntent().getStringExtra("nome");
        String descricao = getIntent().getStringExtra("descricao");

        if (nome != null) {
            etTitulo.setText(nome);
        }

        if (descricao != null) {
            etDescricao.setText(descricao);
        } else {
            etDescricao.setText("Sem descrição cadastrada");
        }
    }

    private void bloquearCampos() {
        etTitulo.setEnabled(false);
        etDescricao.setEnabled(false);

        etTitulo.setAlpha(0.8f);
        etDescricao.setAlpha(0.8f);
    }

    private void liberarCampos() {
        etTitulo.setEnabled(true);
        etDescricao.setEnabled(true);

        etTitulo.setAlpha(1f);
        etDescricao.setAlpha(1f);

        etTitulo.requestFocus();
    }

    private void salvarAlteracoes() {
        String titulo = etTitulo.getText().toString().trim();
        String descricao = etDescricao.getText().toString().trim();

        if (titulo.isEmpty()) {
            etTitulo.setError("Informe o título da tarefa");
            etTitulo.requestFocus();
            return;
        }

        Toast.makeText(this, "Tarefa atualizada!", Toast.LENGTH_SHORT).show();

        bloquearCampos();
        btnAtualizarTarefa.setText("Editar tarefa");
        modoEdicao = false;
    }
}