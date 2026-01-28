package com.example.kidelist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class TarefaChecklistAdapter extends RecyclerView.Adapter<TarefaChecklistAdapter.VH> {

    private final List<TarefaChecklist> itens;

    public TarefaChecklistAdapter(List<TarefaChecklist> itens) {
        this.itens = itens;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tarefa_checklist, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        TarefaChecklist item = itens.get(position);

        h.txtNomeTarefa.setText(item.getNome());

        // Status
        if (item.isFeito()) {
            h.txtStatus.setText("FEITO");
            h.txtStatus.setTextColor(0xFF27AE60); // verde
        } else {
            h.txtStatus.setText("PENDENTE");
            h.txtStatus.setTextColor(0xFFF2994A); // laranja
        }

        // Aplicar cores nos botões de nota (sempre reaplica para não "vazar" ao reciclar)
        aplicarEstiloNotas(h, item.getNota());

        // Clique em cada nota (NÃO usar 'position' capturado!)
        h.btn1.setOnClickListener(v -> atualizarNota(h.getAdapterPosition(), 1));
        h.btn2.setOnClickListener(v -> atualizarNota(h.getAdapterPosition(), 2));
        h.btn3.setOnClickListener(v -> atualizarNota(h.getAdapterPosition(), 3));
        h.btn4.setOnClickListener(v -> atualizarNota(h.getAdapterPosition(), 4));
        h.btn5.setOnClickListener(v -> atualizarNota(h.getAdapterPosition(), 5));


        // Exemplo: clique no botão câmera (você liga sua lógica depois)
        h.btnCamera.setOnClickListener(v -> {
            // TODO: abrir câmera/galeria
        });
    }

    private void atualizarNota(int position, int nota) {
        if (position == RecyclerView.NO_POSITION) return;

        itens.get(position).setNota(nota);
        // regra simples: se avaliou, marca feito (você pode mudar)
        itens.get(position).setFeito(true);

        notifyItemChanged(position);
    }

    private void aplicarEstiloNotas(VH h, int notaSelecionada) {
        // padrão: azul escuro
        setBtnStyle(h.btn1, notaSelecionada == 1);
        setBtnStyle(h.btn2, notaSelecionada == 2);
        setBtnStyle(h.btn3, notaSelecionada == 3);
        setBtnStyle(h.btn4, notaSelecionada == 4);
        setBtnStyle(h.btn5, notaSelecionada == 5);
    }

    private void setBtnStyle(MaterialButton btn, boolean selected) {
        if (selected) {
            btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFF4B400)); // amarelo
            btn.setTextColor(0xFF0E3B66); // azul escuro
        } else {
            btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF0E3B66)); // azul
            btn.setTextColor(0xFFFFFFFF); // branco
        }
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtNomeTarefa, txtStatus;
        MaterialButton btn1, btn2, btn3, btn4, btn5;
        View btnCamera;

        VH(@NonNull View itemView) {
            super(itemView);
            txtNomeTarefa = itemView.findViewById(R.id.txtNomeTarefa);
            txtStatus = itemView.findViewById(R.id.txtStatus);

            btn1 = itemView.findViewById(R.id.btn1);
            btn2 = itemView.findViewById(R.id.btn2);
            btn3 = itemView.findViewById(R.id.btn3);
            btn4 = itemView.findViewById(R.id.btn4);
            btn5 = itemView.findViewById(R.id.btn5);

            btnCamera = itemView.findViewById(R.id.btnCamera);
        }
    }
}
