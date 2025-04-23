package com.example.appmisalmozarabe.presentation

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.appmisalmozarabe.R
import com.google.android.material.button.MaterialButton
import com.example.appmisalmozarabe.data.SQLiteHelper
import com.example.appmisalmozarabe.domain.model.TextoLiturgico

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

        // Crear y añadir TextView para el subtítulo
        val subtituloTextView = TextView(this).apply {
            text = "Oficio de la Misa"
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 95)
        }
        contenedor.addView(subtituloTextView)

        // Clase para recoger datos
        val dbHelper = SQLiteHelper(this)

        // Comprobar fiesta y tiempo
        val seleccion = dbHelper.getCodigoFiestaYTiempo(nombreFiesta)

        // Controlar impresión de secciones por tiempos y fiestas
        when (seleccion?.second) {
            "ADV" -> {
                // Acción para Adviento

                // Cargar textos Praelegendum
                val textosPrae = dbHelper.getPraelegendum(seleccion?.first, seleccion?.second)
                imprimirTextosLiturgicos(contenedor, textosPrae)
            }
            "NAV" -> {
                // Acción para Navidad
            }
            "DEP" -> {
                // Acción para Después de Epifanía

                // Cargar textos Praelegendum
                val textosPrae = dbHelper.getPraelegendum(seleccion?.first, seleccion?.second)
                imprimirTextosLiturgicos(contenedor, textosPrae)
            }
            "CUA" -> {
                // Acción para Cuaresma
            }
            "SES" -> {
                // Acción para Semana Santa
            }
            "PAS" -> {
                // Acción para Pascua
            }
            "PEN" -> {
                // Acción para Pentecostés
            }
            "DPE" -> {
                // Acción para Después de Pentecostés
            }
            null -> {
                // Manejo cuando no hay selección
            }
        }

        // Botón para volver
        val botonVolver = findViewById<MaterialButton>(R.id.btnBack)

        // Listener para el botón para volver
        botonVolver.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                // Recupera los valores actuales de los spinners
                val nombreTiempo = dbHelper.getTiempoFromFiesta(nombreFiesta)
                putExtra("tiempo", nombreTiempo) // Reemplaza con tu variable real
                putExtra("fiesta", nombreFiesta) // Reemplaza con tu variable real
            }
            startActivity(intent)
            finish() // Cierra la actividad actual
        }
    }

    // Metodo para manejar la impresión de textos litúrgicos
    private fun imprimirTextosLiturgicos(contenedor: LinearLayout, textos: List<TextoLiturgico>) {
        for (texto in textos) {
            when (texto.tipo) {
                0 -> imprimirTextoNormal(contenedor, texto.contenido)
                1 -> imprimirRubrica(contenedor, texto.contenido)
                2 -> imprimirSemiRubrica(contenedor, texto.contenido)
                3 -> imprimirRubricaCentrada(contenedor, texto.contenido)
                4 -> imprimirRubricaCentradaNegrita(contenedor, texto.contenido)
                5 -> imprimirTextoPueblo(contenedor, texto.contenido)
                6 -> imprimirTextoCoro(contenedor, texto.contenido)
                else -> imprimirTextoNormal(contenedor, texto.contenido)
            }
        }
    }

    // Metodo para imprimir texto normal
    private fun imprimirTextoNormal(contenedor: LinearLayout, texto: String) {
        val textView = TextView(this).apply {
            text = texto
            textSize = 18f
            setPadding(16, 8, 16, 8)
            setTextColor(Color.BLACK)
        }
        contenedor.addView(textView)
    }

    // Metodo para imprimir rubrica
    private fun imprimirRubrica(contenedor: LinearLayout, texto: String) {
        val textView = TextView(this).apply {
            text = texto
            textSize = 18f
            setTypeface(null, Typeface.ITALIC)
            setTextColor(resources.getColor(R.color.rubrica, null)) // Asegúrate de tener este color en `colors.xml`
            setPadding(16, 8, 16, 8)
        }
        contenedor.addView(textView)
    }

    // Metodo para imprimir rubrica centrada
    private fun imprimirRubricaCentrada(contenedor: LinearLayout, texto: String) {
        val textView = TextView(this).apply {
            text = texto
            textSize = 18f
            setTypeface(null, Typeface.ITALIC)
            setTextColor(resources.getColor(R.color.rubrica, null)) // Asegúrate de tener este color en `colors.xml`
            setPadding(16, 8, 16, 95)
            // Centrar texto horizontalmente
            gravity = Gravity.CENTER
        }
        contenedor.addView(textView)
    }

    // Metodo para imprimir rubrica centrada y en negrita
    private fun imprimirRubricaCentradaNegrita(contenedor: LinearLayout, texto: String) {
        val textView = TextView(this).apply {
            text = texto
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(resources.getColor(R.color.rubrica, null)) // Asegúrate de tener este color en `colors.xml`
            setPadding(16, 8, 16, 8)
            // Centrar texto horizontalmente
            gravity = Gravity.CENTER
        }
        contenedor.addView(textView)
    }

    // Metodo para imprimir rubrica (al principio) y texto normal
    private fun imprimirSemiRubrica(contenedor: LinearLayout, texto: String) {
        // Buscar el índice del primer punto o dos puntos
        val indiceFinRubrica = texto.indexOfFirst { it == '.' || it == ':' }
        // Si no se encuentra punto ni dos puntos, tratamos todo como rubrica
        val rubricaHasta = if (indiceFinRubrica != -1) indiceFinRubrica + 1 else texto.length

        val spannable = android.text.SpannableString(texto)

        // Aplicar estilo a la parte de la rúbrica
        spannable.setSpan(
            android.text.style.StyleSpan(Typeface.ITALIC),
            0,
            rubricaHasta,
            android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            android.text.style.ForegroundColorSpan(resources.getColor(R.color.rubrica, null)),
            0,
            rubricaHasta,
            android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        // El resto del texto (opcional: puedes aplicar estilo "normal" explícitamente si quieres)
        spannable.setSpan(
            android.text.style.ForegroundColorSpan(resources.getColor(R.color.black, null)),
            rubricaHasta,
            texto.length,
            android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        val textView = TextView(this).apply {
            text = spannable
            textSize = 18f
            setPadding(16, 8, 16, 8)
        }

        contenedor.addView(textView)
    }

    // Metodo para imprimir texto del pueblo
    private fun imprimirTextoPueblo(contenedor: LinearLayout, texto: String) {
        val textView = TextView(this).apply {
            text = texto
            textSize = 18f
            setPadding(125, 8, 16, 8)
            setTextColor(Color.BLACK)
            typeface = Typeface.DEFAULT_BOLD
        }
        contenedor.addView(textView)
    }

    // Metodo para imprimir texto del coro
    private fun imprimirTextoCoro(contenedor: LinearLayout, texto: String) {
        val textView = TextView(this).apply {
            text = texto
            textSize = 18f
            setPadding(16, 8, 16, 8)
            setTextColor(Color.BLACK)
            typeface = Typeface.DEFAULT_BOLD
        }
        contenedor.addView(textView)
    }
}