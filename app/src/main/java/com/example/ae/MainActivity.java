package com.example.ae;

import android.content.Intent; // 🔹 Import necesario
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String userName = prefs.getString("userName", null);

        if (userName == null) {
            // Mostrar cuadro de diálogo solo la primera vez
            final EditText input = new EditText(this);
            input.setHint("Escribe tu nombre");
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);

            new AlertDialog.Builder(this)
                    .setTitle("Configura tu nombre")
                    .setMessage("Escribe tu nombre para identificar tus tareas")
                    .setView(input)
                    .setPositiveButton("Guardar", (dialog, which) -> {
                        String nombre = input.getText().toString().trim();
                        if (!nombre.isEmpty()) {
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("userName", nombre);
                            editor.apply();
                            Toast.makeText(this, "Nombre guardado: " + nombre, Toast.LENGTH_SHORT).show();

                            // 🔹 Lanzar TasksActivity después de guardar
                            Intent intent = new Intent(MainActivity.this, TasksActivity.class);
                            startActivity(intent);
                            finish(); // opcional, para que no regrese a MainActivity
                        } else {
                            Toast.makeText(this, "Debes escribir un nombre", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancelar", (dialog, which) -> {
                        Toast.makeText(this, "No se configuró ningún nombre", Toast.LENGTH_SHORT).show();
                    })
                    .setCancelable(false) // 🔹 evita que se cierre tocando fuera
                    .show();
        } else {
            // Ya existe un nombre guardado
            Toast.makeText(this, "Bienvenido de nuevo, " + userName, Toast.LENGTH_SHORT).show();

            // 🔹 Lanzar directamente TasksActivity si ya hay nombre
            Intent intent = new Intent(MainActivity.this, TasksActivity.class);
            startActivity(intent);
            finish(); // opcional
        }
    }
}