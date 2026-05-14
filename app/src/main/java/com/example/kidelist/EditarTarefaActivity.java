package com.example.kidelist;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EditarTarefaActivity extends AppCompatActivity {

    private EditText etTitulo, etDescricao;
    private Button btnAtualizarTarefa;
    private ImageView btnVoltar;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private boolean usuarioGerente = false;
    private Button btnSelecionarData, btnSelecionarHora;
    private CheckBox cbRepetir;
    private Spinner spTipoRepeticao, spUsuarios;
    private Map<String, String> mapaUsuarios = new HashMap<>();

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
        btnSelecionarData = findViewById(R.id.btnSelecionarData);
        btnSelecionarHora = findViewById(R.id.btnSelecionarHora);
        cbRepetir = findViewById(R.id.cbRepetir);
        spTipoRepeticao = findViewById(R.id.spTipoRepeticao);
        spUsuarios = findViewById(R.id.spUsuarios);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        carregarDadosDaTarefa();
        carregarUsuarios();

        bloquearCampos();

        btnVoltar.setOnClickListener(v -> finish());

        btnAtualizarTarefa.setVisibility(View.GONE);

        btnAtualizarTarefa.setVisibility(View.GONE);

        btnSelecionarData.setVisibility(View.VISIBLE);
        btnSelecionarHora.setVisibility(View.VISIBLE);

        btnSelecionarData.setEnabled(false);
        btnSelecionarHora.setEnabled(false);

        cbRepetir.setVisibility(View.GONE);
        spTipoRepeticao.setVisibility(View.GONE);

        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            db.collection("usuarios")
                    .document(user.getUid())
                    .get()
                    .addOnSuccessListener(doc -> {
                        String tipo = doc.getString("tipo");

                        if ("Gerente".equals(tipo)) {
                            usuarioGerente = true;

                            btnAtualizarTarefa.setVisibility(View.VISIBLE);
                            btnSelecionarData.setVisibility(View.VISIBLE);
                            btnSelecionarHora.setVisibility(View.VISIBLE);
                            cbRepetir.setVisibility(View.VISIBLE);

                            btnAtualizarTarefa.setText("Editar tarefa");

                            btnAtualizarTarefa.setOnClickListener(v -> {
                                if (!usuarioGerente) return;

                                if (!modoEdicao) {
                                    liberarCampos();
                                    btnAtualizarTarefa.setText("Salvar alterações");
                                    modoEdicao = true;
                                } else {
                                    salvarAlteracoes();
                                }
                            });

                        } else {
                            usuarioGerente = false;

                            bloquearCampos();

                            btnAtualizarTarefa.setVisibility(View.GONE);

                            btnSelecionarData.setVisibility(View.VISIBLE);
                            btnSelecionarHora.setVisibility(View.VISIBLE);

                            btnSelecionarData.setEnabled(false);
                            btnSelecionarHora.setEnabled(false);

                            btnSelecionarData.setAlpha(0.8f);
                            btnSelecionarHora.setAlpha(0.8f);

                            cbRepetir.setVisibility(View.GONE);
                            spTipoRepeticao.setVisibility(View.GONE);
                        }
                    })
                    .addOnFailureListener(e -> {
                        usuarioGerente = false;

                        btnAtualizarTarefa.setVisibility(View.GONE);

                        btnSelecionarData.setVisibility(View.VISIBLE);
                        btnSelecionarHora.setVisibility(View.VISIBLE);

                        btnSelecionarData.setEnabled(false);
                        btnSelecionarHora.setEnabled(false);

                        btnSelecionarData.setAlpha(0.8f);
                        btnSelecionarHora.setAlpha(0.8f);

                        cbRepetir.setVisibility(View.GONE);
                        spTipoRepeticao.setVisibility(View.GONE);
                    });
        }
    }

    private void carregarDadosDaTarefa() {

        String nome = getIntent().getStringExtra("nome");
        String descricao = getIntent().getStringExtra("descricao");
        String data = getIntent().getStringExtra("data");
        String hora = getIntent().getStringExtra("hora");

        if (nome != null) {
            etTitulo.setText(nome);
        }

        if (descricao != null) {
            etDescricao.setText(descricao);
        } else {
            etDescricao.setText("Sem descrição cadastrada");
        }

        if (data != null) {
            btnSelecionarData.setText(data);
        } else {
            btnSelecionarData.setText("Sem data");
        }

        if (hora != null) {
            btnSelecionarHora.setText(hora);
        } else {
            btnSelecionarHora.setText("Sem hora");
        }
    }

    private void bloquearCampos() {
        etTitulo.setEnabled(false);
        etDescricao.setEnabled(false);

        spUsuarios.setEnabled(false);
        spUsuarios.setClickable(false);
        spUsuarios.setAlpha(0.8f);

        etTitulo.setAlpha(0.8f);
        etDescricao.setAlpha(0.8f);
    }

    private void liberarCampos() {
        etTitulo.setEnabled(true);
        etDescricao.setEnabled(true);

        spUsuarios.setEnabled(true);
        spUsuarios.setClickable(true);
        spUsuarios.setAlpha(1f);

        etTitulo.setAlpha(1f);
        etDescricao.setAlpha(1f);

        etTitulo.requestFocus();
    }

    private void salvarAlteracoes() {

        String titulo = etTitulo.getText().toString().trim();
        String descricao = etDescricao.getText().toString().trim();

        String data = btnSelecionarData.getText().toString().trim();
        String hora = btnSelecionarHora.getText().toString().trim();

        if (titulo.isEmpty()) {
            etTitulo.setError("Informe o título da tarefa");
            etTitulo.requestFocus();
            return;
        }

        String tarefaId = getIntent().getStringExtra("tarefaId");

        if (tarefaId == null) {
            Toast.makeText(this, "Erro ao encontrar tarefa", Toast.LENGTH_SHORT).show();
            return;
        }

        String usuarioSelecionado = spUsuarios.getSelectedItem().toString();

        if (usuarioSelecionado.equals("Selecione um usuário")) {
            Toast.makeText(this, "Selecione o responsável", Toast.LENGTH_SHORT).show();
            return;
        }

        String usuarioResponsavelId = mapaUsuarios.get(usuarioSelecionado);

        Map<String, Object> dados = new HashMap<>();
        dados.put("titulo", titulo);
        dados.put("descricao", descricao);
        dados.put("data", data);
        dados.put("hora", hora);
        dados.put("userId", usuarioResponsavelId);
        dados.put("responsavelNome", usuarioSelecionado);

        db.collection("tarefas")
                .document(tarefaId)
                .update(dados)
                .addOnSuccessListener(unused -> {

                    Toast.makeText(this, "Tarefa atualizada!", Toast.LENGTH_SHORT).show();

                    bloquearCampos();

                    btnAtualizarTarefa.setText("Editar tarefa");

                    modoEdicao = false;
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao atualizar tarefa", Toast.LENGTH_SHORT).show();
                });
    }

    private void carregarUsuarios() {
        db.collection("usuarios")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<String> nomesUsuarios = new ArrayList<>();
                    mapaUsuarios.clear();

                    nomesUsuarios.add("Selecione um usuário");

                    String responsavelAtual = getIntent().getStringExtra("responsavelNome");

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String nome = doc.getString("nome");

                        if (nome != null) {
                            nomesUsuarios.add(nome);
                            mapaUsuarios.put(nome, doc.getId());
                        }
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            this,
                            android.R.layout.simple_spinner_item,
                            nomesUsuarios
                    );

                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spUsuarios.setAdapter(adapter);

                    if (responsavelAtual != null) {
                        int posicao = nomesUsuarios.indexOf(responsavelAtual);
                        if (posicao >= 0) {
                            spUsuarios.setSelection(posicao);
                        }
                    }

                    spUsuarios.setEnabled(false);
                    spUsuarios.setAlpha(0.8f);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao carregar usuários", Toast.LENGTH_SHORT).show();
                });
    }
}