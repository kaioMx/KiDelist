package com.example.kidelist;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsuario, etSenha;
    private Button btnEntrar;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsuario = findViewById(R.id.etUsuario);
        etSenha = findViewById(R.id.etSenha);
        btnEntrar = findViewById(R.id.btnEntrar);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnEntrar.setOnClickListener(v -> {
            String email = etUsuario.getText().toString().trim();
            String senha = etSenha.getText().toString().trim();

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha e-mail e senha.", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.signInWithEmailAndPassword(email, senha)
                    .addOnSuccessListener(result -> {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user == null) {
                            Toast.makeText(this, "Falha no login.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        carregarPerfil(user.getUid());
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Login inválido: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });
    }

    private void carregarPerfil(String uid) {
        db.collection("usuarios").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this,
                                "Usuário logou no Auth, mas não tem perfil no Firestore (usuarios/{uid}).",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    String tipo = doc.getString("tipo");
                    String lojaId = doc.getString("lojaId");

                    if (tipo == null || lojaId == null) {
                        Toast.makeText(this, "Perfil incompleto (tipo/lojaId).", Toast.LENGTH_LONG).show();
                        return;
                    }

                    Intent it = new Intent(this, ChecklistActivity.class);
                    it.putExtra("lojaId", lojaId);
                    it.putExtra("tipo", tipo);
                    startActivity(it);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao carregar perfil: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}