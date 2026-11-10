
package com.example.kidelist;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TarefaChecklistAdapter
        extends RecyclerView.Adapter<TarefaChecklistAdapter.VH> {

    private final Context context;
    private final List<TarefaChecklist> itens;
    private final FirebaseFirestore db;

    private boolean usuarioGerente = false;

    private final OnNotaAtualizadaListener listener;

    public interface OnNotaAtualizadaListener {
        void onNotaAtualizada();
    }

    public TarefaChecklistAdapter(
            Context context,
            List<TarefaChecklist> itens,
            OnNotaAtualizadaListener listener
    ) {
        this.context = context;
        this.itens = itens;
        this.listener = listener;
        this.db = FirebaseFirestore.getInstance();
    }

    public void setUsuarioGerente(boolean gerente) {
        this.usuarioGerente = gerente;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.item_tarefa_checklist,
                        parent,
                        false
                );

        return new VH(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull VH holder,
            int position
    ) {
        TarefaChecklist tarefa = itens.get(position);

        holder.txtNomeTarefa.setText(tarefa.getNome());

        atualizarStatus(holder, tarefa.isFeito());

        // Imagem da tarefa
        if (tarefa.getFotoLocal() != null
                && !tarefa.getFotoLocal().isEmpty()) {

            try {
                holder.imgThumb.setImageURI(
                        Uri.parse(tarefa.getFotoLocal())
                );
            } catch (Exception e) {
                holder.imgThumb.setImageResource(
                        android.R.drawable.ic_menu_gallery
                );
            }

        } else {
            holder.imgThumb.setImageResource(
                    android.R.drawable.ic_menu_gallery
            );
        }

        // Abre os detalhes da tarefa
        holder.itemView.setOnClickListener(v -> {

            Intent intent = new Intent(
                    context,
                    EditarTarefaActivity.class
            );

            intent.putExtra("tarefaId", tarefa.getId());
            intent.putExtra("nome", tarefa.getNome());
            intent.putExtra("descricao", tarefa.getDescricao());
            intent.putExtra("nota", tarefa.getNota());
            intent.putExtra("feito", tarefa.isFeito());
            intent.putExtra("data", tarefa.getData());
            intent.putExtra("hora", tarefa.getHora());
            intent.putExtra(
                    "responsavelNome",
                    tarefa.getResponsavelNome()
            );

            context.startActivity(intent);
        });

        // Câmera
        holder.btnCamera.setOnClickListener(v -> {

            Intent cameraIntent = new Intent(
                    MediaStore.ACTION_IMAGE_CAPTURE
            );

            if (cameraIntent.resolveActivity(
                    context.getPackageManager()
            ) != null) {

                context.startActivity(cameraIntent);

            } else {

                Toast.makeText(
                        context,
                        "Nenhum aplicativo de câmera encontrado.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        if (usuarioGerente) {
            configurarModoGerente(holder, tarefa);
        } else {
            configurarModoFuncionario(holder, tarefa);
        }
    }

    /*
     * MODO GERENTE
     *
     * O gerente pode atribuir uma nota de 1 a 5.
     * A nota é gravada para o funcionário responsável,
     * e não para o usuário gerente que está logado.
     */
    private void configurarModoGerente(
            VH holder,
            TarefaChecklist tarefa
    ) {

        holder.groupNota.setVisibility(View.VISIBLE);
        holder.txtPergunta.setVisibility(View.VISIBLE);

        holder.txtPergunta.setText(
                "Avalie o funcionário:"
        );

        if (holder.btnConcluir != null) {
            holder.btnConcluir.setVisibility(View.GONE);
        }

        aplicarNotaSelecionada(holder, tarefa.getNota());

        holder.btn1.setOnClickListener(v ->
                atualizarNota(holder.getAdapterPosition(), 1)
        );

        holder.btn2.setOnClickListener(v ->
                atualizarNota(holder.getAdapterPosition(), 2)
        );

        holder.btn3.setOnClickListener(v ->
                atualizarNota(holder.getAdapterPosition(), 3)
        );

        holder.btn4.setOnClickListener(v ->
                atualizarNota(holder.getAdapterPosition(), 4)
        );

        holder.btn5.setOnClickListener(v ->
                atualizarNota(holder.getAdapterPosition(), 5)
        );
    }

    /*
     * MODO FUNCIONÁRIO
     *
     * O funcionário não vê as notas.
     * Ele pode somente marcar a tarefa como concluída
     * ou voltar a marcá-la como pendente.
     */
    private void configurarModoFuncionario(
            VH holder,
            TarefaChecklist tarefa
    ) {

        holder.groupNota.setVisibility(View.GONE);
        holder.txtPergunta.setVisibility(View.GONE);

        if (holder.btnConcluir != null) {

            holder.btnConcluir.setVisibility(View.VISIBLE);

            if (holder.btnConcluir instanceof TextView) {

                ((TextView) holder.btnConcluir).setText(
                        tarefa.isFeito()
                                ? "Marcar como pendente"
                                : "Marcar como concluída"
                );
            }

            holder.btnConcluir.setOnClickListener(v -> {

                int position = holder.getAdapterPosition();

                if (position != RecyclerView.NO_POSITION) {
                    TarefaChecklist tarefaAtual = itens.get(position);

                    atualizarConclusao(
                            position,
                            !tarefaAtual.isFeito()
                    );
                }
            });

        } else {

            // Compatibilidade com o XML antigo:
            // o próprio status funciona como botão.
            holder.txtStatus.setClickable(true);
            holder.txtStatus.setFocusable(true);

            holder.txtStatus.setOnClickListener(v -> {

                int position = holder.getAdapterPosition();

                if (position != RecyclerView.NO_POSITION) {
                    TarefaChecklist tarefaAtual = itens.get(position);

                    atualizarConclusao(
                            position,
                            !tarefaAtual.isFeito()
                    );
                }
            });
        }
    }

    /*
     * Atualiza o texto de status.
     */
    private void atualizarStatus(
            VH holder,
            boolean feito
    ) {

        if (feito) {
            holder.txtStatus.setText("FEITO");
            holder.txtStatus.setTextColor(0xFF27AE60);

        } else {
            holder.txtStatus.setText("PENDENTE");
            holder.txtStatus.setTextColor(0xFFF2994A);
        }
    }

    /*
     * Destaca a nota que já foi selecionada.
     */
    private void aplicarNotaSelecionada(
            VH holder,
            int nota
    ) {

        MaterialButton[] botoes = {
                holder.btn1,
                holder.btn2,
                holder.btn3,
                holder.btn4,
                holder.btn5
        };

        if (nota >= 1 && nota <= 5) {
            holder.groupNota.check(botoes[nota - 1].getId());
        } else {
            holder.groupNota.clearChecked();
        }

        for (int i = 0; i < botoes.length; i++) {

            boolean selecionado = (i + 1) == nota;

            if (selecionado) {

                botoes[i].setBackgroundTintList(
                        ColorStateList.valueOf(0xFFF4B400)
                );

                botoes[i].setTextColor(0xFF0E3B66);

            } else {

                botoes[i].setBackgroundTintList(
                        ColorStateList.valueOf(0xFF0E3B66)
                );

                botoes[i].setTextColor(0xFFFFFFFF);
            }
        }
    }

    /*
     * FUNCIONÁRIO:
     * grava somente a conclusão.
     *
     * SetOptions.merge() preserva a nota já atribuída
     * pelo gerente no mesmo documento.
     */
    private void atualizarConclusao(
            int position,
            boolean concluida
    ) {

        if (position == RecyclerView.NO_POSITION) {
            return;
        }

        FirebaseUser user =
                FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            return;
        }

        TarefaChecklist tarefa = itens.get(position);

        if (tarefa.getUserId() == null
                || !user.getUid().equals(tarefa.getUserId())) {

            Toast.makeText(
                    context,
                    "Você não pode alterar esta tarefa.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        Map<String, Object> dados = new HashMap<>();

        dados.put("userId", user.getUid());
        dados.put("tarefaId", tarefa.getId());
        dados.put("concluida", concluida);

        db.collection("tarefas")
                .document(tarefa.getId())
                .collection("notas")
                .document(user.getUid())
                .set(dados, SetOptions.merge())
                .addOnSuccessListener(unused -> {

                    tarefa.setFeito(concluida);

                    int posicaoAtual = itens.indexOf(tarefa);

                    if (posicaoAtual != -1) {
                        notifyItemChanged(posicaoAtual);
                    }

                    Toast.makeText(
                            context,
                            concluida
                                    ? "Tarefa concluída!"
                                    : "Tarefa marcada como pendente.",
                            Toast.LENGTH_SHORT
                    ).show();

                    if (listener != null) {
                        listener.onNotaAtualizada();
                    }
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            context,
                            "Erro ao atualizar tarefa: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    /*
     * GERENTE:
     * grava a nota para o funcionário responsável.
     *
     * Não altera o campo concluida.
     */
    private void atualizarNota(
            int position,
            int nota
    ) {

        if (position == RecyclerView.NO_POSITION) {
            return;
        }

        FirebaseUser user =
                FirebaseAuth.getInstance().getCurrentUser();

        if (user == null || !usuarioGerente) {
            return;
        }

        TarefaChecklist tarefa = itens.get(position);

        String funcionarioId = tarefa.getUserId();

        if (funcionarioId == null || funcionarioId.isEmpty()) {

            Toast.makeText(
                    context,
                    "Esta tarefa não possui funcionário responsável.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        Map<String, Object> dados = new HashMap<>();

        dados.put("nota", nota);
        dados.put("userId", funcionarioId);
        dados.put("tarefaId", tarefa.getId());
        dados.put("concluida", true);

        db.collection("tarefas")
                .document(tarefa.getId())
                .collection("notas")
                .document(funcionarioId)
                .set(dados, SetOptions.merge())
                .addOnSuccessListener(unused -> {

                    tarefa.setNota(nota);
                    tarefa.setFeito(true);

                    int posicaoAtual = itens.indexOf(tarefa);

                    if (posicaoAtual != -1) {
                        notifyItemChanged(posicaoAtual);
                    }

                    Toast.makeText(
                            context,
                            "Avaliação registrada!",
                            Toast.LENGTH_SHORT
                    ).show();

                    if (listener != null) {
                        listener.onNotaAtualizada();
                    }
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            context,
                            "Erro ao registrar avaliação: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    static class VH extends RecyclerView.ViewHolder {

        TextView txtNomeTarefa;
        TextView txtStatus;
        TextView txtPergunta;

        MaterialButton btn1;
        MaterialButton btn2;
        MaterialButton btn3;
        MaterialButton btn4;
        MaterialButton btn5;

        MaterialButtonToggleGroup groupNota;

        View btnCamera;
        View btnConcluir;

        ImageView imgThumb;

        VH(@NonNull View itemView) {
            super(itemView);

            txtNomeTarefa =
                    itemView.findViewById(R.id.txtNomeTarefa);

            txtStatus =
                    itemView.findViewById(R.id.txtStatus);

            txtPergunta =
                    itemView.findViewById(R.id.txtPergunta);

            groupNota =
                    itemView.findViewById(R.id.groupNota);

            btn1 = itemView.findViewById(R.id.btn1);
            btn2 = itemView.findViewById(R.id.btn2);
            btn3 = itemView.findViewById(R.id.btn3);
            btn4 = itemView.findViewById(R.id.btn4);
            btn5 = itemView.findViewById(R.id.btn5);

            btnCamera =
                    itemView.findViewById(R.id.btnCamera);

            imgThumb =
                    itemView.findViewById(R.id.imgThumb);

            // Procura o botão sem exigir que ele exista
            // no XML antigo do projeto.
            int idBotaoConcluir =
                    itemView.getResources().getIdentifier(
                            "btnConcluir",
                            "id",
                            itemView.getContext().getPackageName()
                    );

            if (idBotaoConcluir != 0) {
                btnConcluir =
                        itemView.findViewById(idBotaoConcluir);
            } else {
                btnConcluir = null;
            }
        }
    }
}