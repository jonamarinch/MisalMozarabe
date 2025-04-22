package com.example.appmisalmozarabe.presentation

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appmisalmozarabe.R
import com.google.android.material.button.MaterialButton

class DisplayActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display) // Tu ScrollView con LinearLayout

        val contenedor = findViewById<LinearLayout>(R.id.contenedorTextos)

        // Recuperar datos del intent
        val nombreFiesta = intent.getStringExtra("nombreFiesta") ?: "Fiesta desconocida"

        // Crear y añadir TextView para el título de la fiesta
        val tituloTextView = TextView(this).apply {
            text = nombreFiesta
            textSize = 24f
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 24)
        }
        contenedor.addView(tituloTextView)

        // Botón para volver
        val botonVolver = findViewById<MaterialButton>(R.id.btnBack)

        // Listener para el botón para volver
        botonVolver.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}
