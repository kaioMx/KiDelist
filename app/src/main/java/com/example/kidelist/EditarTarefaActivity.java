
package com.example.kidelist;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
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

    private EditText etTitulo;
    private EditText etDescricao;

    private Button btnAtualizarTarefa;
    private ImageView btnVoltar;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private boolean usuarioGerente = false;
    private boolean modoEdicao = false;

    private Button btnSelecionarData;
    private Button btnSelecionarHora;

    private CheckBox cbRepetir;

    private Spinner spTipoRepeticao;
    private Spinner spUsuarios;

    private Map<String, String> mapaUsuarios =
            new HashMap<>();

    // Componentes da seção de avaliações.
    private LinearLayout secaoAvaliacoes;
    private LinearLayout layoutAvaliacoes;
    private TextView txtMediaAvaliacoes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_editar_tarefa);

        FooterNavigation.setup(
                this,
                FooterNavigation.TELA_TAREFAS
        );

        etTitulo = findViewById(R.id.etTitulo);
        etDescricao = findViewById(R.id.etDescricao);

        btnAtualizarTarefa =
                findViewById(R.id.btnAtualizarTarefa);

        btnVoltar =
                findViewById(R.id.btnVoltar);

        btnSelecionarData =
                findViewById(R.id.btnSelecionarData);

        btnSelecionarHora =
                findViewById(R.id.btnSelecionarHora);

        cbRepetir =
                findViewById(R.id.cbRepetir);

        spTipoRepeticao =
                findViewById(R.id.spTipoRepeticao);

        spUsuarios =
                findViewById(R.id.spUsuarios);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        criarSecaoAvaliacoes();

        carregarDadosDaTarefa();
        carregarUsuarios();

        bloquearCampos();

        btnVoltar.setOnClickListener(v -> finish());

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

                        if (!doc.exists()) {
                            configurarModoFuncionario();
                            return;
                        }

                        String tipo = doc.getString("tipo");

                        if ("Gerente".equals(tipo)) {

                            configurarModoGerente();

                        } else {

                            configurarModoFuncionario();
                        }
                    })
                    .addOnFailureListener(e -> {

                        configurarModoFuncionario();

                        Toast.makeText(
                                this,
                                "Erro ao verificar permissões.",
                                Toast.LENGTH_SHORT
                        ).show();
                    });

        } else {

            configurarModoFuncionario();
        }
    }

    /*
     * Cria a seção de avaliações dentro do card existente.
     * Assim não é necessário adicionar IDs ao XML.
     */
    private void criarSecaoAvaliacoes() {

        ScrollView scrollFormulario =
                findViewById(R.id.scrollFormulario);

        if (scrollFormulario == null
                || scrollFormulario.getChildCount() == 0) {
            return;
        }

        View primeiroFilho =
                scrollFormulario.getChildAt(0);

        if (!(primeiroFilho instanceof LinearLayout)) {
            return;
        }

        LinearLayout conteudo =
                (LinearLayout) primeiroFilho;

        if (conteudo.getChildCount() == 0) {
            return;
        }

        View segundoFilho =
                conteudo.getChildAt(0);

        if (!(segundoFilho instanceof LinearLayout)) {
            return;
        }

        LinearLayout card =
                (LinearLayout) segundoFilho;

        secaoAvaliacoes = new LinearLayout(this);
        secaoAvaliacoes.setOrientation(
                LinearLayout.VERTICAL
        );

        secaoAvaliacoes.setVisibility(View.GONE);

        TextView titulo = new TextView(this);

        titulo.setText(
                "Avaliações dos funcionários"
        );

        titulo.setTextColor(0xFF33428C);
        titulo.setTextSize(18);
        titulo.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        titulo.setPadding(0, 24, 0, 12);

        txtMediaAvaliacoes = new TextView(this);

        txtMediaAvaliacoes.setTextColor(0xFF0E3B66);
        txtMediaAvaliacoes.setTextSize(16);
        txtMediaAvaliacoes.setPadding(0, 4, 0, 12);

        layoutAvaliacoes = new LinearLayout(this);

        layoutAvaliacoes.setOrientation(
                LinearLayout.VERTICAL
        );

        secaoAvaliacoes.addView(titulo);
        secaoAvaliacoes.addView(txtMediaAvaliacoes);
        secaoAvaliacoes.addView(layoutAvaliacoes);

        int posicaoBotao =
                card.indexOfChild(btnAtualizarTarefa);

        if (posicaoBotao >= 0) {

            card.addView(
                    secaoAvaliacoes,
                    posicaoBotao
            );

        } else {

            card.addView(secaoAvaliacoes);
        }
    }

    /*
     * Configuração para gerente.
     */
    private void configurarModoGerente() {

        usuarioGerente = true;

        bloquearCampos();

        btnAtualizarTarefa.setVisibility(View.VISIBLE);

        btnSelecionarData.setVisibility(View.VISIBLE);
        btnSelecionarHora.setVisibility(View.VISIBLE);

        btnSelecionarData.setEnabled(false);
        btnSelecionarHora.setEnabled(false);

        btnSelecionarData.setAlpha(0.8f);
        btnSelecionarHora.setAlpha(0.8f);

        cbRepetir.setVisibility(View.VISIBLE);

        secaoAvaliacoes.setVisibility(View.VISIBLE);

        btnAtualizarTarefa.setText("Editar tarefa");

        btnAtualizarTarefa.setOnClickListener(v -> {

            if (!usuarioGerente) {
                return;
            }

            if (!modoEdicao) {

                liberarCampos();

                btnAtualizarTarefa.setText(
                        "Salvar alterações"
                );

                modoEdicao = true;

            } else {

                salvarAlteracoes();
            }
        });

        String tarefaId =
                getIntent().getStringExtra("tarefaId");

        if (tarefaId != null && !tarefaId.isEmpty()) {
            carregarAvaliacoes(tarefaId);
        }
    }

    /*
     * Configuração para funcionário.
     */
    private void configurarModoFuncionario() {

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

        if (secaoAvaliacoes != null) {
            secaoAvaliacoes.setVisibility(View.GONE);
        }
    }

    /*
     * Carrega os dados recebidos da tela anterior.
     */
    private void carregarDadosDaTarefa() {

        String nome =
                getIntent().getStringExtra("nome");

        String descricao =
                getIntent().getStringExtra("descricao");

        String data =
                getIntent().getStringExtra("data");

        String hora =
                getIntent().getStringExtra("hora");

        if (nome != null) {
            etTitulo.setText(nome);
        }

        if (descricao != null) {
            etDescricao.setText(descricao);
        } else {
            etDescricao.setText(
                    "Sem descrição cadastrada"
            );
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

    /*
     * Bloqueia os campos.
     */
    private void bloquearCampos() {

        etTitulo.setEnabled(false);
        etDescricao.setEnabled(false);

        spUsuarios.setEnabled(false);
        spUsuarios.setClickable(false);
        spUsuarios.setAlpha(0.8f);

        etTitulo.setAlpha(0.8f);
        etDescricao.setAlpha(0.8f);
    }

    /*
     * Libera os campos para edição pelo gerente.
     */
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

    /*
     * Salva alterações da tarefa.
     */
    private void salvarAlteracoes() {

        if (!usuarioGerente) {
            return;
        }

        String titulo =
                etTitulo.getText().toString().trim();

        String descricao =
                etDescricao.getText().toString().trim();

        String data =
                btnSelecionarData.getText().toString().trim();

        String hora =
                btnSelecionarHora.getText().toString().trim();

        if (titulo.isEmpty()) {

            etTitulo.setError(
                    "Informe o título da tarefa"
            );

            etTitulo.requestFocus();
            return;
        }

        String tarefaId =
                getIntent().getStringExtra("tarefaId");

        if (tarefaId == null || tarefaId.isEmpty()) {

            Toast.makeText(
                    this,
                    "Erro ao encontrar tarefa.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (spUsuarios.getSelectedItem() == null) {

            Toast.makeText(
                    this,
                    "Selecione o responsável.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String usuarioSelecionado =
                spUsuarios.getSelectedItem().toString();

        if ("Selecione um usuário".equals(
                usuarioSelecionado
        )) {

            Toast.makeText(
                    this,
                    "Selecione o responsável.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String usuarioResponsavelId =
                mapaUsuarios.get(usuarioSelecionado);

        if (usuarioResponsavelId == null) {

            Toast.makeText(
                    this,
                    "Não foi possível identificar o responsável.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        Map<String, Object> dados =
                new HashMap<>();

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

                    Toast.makeText(
                            this,
                            "Tarefa atualizada!",
                            Toast.LENGTH_SHORT
                    ).show();

                    bloquearCampos();

                    btnAtualizarTarefa.setText(
                            "Editar tarefa"
                    );

                    modoEdicao = false;

                    // Atualiza os dados exibidos na tela anterior.
                    setResult(RESULT_OK);

                    // Recarrega as avaliações da tarefa.
                    carregarAvaliacoes(tarefaId);
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "Erro ao atualizar tarefa: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    /*
     * Carrega todos os documentos da subcoleção notas.
     *
     * Isso permite visualizar avaliações antigas e atuais,
     * inclusive depois que a tarefa foi concluída.
     */
    private void carregarAvaliacoes(String tarefaId) {

        if (!usuarioGerente || layoutAvaliacoes == null) {
            return;
        }

        layoutAvaliacoes.removeAllViews();

        txtMediaAvaliacoes.setText(
                "Calculando média..."
        );

        db.collection("tarefas")
                .document(tarefaId)
                .collection("notas")
                .get()
                .addOnSuccessListener(resultado -> {

                    if (resultado.isEmpty()) {

                        txtMediaAvaliacoes.setText(
                                "Média: sem avaliações"
                        );

                        adicionarTextoAvaliacao(
                                "Ainda não existem avaliações para esta tarefa."
                        );

                        return;
                    }

                    final int total =
                            resultado.size();

                    final int[] processadas = {0};
                    final int[] quantidadeNotas = {0};
                    final double[] somaNotas = {0};

                    for (DocumentSnapshot doc :
                            resultado.getDocuments()) {

                        String funcionarioId =
                                doc.getString("userId");

                        if (funcionarioId == null) {
                            funcionarioId = doc.getId();
                        }

                        Long nota =
                                doc.getLong("nota");

                        Boolean concluida =
                                doc.getBoolean("concluida");

                        final String idUsuario =
                                funcionarioId;

                        final Long notaFuncionario =
                                nota;

                        final Boolean tarefaConcluida =
                                concluida;

                        db.collection("usuarios")
                                .document(idUsuario)
                                .get()
                                .addOnSuccessListener(usuarioDoc -> {

                                    String nome =
                                            usuarioDoc.getString("nome");

                                    if (nome == null
                                            || nome.isEmpty()) {
                                        nome = "Funcionário";
                                    }

                                    adicionarLinhaAvaliacao(
                                            nome,
                                            notaFuncionario,
                                            tarefaConcluida
                                    );

                                    if (notaFuncionario != null) {

                                        somaNotas[0] +=
                                                notaFuncionario;

                                        quantidadeNotas[0]++;
                                    }

                                    processadas[0]++;

                                    finalizarCarregamentoAvaliacoes(
                                            processadas[0],
                                            total,
                                            somaNotas[0],
                                            quantidadeNotas[0]
                                    );
                                })
                                .addOnFailureListener(e -> {

                                    adicionarLinhaAvaliacao(
                                            "Funcionário",
                                            notaFuncionario,
                                            tarefaConcluida
                                    );

                                    if (notaFuncionario != null) {

                                        somaNotas[0] +=
                                                notaFuncionario;

                                        quantidadeNotas[0]++;
                                    }

                                    processadas[0]++;

                                    finalizarCarregamentoAvaliacoes(
                                            processadas[0],
                                            total,
                                            somaNotas[0],
                                            quantidadeNotas[0]
                                    );
                                });
                    }
                })
                .addOnFailureListener(e -> {

                    txtMediaAvaliacoes.setText(
                            "Não foi possível carregar as avaliações."
                    );

                    Toast.makeText(
                            this,
                            "Erro ao carregar avaliações: "
                                    + e.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    /*
     * Adiciona a avaliação de um funcionário à tela.
     */
    private void adicionarLinhaAvaliacao(
            String nome,
            Long nota,
            Boolean concluida
    ) {

        String textoNota =
                nota != null
                        ? nota + "/5"
                        : "Ainda não avaliado";

        String status =
                Boolean.TRUE.equals(concluida)
                        ? "Concluída"
                        : "Pendente";

        TextView item = new TextView(this);

        item.setText(
                nome
                        + "\nNota: " + textoNota
                        + "\nStatus: " + status
        );

        item.setTextColor(0xFF0E3B66);
        item.setTextSize(15);

        item.setPadding(
                12,
                14,
                12,
                14
        );

        layoutAvaliacoes.addView(item);

        View divisor = new View(this);

        divisor.setBackgroundColor(0xFFE0E0E0);

        layoutAvaliacoes.addView(
                divisor,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1
                )
        );
    }

    /*
     * Atualiza a média depois que todos os documentos
     * da subcoleção forem processados.
     */
    private void finalizarCarregamentoAvaliacoes(
            int processadas,
            int total,
            double somaNotas,
            int quantidadeNotas
    ) {

        if (processadas != total) {
            return;
        }

        if (quantidadeNotas == 0) {

            txtMediaAvaliacoes.setText(
                    "Média: sem avaliações"
            );

            return;
        }

        double media =
                somaNotas / quantidadeNotas;

        media =
                Math.round(media * 100.0) / 100.0;

        txtMediaAvaliacoes.setText(
                "Média das avaliações: "
                        + media
                        + "/5"
        );
    }

    /*
     * Mensagem simples na seção de avaliações.
     */
    private void adicionarTextoAvaliacao(String texto) {

        TextView item = new TextView(this);

        item.setText(texto);
        item.setTextColor(0xFF777777);
        item.setTextSize(14);
        item.setPadding(8, 12, 8, 12);

        layoutAvaliacoes.addView(item);
    }

    /*
     * Carrega os usuários para o Spinner de responsável.
     */
    private void carregarUsuarios() {

        db.collection("usuarios")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    ArrayList<String> nomesUsuarios =
                            new ArrayList<>();

                    mapaUsuarios.clear();

                    nomesUsuarios.add(
                            "Selecione um usuário"
                    );

                    String responsavelAtual =
                            getIntent().getStringExtra(
                                    "responsavelNome"
                            );

                    for (DocumentSnapshot doc :
                            queryDocumentSnapshots) {

                        String nome =
                                doc.getString("nome");

                        if (nome != null) {

                            nomesUsuarios.add(nome);

                            mapaUsuarios.put(
                                    nome,
                                    doc.getId()
                            );
                        }
                    }

                    ArrayAdapter<String> adapter =
                            new ArrayAdapter<>(
                                    this,
                                    android.R.layout.simple_spinner_item,
                                    nomesUsuarios
                            );

                    adapter.setDropDownViewResource(
                            android.R.layout.simple_spinner_dropdown_item
                    );

                    spUsuarios.setAdapter(adapter);

                    if (responsavelAtual != null) {

                        int posicao =
                                nomesUsuarios.indexOf(
                                        responsavelAtual
                                );

                        if (posicao >= 0) {
                            spUsuarios.setSelection(posicao);
                        }
                    }

                    spUsuarios.setEnabled(false);
                    spUsuarios.setAlpha(0.8f);
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "Erro ao carregar usuários: "
                                    + e.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }
}