package com.example.kidelist;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RankingActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    private Button btnSemanal, btnMensal;
    private ImageView btnVoltar;

    private RecyclerView rvRankingLojas;
    private LojaRankingAdapter adapter;

    private TextView txtLojaPrimeiro, txtPontosPrimeiro;
    private TextView txtLojaSegundo, txtPontosSegundo;
    private TextView txtLojaTerceiro, txtPontosTerceiro;

    private boolean filtroSemanal = true;

    private final Map<String, String> nomesLojas = new HashMap<>();
    private final Map<String, String> usuarioLojaId = new HashMap<>();
    private final Map<String, String> gerenteLoja = new HashMap<>();

    private final Map<String, LojaRankingTemp> rankingTemp = new HashMap<>();

    private final List<LojaRanking> listaRanking = new ArrayList<>();

    private int totalAvaliacoesProcessadas = 0;
    private int totalAvaliacoesParaProcessar = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        FooterNavigation.setup(this, FooterNavigation.TELA_RANKING);

        db = FirebaseFirestore.getInstance();

        btnVoltar = findViewById(R.id.btnVoltar);

        btnSemanal = findViewById(R.id.btnSemanal);
        btnMensal = findViewById(R.id.btnMensal);

        rvRankingLojas = findViewById(R.id.rvRankingLojas);

        txtLojaPrimeiro = findViewById(R.id.txtLojaPrimeiro);
        txtPontosPrimeiro = findViewById(R.id.txtPontosPrimeiro);

        txtLojaSegundo = findViewById(R.id.txtLojaSegundo);
        txtPontosSegundo = findViewById(R.id.txtPontosSegundo);

        txtLojaTerceiro = findViewById(R.id.txtLojaTerceiro);
        txtPontosTerceiro = findViewById(R.id.txtPontosTerceiro);

        btnVoltar.setOnClickListener(v -> finish());

        rvRankingLojas.setLayoutManager(new LinearLayoutManager(this));
        rvRankingLojas.setNestedScrollingEnabled(false);

        adapter = new LojaRankingAdapter(listaRanking);
        rvRankingLojas.setAdapter(adapter);

        btnSemanal.setOnClickListener(v -> {
            filtroSemanal = true;
            atualizarBotoesFiltro();
            carregarRanking();
        });

        btnMensal.setOnClickListener(v -> {
            filtroSemanal = false;
            atualizarBotoesFiltro();
            carregarRanking();
        });

        atualizarBotoesFiltro();
        carregarRanking();
    }

    private void atualizarBotoesFiltro() {

        if (filtroSemanal) {
            btnSemanal.setBackgroundResource(R.drawable.bg_filtro_ativo);
            btnMensal.setBackgroundResource(R.drawable.bg_filtro_inativo);
        } else {
            btnMensal.setBackgroundResource(R.drawable.bg_filtro_ativo);
            btnSemanal.setBackgroundResource(R.drawable.bg_filtro_inativo);
        }
    }

    private void carregarRanking() {

        nomesLojas.clear();
        usuarioLojaId.clear();
        gerenteLoja.clear();
        rankingTemp.clear();

        listaRanking.clear();
        adapter.notifyDataSetChanged();

        limparTopTres();

        /*
         * PRIMEIRO: CARREGA AS LOJAS.
         *
         * O ID do documento é o lojaId.
         * O único campo do documento é nome.
         */
        db.collection("lojas")
                .get()
                .addOnSuccessListener(lojas -> {

                    for (DocumentSnapshot doc : lojas.getDocuments()) {

                        String lojaId = doc.getId();
                        String nomeLoja = doc.getString("nome");

                        if (nomeLoja != null && !nomeLoja.trim().isEmpty()) {
                            nomesLojas.put(lojaId, nomeLoja);
                        }

                        Log.d(
                                "RANKING",
                                "Loja encontrada: ID=" + lojaId
                                        + " | Nome=" + nomeLoja
                        );
                    }

                    carregarUsuarios();

                })
                .addOnFailureListener(e -> {

                    Log.e("RANKING", "Erro ao carregar lojas", e);

                    Toast.makeText(
                            this,
                            "Erro ao carregar lojas: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void carregarUsuarios() {

        db.collection("usuarios")
                .get()
                .addOnSuccessListener(usuarios -> {

                    usuarioLojaId.clear();
                    gerenteLoja.clear();

                    for (DocumentSnapshot doc : usuarios.getDocuments()) {

                        String usuarioId = doc.getId();
                        String tipo = doc.getString("tipo");
                        String nome = doc.getString("nome");

                        Object lojaIdObj = doc.get("lojaId");

                        if (lojaIdObj == null) {
                            Log.d(
                                    "RANKING",
                                    "Usuário sem loja: " + usuarioId
                            );
                            continue;
                        }

                        String lojaId = String.valueOf(lojaIdObj).trim();

                        if (lojaId.isEmpty()) {
                            continue;
                        }

                        if (!nomesLojas.containsKey(lojaId)) {
                            Log.w(
                                    "RANKING",
                                    "Loja não encontrada: "
                                            + lojaId
                                            + " | Usuário: "
                                            + usuarioId
                            );
                            continue;
                        }

                        /*
                         * Relaciona o usuário à loja.
                         * Não depende do valor do campo tipo.
                         */
                        usuarioLojaId.put(usuarioId, lojaId);

                        if ("Gerente".equals(tipo)) {
                            gerenteLoja.put(lojaId, nome);
                        }

                        Log.d(
                                "RANKING",
                                "Usuário associado: "
                                        + nome
                                        + " | Usuário ID: "
                                        + usuarioId
                                        + " | Loja ID: "
                                        + lojaId
                                        + " | Loja: "
                                        + nomesLojas.get(lojaId)
                        );
                    }

                    Log.d(
                            "RANKING",
                            "Total de usuários associados a lojas: "
                                    + usuarioLojaId.size()
                    );

                    carregarTarefas();

                })
                .addOnFailureListener(e -> {

                    Log.e("RANKING", "Erro ao carregar usuários", e);

                    Toast.makeText(
                            this,
                            "Erro ao carregar funcionários: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void carregarTarefas() {

        Log.d("RANKING", "========== INÍCIO CARREGAMENTO TAREFAS ==========");

        Log.d("RANKING", "Usuários associados no mapa: " + usuarioLojaId.size());

        db.collection("tarefas")
                .get()
                .addOnSuccessListener(tarefas -> {

                    Date inicio = obterInicioPeriodo();
                    Date fim = obterFimPeriodo();

                    Log.d("RANKING", "Total de tarefas no Firestore: " + tarefas.size());
                    Log.d("RANKING", "Início do período: " + inicio);
                    Log.d("RANKING", "Fim do período: " + fim);

                    List<DocumentSnapshot> tarefasValidas = new ArrayList<>();

                    int semUsuario = 0;
                    int usuarioNaoEncontrado = 0;
                    int dataNula = 0;
                    int foraPeriodo = 0;

                    for (DocumentSnapshot tarefa : tarefas.getDocuments()) {

                        String tarefaId = tarefa.getId();
                        String funcionarioId = tarefa.getString("userId");

                        Log.d("RANKING", "--------------------------------");
                        Log.d("RANKING", "Tarefa: " + tarefaId);
                        Log.d("RANKING", "userId da tarefa: " + funcionarioId);

                        if (funcionarioId == null || funcionarioId.trim().isEmpty()) {
                            semUsuario++;
                            Log.d("RANKING", "DESCARTADA: tarefa sem userId");
                            continue;
                        }

                        if (!usuarioLojaId.containsKey(funcionarioId)) {
                            usuarioNaoEncontrado++;

                            Log.d(
                                    "RANKING",
                                    "DESCARTADA: usuário não encontrado no mapa"
                            );

                            Log.d(
                                    "RANKING",
                                    "IDs disponíveis no mapa: " + usuarioLojaId.keySet()
                            );

                            continue;
                        }

                        String lojaId = usuarioLojaId.get(funcionarioId);

                        Log.d("RANKING", "Funcionário encontrado!");
                        Log.d("RANKING", "Loja associada: " + lojaId);

                        Date dataTarefa = obterDataTarefa(tarefa);

                        if (dataTarefa == null) {
                            dataNula++;

                            Log.d("RANKING", "DESCARTADA: data da tarefa nula");

                            Log.d(
                                    "RANKING",
                                    "Campos da tarefa: " + tarefa.getData()
                            );

                            continue;
                        }

                        Log.d("RANKING", "Data da tarefa: " + dataTarefa);

                        if (dataTarefa.before(inicio) || dataTarefa.after(fim)) {
                            foraPeriodo++;

                            Log.d(
                                    "RANKING",
                                    "DESCARTADA: fora do período selecionado"
                            );

                            continue;
                        }

                        Log.d("RANKING", "TAREFA APROVADA!");

                        tarefasValidas.add(tarefa);
                    }

                    Log.d("RANKING", "========== RESULTADO DOS FILTROS ==========");
                    Log.d("RANKING", "Total de tarefas: " + tarefas.size());
                    Log.d("RANKING", "Sem usuário: " + semUsuario);
                    Log.d("RANKING", "Usuário não encontrado: " + usuarioNaoEncontrado);
                    Log.d("RANKING", "Data nula: " + dataNula);
                    Log.d("RANKING", "Fora do período: " + foraPeriodo);
                    Log.d("RANKING", "Tarefas aprovadas: " + tarefasValidas.size());

                    totalAvaliacoesParaProcessar = tarefasValidas.size();
                    totalAvaliacoesProcessadas = 0;

                    rankingTemp.clear();

                    if (totalAvaliacoesParaProcessar == 0) {
                        Log.e("RANKING", "Nenhuma tarefa passou pelos filtros!");
                        finalizarRanking();
                        return;
                    }

                    for (DocumentSnapshot tarefa : tarefasValidas) {

                        String tarefaId = tarefa.getId();
                        String funcionarioId = tarefa.getString("userId");

                        String lojaId = usuarioLojaId.get(funcionarioId);

                        Log.d("RANKING", "Buscando nota da tarefa: " + tarefaId);
                        Log.d("RANKING", "Documento esperado da nota: " + funcionarioId);

                        db.collection("tarefas")
                                .document(tarefaId)
                                .collection("notas")
                                .document(funcionarioId)
                                .get()
                                .addOnSuccessListener(notaDoc -> {

                                    Log.d(
                                            "RANKING",
                                            "Resultado da nota " + tarefaId
                                                    + ": existe=" + notaDoc.exists()
                                    );

                                    if (notaDoc.exists()) {

                                        Object notaObj = notaDoc.get("nota");

                                        Log.d(
                                                "RANKING",
                                                "Valor da nota: " + notaObj
                                        );

                                        if (notaObj instanceof Number) {

                                            int nota = ((Number) notaObj).intValue();

                                            if (nota >= 1 && nota <= 5) {

                                                LojaRankingTemp loja =
                                                        rankingTemp.get(lojaId);

                                                if (loja == null) {
                                                    loja = new LojaRankingTemp();
                                                    rankingTemp.put(lojaId, loja);
                                                }

                                                loja.somaNotas += nota;
                                                loja.quantidadeNotas++;

                                                Log.d(
                                                        "RANKING",
                                                        "NOTA CONTABILIZADA: loja=" + lojaId
                                                                + " nota=" + nota
                                                                + " quantidade=" + loja.quantidadeNotas
                                                );

                                            } else {
                                                Log.e(
                                                        "RANKING",
                                                        "Nota fora do intervalo: " + nota
                                                );
                                            }

                                        } else {
                                            Log.e(
                                                    "RANKING",
                                                    "Campo nota não é numérico: " + notaObj
                                            );
                                        }

                                    } else {
                                        Log.e(
                                                "RANKING",
                                                "Documento de avaliação não encontrado: "
                                                        + tarefaId + "/notas/" + funcionarioId
                                        );
                                    }

                                    registrarProcessamento();

                                })
                                .addOnFailureListener(e -> {

                                    Log.e(
                                            "RANKING",
                                            "Erro ao buscar nota da tarefa " + tarefaId,
                                            e
                                    );

                                    registrarProcessamento();
                                });
                    }

                })
                .addOnFailureListener(e -> {

                    Log.e("RANKING", "Erro ao carregar tarefas", e);

                    Toast.makeText(
                            this,
                            "Erro ao carregar tarefas: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void registrarProcessamento() {

        totalAvaliacoesProcessadas++;

        if (totalAvaliacoesProcessadas >= totalAvaliacoesParaProcessar) {
            finalizarRanking();
        }
    }

    private Date obterDataTarefa(DocumentSnapshot tarefa) {

        Object dataObj = tarefa.get("data");

        if (dataObj == null) {
            return null;
        }

        if (dataObj instanceof Timestamp) {
            return ((Timestamp) dataObj).toDate();
        }

        if (dataObj instanceof Date) {
            return (Date) dataObj;
        }

        if (dataObj instanceof String) {

            String data = ((String) dataObj).trim();

            String[] formatos = {
                    "dd/MM/yyyy",
                    "yyyy-MM-dd",
                    "dd-MM-yyyy"
            };

            for (String formato : formatos) {

                try {
                    SimpleDateFormat sdf =
                            new SimpleDateFormat(formato, Locale.getDefault());

                    sdf.setLenient(false);

                    return sdf.parse(data);

                } catch (ParseException ignored) {
                }
            }
        }

        return null;
    }

    private Date obterInicioPeriodo() {

        Calendar calendario = Calendar.getInstance();

        if (filtroSemanal) {

            calendario.set(
                    Calendar.DAY_OF_WEEK,
                    Calendar.MONDAY
            );

        } else {

            calendario.set(Calendar.DAY_OF_MONTH, 1);
        }

        calendario.set(Calendar.HOUR_OF_DAY, 0);
        calendario.set(Calendar.MINUTE, 0);
        calendario.set(Calendar.SECOND, 0);
        calendario.set(Calendar.MILLISECOND, 0);

        return calendario.getTime();
    }

    private Date obterFimPeriodo() {

        Calendar calendario = Calendar.getInstance();

        if (filtroSemanal) {

            calendario.set(
                    Calendar.DAY_OF_WEEK,
                    Calendar.MONDAY
            );

            calendario.add(Calendar.DAY_OF_MONTH, 6);

        } else {

            calendario.set(
                    Calendar.DAY_OF_MONTH,
                    calendario.getActualMaximum(Calendar.DAY_OF_MONTH)
            );
        }

        calendario.set(Calendar.HOUR_OF_DAY, 23);
        calendario.set(Calendar.MINUTE, 59);
        calendario.set(Calendar.SECOND, 59);
        calendario.set(Calendar.MILLISECOND, 999);

        return calendario.getTime();
    }

    private void finalizarRanking() {
        Log.d("RANKING", "========== INÍCIO FINALIZAÇÃO DO RANKING ==========");

        listaRanking.clear();

        Log.d("RANKING", "Total de lojas no rankingTemp: " + rankingTemp.size());
        Log.d("RANKING", "Total de lojas cadastradas: " + nomesLojas.size());

        for (Map.Entry<String, LojaRankingTemp> entry : rankingTemp.entrySet()) {

            String lojaId = entry.getKey();
            LojaRankingTemp dados = entry.getValue();

            Log.d("RANKING", "--------------------------------");
            Log.d("RANKING", "ID da loja no rankingTemp: " + lojaId);
            Log.d("RANKING", "Soma das notas: " + dados.somaNotas);
            Log.d("RANKING", "Quantidade de notas: " + dados.quantidadeNotas);

            if (dados.quantidadeNotas == 0) {
                Log.d("RANKING", "LOJA DESCARTADA: nenhuma avaliação.");
                continue;
            }

            boolean lojaExiste = nomesLojas.containsKey(lojaId);
            String nomeLoja = nomesLojas.get(lojaId);

            Log.d("RANKING", "ID existe em nomesLojas? " + lojaExiste);
            Log.d("RANKING", "Nome encontrado: " + nomeLoja);

            if (nomeLoja == null || nomeLoja.trim().isEmpty()) {
                Log.e("RANKING", "LOJA DESCARTADA: nome não encontrado para ID " + lojaId);
                continue;
            }

            double media = dados.somaNotas / dados.quantidadeNotas;

            String gerente = gerenteLoja.getOrDefault(lojaId, "Não informado");

            Log.d("RANKING", "Média calculada: " + media);
            Log.d("RANKING", "Gerente: " + gerente);

            LojaRanking lojaRanking = new LojaRanking(
                    0,
                    nomeLoja,
                    gerente,
                    media
            );

            listaRanking.add(lojaRanking);

            Log.d("RANKING", "LOJA ADICIONADA AO RANKING: " + nomeLoja);
        }

        // Ordena da maior média para a menor
        Collections.sort(listaRanking, (l1, l2) ->
                Double.compare(l2.getPontos(), l1.getPontos())
        );

        // Atualiza a posição de cada loja
        for (int i = 0; i < listaRanking.size(); i++) {
            listaRanking.get(i).setPosicao(i + 1);
        }

        Log.d("RANKING", "========== RESULTADO FINAL ==========");
        Log.d("RANKING", "Lojas classificadas: " + listaRanking.size());

        for (LojaRanking loja : listaRanking) {
            Log.d("RANKING", "Posição: " + loja.getPosicao()
                    + " | Loja: " + loja.getNomeLoja()
                    + " | Gerente: " + loja.getGerente()
                    + " | Média: " + loja.getPontos());
        }

        adapter.atualizarLista(listaRanking);

        atualizarTopTres();

        Log.d("RANKING_UI", "========== TESTE DA INTERFACE ==========");

        Log.d("RANKING_UI", "ListaRanking: " + listaRanking.size());

        Log.d("RANKING_UI", "Itens no adapter: " + adapter.getItemCount());

        Log.d("RANKING_UI", "RecyclerView existe: " + (rvRankingLojas != null));

        if (rvRankingLojas != null) {
            Log.d("RANKING_UI", "RecyclerView visibilidade: " + rvRankingLojas.getVisibility());
            Log.d("RANKING_UI", "RecyclerView altura: " + rvRankingLojas.getHeight());
            Log.d("RANKING_UI", "LayoutManager: " + rvRankingLojas.getLayoutManager());
            Log.d("RANKING_UI", "Adapter conectado: " + (rvRankingLojas.getAdapter() != null));
        }
    }

    private void atualizarTopTres() {

        if (listaRanking.size() >= 1) {
            atualizarCard(
                    txtLojaPrimeiro,
                    txtPontosPrimeiro,
                    listaRanking.get(0)
            );
        }

        if (listaRanking.size() >= 2) {
            atualizarCard(
                    txtLojaSegundo,
                    txtPontosSegundo,
                    listaRanking.get(1)
            );
        }

        if (listaRanking.size() >= 3) {
            atualizarCard(
                    txtLojaTerceiro,
                    txtPontosTerceiro,
                    listaRanking.get(2)
            );
        }
    }

    private void atualizarCard(
            TextView txtLoja,
            TextView txtPontos,
            LojaRanking loja
    ) {

        DecimalFormatSymbols simbolos =
                new DecimalFormatSymbols(new Locale("pt", "BR"));

        DecimalFormat formato =
                new DecimalFormat("0.00", simbolos);

        txtLoja.setText(loja.getNomeLoja());

        txtPontos.setText(
                formato.format(loja.getPontos()) + " / 5"
        );
    }

    private void limparTopTres() {

        txtLojaPrimeiro.setText("—");
        txtPontosPrimeiro.setText("0,00 / 5");

        txtLojaSegundo.setText("—");
        txtPontosSegundo.setText("0,00 / 5");

        txtLojaTerceiro.setText("—");
        txtPontosTerceiro.setText("0,00 / 5");
    }

    private static class LojaRankingTemp {

        double somaNotas = 0;
        int quantidadeNotas = 0;
    }
}