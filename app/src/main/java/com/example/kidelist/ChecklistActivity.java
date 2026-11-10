
package com.example.kidelist;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ChecklistActivity extends AppCompatActivity {

    private final List<TarefaChecklist> tarefas =
            new ArrayList<>();

    private final List<Usuario> usuarios =
            new ArrayList<>();

    private final List<String> nomesFuncionarios =
            new ArrayList<>();

    private final List<String> idsFuncionarios =
            new ArrayList<>();

    private TarefaChecklistAdapter adapter;

    private TextView txtTitulo;
    private TextView txtGerente;
    private TextView txtNotaMensal;

    private LinearLayout layoutFiltroFuncionario;

    private Spinner spinnerFuncionarios;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private String funcionarioSelecionadoId = "TODOS";
    private String uidLogado;

    private boolean usuarioGerenteLogado = false;
    private boolean primeiraRetomada = true;

    // Impede que consultas antigas sobrescrevam filtros recentes.
    private int tokenCarregamento = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_checklist);

        FooterNavigation.setup(
                this,
                FooterNavigation.TELA_TAREFAS
        );

        RecyclerView rv =
                findViewById(R.id.rvTarefas);

        MaterialCardView cardNovaTarefa =
                findViewById(R.id.cardNovaTarefa);

        txtTitulo =
                findViewById(R.id.txtTitulo);

        txtGerente =
                findViewById(R.id.txtGerente);

        txtNotaMensal =
                findViewById(R.id.txtNotaMensal);

        layoutFiltroFuncionario =
                findViewById(R.id.layoutFiltroFuncionario);

        spinnerFuncionarios =
                findViewById(R.id.spinnerFuncionarios);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            finish();
            return;
        }

        uidLogado = user.getUid();

        txtNotaMensal.setVisibility(View.GONE);
        layoutFiltroFuncionario.setVisibility(View.GONE);
        cardNovaTarefa.setVisibility(View.GONE);

        /*
         * Adapter das tarefas.
         */
        adapter = new TarefaChecklistAdapter(
                this,
                tarefas,
                () -> {

                    if (usuarioGerenteLogado) {
                        carregarNotaGeral();
                    }
                }
        );

        rv.setLayoutManager(
                new LinearLayoutManager(this)
        );

        rv.setAdapter(adapter);

        /*
         * Verifica o tipo do usuário logado.
         */
        db.collection("usuarios")
                .document(uidLogado)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) {
                        Toast.makeText(
                                this,
                                "Usuário não encontrado.",
                                Toast.LENGTH_SHORT
                        ).show();

                        finish();
                        return;
                    }

                    String tipo = doc.getString("tipo");

                    usuarioGerenteLogado =
                            "Gerente".equals(tipo);

                    adapter.setUsuarioGerente(
                            usuarioGerenteLogado
                    );

                    if (usuarioGerenteLogado) {

                        /*
                         * GERENTE:
                         * pode criar tarefas, filtrar funcionários
                         * e avaliar os funcionários.
                         */
                        cardNovaTarefa.setVisibility(View.VISIBLE);

                        layoutFiltroFuncionario.setVisibility(
                                View.VISIBLE
                        );

                        txtNotaMensal.setVisibility(
                                View.VISIBLE
                        );

                        carregarFuncionarios();

                        carregarTodasTarefas();

                        carregarNotaGeral();

                    } else {

                        /*
                         * FUNCIONÁRIO:
                         * visualiza somente as próprias tarefas.
                         */
                        cardNovaTarefa.setVisibility(View.GONE);

                        layoutFiltroFuncionario.setVisibility(
                                View.GONE
                        );

                        txtNotaMensal.setVisibility(
                                View.GONE
                        );

                        carregarTarefasUsuario(uidLogado);
                    }
                })
                .addOnFailureListener(e -> {

                    cardNovaTarefa.setVisibility(View.GONE);

                    layoutFiltroFuncionario.setVisibility(
                            View.GONE
                    );

                    txtNotaMensal.setVisibility(View.GONE);

                    Toast.makeText(
                            this,
                            "Erro ao verificar usuário: "
                                    + e.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show();
                });

        carregarHeader(uidLogado);

        /*
         * Botão Nova Tarefa.
         * Somente o gerente consegue visualizar esse botão.
         */
        cardNovaTarefa.setOnClickListener(v -> {

            if (!usuarioGerenteLogado) {
                return;
            }

            Intent intent = new Intent(
                    ChecklistActivity.this,
                    CriarTarefaActivity.class
            );

            startActivity(intent);
        });

        /*
         * Botão voltar.
         */
        findViewById(R.id.btnBack).setOnClickListener(
                v -> finish()
        );
    }

    /*
     * Atualiza a lista quando retorna de outra tela.
     * A primeira abertura não gera uma segunda consulta.
     */
    @Override
    protected void onResume() {
        super.onResume();

        if (primeiraRetomada) {
            primeiraRetomada = false;
            return;
        }

        if (uidLogado == null || adapter == null) {
            return;
        }

        if (usuarioGerenteLogado) {

            if ("TODOS".equals(funcionarioSelecionadoId)) {

                carregarTodasTarefas();

            } else {

                carregarTarefasFuncionario(
                        funcionarioSelecionadoId
                );
            }

            carregarNotaGeral();

        } else {

            carregarTarefasUsuario(uidLogado);
        }
    }

    /*
     * Carrega todas as tarefas.
     */
    private void carregarTodasTarefas() {

        carregarTarefasComNotas(
                db.collection("tarefas")
        );
    }

    /*
     * Carrega tarefas de um funcionário específico.
     */
    private void carregarTarefasFuncionario(
            String funcionarioId
    ) {

        carregarTarefasComNotas(
                db.collection("tarefas")
                        .whereEqualTo("userId", funcionarioId)
        );
    }

    /*
     * Carrega somente as tarefas do funcionário logado.
     */
    private void carregarTarefasUsuario(String uid) {

        carregarTarefasComNotas(
                db.collection("tarefas")
                        .whereEqualTo("userId", uid)
        );
    }

    /*
     * Carrega tarefas e seus dados de avaliação/conclusão.
     *
     * O documento de avaliação continua no caminho:
     *
     * tarefas/{tarefaId}/notas/{funcionarioId}
     *
     * A nota e a conclusão são recuperadas separadamente
     * dos dados principais da tarefa.
     */
    private void carregarTarefasComNotas(Query consulta) {

        final int tokenAtual = ++tokenCarregamento;

        consulta.get()
                .addOnSuccessListener(resultado -> {

                    if (tokenAtual != tokenCarregamento) {
                        return;
                    }

                    tarefas.clear();

                    for (DocumentSnapshot doc :
                            resultado.getDocuments()) {

                        TarefaChecklist tarefa =
                                criarTarefaFromDocument(doc);

                        tarefas.add(tarefa);
                    }

                    adapter.notifyDataSetChanged();

                    Log.d(
                            "TAREFAS",
                            "Tarefas encontradas: " + tarefas.size()
                    );

                    if (tarefas.isEmpty()) {
                        return;
                    }

                    for (TarefaChecklist tarefa :
                            new ArrayList<>(tarefas)) {

                        String funcionarioId =
                                tarefa.getUserId();

                        if (funcionarioId == null
                                || funcionarioId.isEmpty()) {
                            continue;
                        }

                        db.collection("tarefas")
                                .document(tarefa.getId())
                                .collection("notas")
                                .document(funcionarioId)
                                .get()
                                .addOnSuccessListener(notaDoc -> {

                                    if (tokenAtual != tokenCarregamento) {
                                        return;
                                    }

                                    if (notaDoc.exists()) {

                                        Long nota =
                                                notaDoc.getLong("nota");

                                        Boolean concluida =
                                                notaDoc.getBoolean(
                                                        "concluida"
                                                );

                                        tarefa.setNota(
                                                nota != null
                                                        ? nota.intValue()
                                                        : 0
                                        );

                                        tarefa.setFeito(
                                                Boolean.TRUE.equals(
                                                        concluida
                                                )
                                        );

                                        adapter.notifyDataSetChanged();
                                    }
                                })
                                .addOnFailureListener(e ->
                                        Log.e(
                                                "AVALIACAO",
                                                "Erro ao carregar avaliação: "
                                                        + e.getMessage()
                                        )
                                );
                    }
                })
                .addOnFailureListener(e -> {

                    if (tokenAtual != tokenCarregamento) {
                        return;
                    }

                    Toast.makeText(
                            this,
                            "Erro ao carregar tarefas: "
                                    + e.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    /*
     * Converte um documento do Firestore em TarefaChecklist.
     */
    private TarefaChecklist criarTarefaFromDocument(
            DocumentSnapshot doc
    ) {

        TarefaChecklist tarefa =
                new TarefaChecklist();

        tarefa.setId(doc.getId());

        tarefa.setNome(
                doc.getString("titulo") != null
                        ? doc.getString("titulo")
                        : "Sem título"
        );

        tarefa.setDescricao(
                doc.getString("descricao") != null
                        ? doc.getString("descricao")
                        : ""
        );

        tarefa.setFotoLocal(
                doc.getString("fotoUrl") != null
                        ? doc.getString("fotoUrl")
                        : ""
        );

        tarefa.setData(
                doc.getString("data") != null
                        ? doc.getString("data")
                        : ""
        );

        tarefa.setHora(
                doc.getString("hora") != null
                        ? doc.getString("hora")
                        : ""
        );

        tarefa.setUserId(
                doc.getString("userId")
        );

        tarefa.setResponsavelNome(
                doc.getString("responsavelNome") != null
                        ? doc.getString("responsavelNome")
                        : ""
        );

        tarefa.setNota(0);
        tarefa.setFeito(false);

        return tarefa;
    }

    /*
     * Carrega todos os funcionários no Spinner.
     */
    private void carregarFuncionarios() {

        usuarios.clear();
        nomesFuncionarios.clear();
        idsFuncionarios.clear();

        nomesFuncionarios.add(
                "Todos os funcionários"
        );

        idsFuncionarios.add("TODOS");

        db.collection("usuarios")
                .whereEqualTo("tipo", "Funcionario")
                .get()
                .addOnSuccessListener(funcionarios -> {

                    Log.d(
                            "FUNCIONARIOS",
                            "Total de funcionários encontrados: "
                                    + funcionarios.size()
                    );

                    for (DocumentSnapshot funcionario :
                            funcionarios.getDocuments()) {

                        String funcionarioId =
                                funcionario.getId();

                        String nomeFuncionario =
                                funcionario.getString("nome");

                        if (nomeFuncionario == null) {
                            nomeFuncionario = "Sem nome";
                        }

                        nomesFuncionarios.add(
                                nomeFuncionario
                        );

                        idsFuncionarios.add(
                                funcionarioId
                        );

                        Usuario usuario =
                                new Usuario();

                        usuario.setId(funcionarioId);
                        usuario.setNome(nomeFuncionario);

                        usuarios.add(usuario);
                    }

                    ArrayAdapter<String> spinnerAdapter =
                            new ArrayAdapter<>(
                                    ChecklistActivity.this,
                                    android.R.layout.simple_spinner_item,
                                    nomesFuncionarios
                            );

                    spinnerAdapter.setDropDownViewResource(
                            android.R.layout.simple_spinner_dropdown_item
                    );

                    spinnerFuncionarios.setAdapter(
                            spinnerAdapter
                    );

                    spinnerFuncionarios.setOnItemSelectedListener(
                            new AdapterView.OnItemSelectedListener() {

                                @Override
                                public void onItemSelected(
                                        AdapterView<?> parent,
                                        View view,
                                        int position,
                                        long id
                                ) {

                                    if (position >=
                                            idsFuncionarios.size()) {
                                        return;
                                    }

                                    funcionarioSelecionadoId =
                                            idsFuncionarios.get(position);

                                    Log.d(
                                            "FILTRO",
                                            "Funcionário selecionado: "
                                                    + nomesFuncionarios.get(
                                                    position
                                            )
                                    );

                                    if ("TODOS".equals(
                                            funcionarioSelecionadoId
                                    )) {

                                        carregarTodasTarefas();

                                    } else {

                                        carregarTarefasFuncionario(
                                                funcionarioSelecionadoId
                                        );
                                    }
                                }

                                @Override
                                public void onNothingSelected(
                                        AdapterView<?> parent
                                ) {
                                }
                            }
                    );

                    spinnerFuncionarios.setSelection(0);

                    // Garante o carregamento inicial de todas as tarefas.
                    carregarTodasTarefas();
                })
                .addOnFailureListener(e -> {

                    Log.e(
                            "FUNCIONARIOS",
                            "Erro ao buscar funcionários: "
                                    + e.getMessage()
                    );

                    Toast.makeText(
                            ChecklistActivity.this,
                            "Erro ao carregar funcionários",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    /*
     * Carrega o cabeçalho.
     */
    private void carregarHeader(String uid) {

        db.collection("usuarios")
                .document(uid)
                .get()
                .addOnSuccessListener(userDoc -> {

                    if (!userDoc.exists()) {
                        return;
                    }

                    String nome =
                            userDoc.getString("nome");

                    if (nome != null) {
                        txtGerente.setText(nome);
                    }

                    String lojaId =
                            userDoc.getString("lojaId");

                    if (lojaId != null) {
                        carregarNomeLoja(lojaId);
                    }
                });
    }

    private void carregarNomeLoja(String lojaId) {

        db.collection("lojas")
                .document(lojaId)
                .get()
                .addOnSuccessListener(lojaDoc -> {

                    String nomeLoja =
                            lojaDoc.getString("nome");

                    if (nomeLoja == null) {
                        nomeLoja = "Loja";
                    }

                    txtTitulo.setText(
                            "CheckList - " + nomeLoja
                    );
                });
    }

    /*
     * Calcula a nota geral com base nas notas atribuídas
     * pelo gerente às tarefas.
     */
    private void carregarNotaGeral() {

        db.collection("tarefas")
                .get()
                .addOnSuccessListener(query -> {

                    if (query.isEmpty()) {

                        txtNotaMensal.setText(
                                "Nota Mensal: 0%"
                        );

                        return;
                    }

                    final double[] somaNotas = {0};
                    final int[] tarefasComNota = {0};
                    final int[] tarefasProcessadas = {0};

                    final int totalTarefas = query.size();

                    for (DocumentSnapshot tarefaDoc :
                            query.getDocuments()) {

                        String tarefaId =
                                tarefaDoc.getId();

                        String userId =
                                tarefaDoc.getString("userId");

                        if (userId == null || userId.isEmpty()) {

                            tarefasProcessadas[0]++;

                            verificarCalculoNotaGeral(
                                    tarefasProcessadas[0],
                                    totalTarefas,
                                    somaNotas[0],
                                    tarefasComNota[0]
                            );

                            continue;
                        }

                        db.collection("tarefas")
                                .document(tarefaId)
                                .collection("notas")
                                .document(userId)
                                .get()
                                .addOnSuccessListener(notaDoc -> {

                                    if (notaDoc.exists()) {

                                        Long nota =
                                                notaDoc.getLong("nota");

                                        if (nota != null) {

                                            somaNotas[0] += nota;

                                            tarefasComNota[0]++;
                                        }
                                    }

                                    tarefasProcessadas[0]++;

                                    verificarCalculoNotaGeral(
                                            tarefasProcessadas[0],
                                            totalTarefas,
                                            somaNotas[0],
                                            tarefasComNota[0]
                                    );
                                })
                                .addOnFailureListener(e -> {

                                    tarefasProcessadas[0]++;

                                    verificarCalculoNotaGeral(
                                            tarefasProcessadas[0],
                                            totalTarefas,
                                            somaNotas[0],
                                            tarefasComNota[0]
                                    );
                                });
                    }
                })
                .addOnFailureListener(e ->

                        Toast.makeText(
                                this,
                                "Erro ao calcular nota: "
                                        + e.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    private void verificarCalculoNotaGeral(
            int processadas,
            int total,
            double soma,
            int quantidade
    ) {

        if (processadas == total) {
            exibirNotaLoja(soma, quantidade);
        }
    }

    private void exibirNotaLoja(
            double somaNotas,
            int tarefasComNota
    ) {

        double percentual = 0;

        if (tarefasComNota > 0) {

            double media =
                    somaNotas / tarefasComNota;

            percentual =
                    (media / 5.0) * 100.0;
        }

        percentual =
                Math.round(percentual * 100.0) / 100.0;

        txtNotaMensal.setText(
                "Nota Mensal: " + percentual + "%"
        );
    }
}
