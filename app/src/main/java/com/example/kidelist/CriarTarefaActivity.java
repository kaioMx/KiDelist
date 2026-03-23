package com.example.kidelist;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CriarTarefaActivity extends AppCompatActivity {

    private EditText etTitulo, etDescricao;
    private Spinner spTipoRepeticao;
    private Button btnSelecionarData, btnSelecionarHora, btnSalvarTarefa;
    private CheckBox cbRepetir;
    private ImageView btnVoltar;

    private String dataSelecionada = "";
    private String horaSelecionada = "";

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_criar_tarefa);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        inicializarViews();
        configurarSpinnerRepeticao();
        configurarEventos();
        FooterNavigation.setup(this, FooterNavigation.TELA_TAREFAS);
    }

    private void inicializarViews() {
        etTitulo = findViewById(R.id.etTitulo);
        etDescricao = findViewById(R.id.etDescricao);
        spTipoRepeticao = findViewById(R.id.spTipoRepeticao);
        btnSelecionarData = findViewById(R.id.btnSelecionarData);
        btnSelecionarHora = findViewById(R.id.btnSelecionarHora);
        btnSalvarTarefa = findViewById(R.id.btnSalvarTarefa);
        cbRepetir = findViewById(R.id.cbRepetir);
        btnVoltar = findViewById(R.id.btnVoltar);
    }

    private void configurarSpinnerRepeticao() {
        ArrayAdapter<CharSequence> adapterRepeticao = ArrayAdapter.createFromResource(
                this,
                R.array.tipos_repeticao_array,
                android.R.layout.simple_spinner_item
        );
        adapterRepeticao.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTipoRepeticao.setAdapter(adapterRepeticao);
        spTipoRepeticao.setVisibility(View.GONE);
    }

    private void configurarEventos() {
        btnVoltar.setOnClickListener(v -> {
            Intent intent = new Intent(CriarTarefaActivity.this, ChecklistActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        btnSelecionarData.setOnClickListener(v -> abrirDatePicker());
        btnSelecionarHora.setOnClickListener(v -> abrirTimePicker());

        cbRepetir.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                spTipoRepeticao.setVisibility(View.VISIBLE);
            } else {
                spTipoRepeticao.setVisibility(View.GONE);
                spTipoRepeticao.setSelection(0);
            }
        });

        btnSalvarTarefa.setOnClickListener(v -> salvarTarefa());
    }

    private void abrirDatePicker() {
        Calendar calendario = Calendar.getInstance();

        int ano = calendario.get(Calendar.YEAR);
        int mes = calendario.get(Calendar.MONTH);
        int dia = calendario.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    dataSelecionada = String.format(
                            Locale.getDefault(),
                            "%02d/%02d/%04d",
                            dayOfMonth,
                            month + 1,
                            year
                    );
                    btnSelecionarData.setText(dataSelecionada);
                },
                ano, mes, dia
        );

        datePickerDialog.show();
    }

    private void abrirTimePicker() {
        Calendar calendario = Calendar.getInstance();

        int hora = calendario.get(Calendar.HOUR_OF_DAY);
        int minuto = calendario.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    horaSelecionada = String.format(
                            Locale.getDefault(),
                            "%02d:%02d",
                            hourOfDay,
                            minute
                    );
                    btnSelecionarHora.setText(horaSelecionada);
                },
                hora, minuto, true
        );

        timePickerDialog.show();
    }

    private void salvarTarefa() {
        String titulo = etTitulo.getText().toString().trim();
        String descricao = etDescricao.getText().toString().trim();

        boolean repetir = cbRepetir.isChecked();
        String tipoRepeticao = repetir ? spTipoRepeticao.getSelectedItem().toString() : "Não repetir";

        if (titulo.isEmpty()) {
            etTitulo.setError("Informe o título da tarefa");
            etTitulo.requestFocus();
            return;
        }

        if (dataSelecionada.isEmpty()) {
            Toast.makeText(this, "Selecione uma data", Toast.LENGTH_SHORT).show();
            return;
        }

        if (horaSelecionada.isEmpty()) {
            Toast.makeText(this, "Selecione uma hora", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser usuario = auth.getCurrentUser();
        if (usuario == null) {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSalvarTarefa.setEnabled(false);

        Map<String, Object> tarefa = new HashMap<>();
        tarefa.put("titulo", titulo);
        tarefa.put("descricao", descricao);
        tarefa.put("data", dataSelecionada);
        tarefa.put("hora", horaSelecionada);
        tarefa.put("repetir", repetir);
        tarefa.put("tipoRepeticao", tipoRepeticao);
        tarefa.put("concluida", false);
        tarefa.put("userId", usuario.getUid());
        tarefa.put("createdAt", Timestamp.now());

        db.collection("tarefas")
                .add(tarefa)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(
                            CriarTarefaActivity.this,
                            "Tarefa salva com sucesso!",
                            Toast.LENGTH_SHORT
                    ).show();

                    limparCampos();
                    btnSalvarTarefa.setEnabled(true);

                    // opcional: voltar para checklist
                    // finish();
                })
                .addOnFailureListener(e -> {
                    btnSalvarTarefa.setEnabled(true);
                    Toast.makeText(
                            CriarTarefaActivity.this,
                            "Erro ao salvar: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void limparCampos() {
        etTitulo.setText("");
        etDescricao.setText("");
        cbRepetir.setChecked(false);
        spTipoRepeticao.setSelection(0);
        btnSelecionarData.setText("Selecionar data");
        btnSelecionarHora.setText("Selecionar hora");

        dataSelecionada = "";
        horaSelecionada = "";
    }
}