package com.example.ae;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;


public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText emailField, passwordField;
    private Button loginButton, registerButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        // ✅ Inicializar Firebase aquí
        FirebaseApp.initializeApp(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // ✅ Si quieres saltar el login temporalmente:
        if (true) { // pon una condición fija para pruebas
            startActivity(new Intent(this, TasksActivity.class));
            finish();
            return;
        }


        // Conectar los elementos del layout
        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);

        // Acciones de los botones
        loginButton.setOnClickListener(v -> loginUser());
        registerButton.setOnClickListener(v -> registerUser());




    }

    private void loginUser() {
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Login exitoso", Toast.LENGTH_SHORT).show();
                        // Aquí pasas a la pantalla principal de tareas
                    } else {
                        Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void registerUser() {
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Usuario registrado", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}


