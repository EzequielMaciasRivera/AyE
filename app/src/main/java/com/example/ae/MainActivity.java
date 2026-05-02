package com.example.ae;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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

                            // 🔹 Toast personalizado
                            View layout = LayoutInflater.from(this)
                                    .inflate(R.layout.custom_toast, (ViewGroup) findViewById(android.R.id.content), false);

                            TextView text = layout.findViewById(R.id.toastText);
                            text.setText("Nombre guardado: " + nombre);

                            ImageView icon = layout.findViewById(R.id.toastIcon);
                            icon.setImageResource(R.drawable.enamorado); // usa tu drawable

                            Toast toast = new Toast(this);
                            toast.setDuration(Toast.LENGTH_SHORT);
                            toast.setView(layout);
                            toast.show();

                            // 🔹 Lanzar TasksActivity después de guardar
                            Intent intent = new Intent(MainActivity.this, TasksActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // 🔹 Toast de error personalizado
                            View layout = LayoutInflater.from(this)
                                    .inflate(R.layout.custom_toast, (ViewGroup) findViewById(android.R.id.content), false);

                            TextView text = layout.findViewById(R.id.toastText);
                            text.setText("Debes escribir un nombre");

                            ImageView icon = layout.findViewById(R.id.toastIcon);
                            icon.setImageResource(R.drawable.no);

                            Toast toast = new Toast(this);
                            toast.setDuration(Toast.LENGTH_SHORT);
                            toast.setView(layout);
                            toast.show();
                        }
                    })
                    .setNegativeButton("Cancelar", (dialog, which) -> {
                        // 🔹 Toast personalizado al cancelar
                        View layout = LayoutInflater.from(this)
                                .inflate(R.layout.custom_toast, (ViewGroup) findViewById(android.R.id.content), false);

                        TextView text = layout.findViewById(R.id.toastText);
                        text.setText("No se configuró ningún nombre");

                        ImageView icon = layout.findViewById(R.id.toastIcon);
                        icon.setImageResource(R.drawable.sorpresa);

                        Toast toast = new Toast(this);
                        toast.setDuration(Toast.LENGTH_SHORT);
                        toast.setView(layout);
                        toast.show();
                    })
                    .setCancelable(false)
                    .show();
        } else {
            // Ya existe un nombre guardado
            View layout = LayoutInflater.from(this)
                    .inflate(R.layout.custom_toast, (ViewGroup) findViewById(android.R.id.content), false);

            TextView text = layout.findViewById(R.id.toastText);
            text.setText("Bienvenid@ de nuevo, " + userName);

            ImageView icon = layout.findViewById(R.id.toastIcon);
            icon.setImageResource(R.drawable.enamorado);

            Toast toast = new Toast(this);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setView(layout);
            toast.show();

            // 🔹 Lanzar directamente TasksActivity si ya hay nombre
            Intent intent = new Intent(MainActivity.this, TasksActivity.class);
            startActivity(intent);
            finish();
        }
    }
}