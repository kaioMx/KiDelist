package com.example.kidelist;

import android.app.Activity;
import android.content.Intent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.example.kidelist.RankingActivity;
import androidx.core.content.ContextCompat;

public class FooterNavigation {

    public static final int TELA_TAREFAS = 1;
    public static final int TELA_RANKING = 2;

    public static void setup(Activity activity, int telaAtual) {
        LinearLayout btnTarefas = activity.findViewById(R.id.btnTarefas);
        LinearLayout btnRanking = activity.findViewById(R.id.btnRanking);

        ImageView iconTarefas = activity.findViewById(R.id.iconTarefas);
        ImageView iconRanking = activity.findViewById(R.id.iconRanking);

        if (btnTarefas != null && iconTarefas != null) {
            if (telaAtual == TELA_TAREFAS) {
                btnTarefas.setBackgroundResource(R.drawable.bg_footer_item_active);
                iconTarefas.setColorFilter(ContextCompat.getColor(activity, R.color.kidelist_primary));
            } else {
                btnTarefas.setBackgroundResource(R.drawable.bg_footer_item_inactive);
                iconTarefas.setColorFilter(ContextCompat.getColor(activity, android.R.color.darker_gray));
            }

            btnTarefas.setOnClickListener(v -> {
                if (telaAtual != TELA_TAREFAS) {
                    Intent intent = new Intent(activity, ChecklistActivity.class);
                    activity.startActivity(intent);
                }
            });
        }

        if (btnRanking != null && iconRanking != null) {
            if (telaAtual == TELA_RANKING) {
                btnRanking.setBackgroundResource(R.drawable.bg_footer_item_active);
                iconRanking.setColorFilter(ContextCompat.getColor(activity, R.color.kidelist_primary));
            } else {
                btnRanking.setBackgroundResource(R.drawable.bg_footer_item_inactive);
                iconRanking.setColorFilter(ContextCompat.getColor(activity, android.R.color.darker_gray));
            }

            btnRanking.setOnClickListener(v -> {
                if (telaAtual != TELA_RANKING) {
                    Intent intent = new Intent(activity, RankingActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    activity.startActivity(intent);
                }
            });
        }
    }
}