package com.example.appmisalmozarabe.presentation

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.text.Spannable
import android.text.SpannableString
import android.view.Gravity
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.app.AlertDialog
import android.content.res.Configuration
import android.util.TypedValue
import android.view.View
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.appmisalmozarabe.R
import com.google.android.material.button.MaterialButton
import com.example.appmisalmozarabe.data.SQLiteHelper
import com.example.appmisalmozarabe.data.ComentarioManager
import com.example.appmisalmozarabe.domain.model.TextoLiturgico

class DisplayActivity : AppCompatActivity() {
    // Lista para recoger todos los campos EditText mostrados en modo lectura y en modo edicion
    val textosControl = mutableListOf<Triple<String, String, String>>() // (tabla, original, nuevo)
    // Lista para recoger todos los campos EditText mostrados en modo edición
    val textosEditados= mutableListOf<EditText>()
    // Índice con las secciones
    private val indiceSecciones = mutableMapOf<String, View>()

    // Indicaciones al crear la actividad
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Manejo de preferencias guardadas para modo claro/oscuro
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        val nightModeSaved = prefs.getBoolean("modoOscuro", false)
        AppCompatDelegate.setDefaultNightMode(
            if (nightModeSaved) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )

        // Inflar layout principal con ScrollView y contenedor vertical
        setContentView(R.layout.activity_display) // Tu ScrollView con LinearLayout
        val contenedor = findViewById<LinearLayout>(R.id.contenedorTextos)

        // Metodo para ajustar el tamaño del texto
        fun ajustarTamanoTexto(tamano: Float) {
            val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
            prefs.edit().putFloat("tamanoTexto", tamano).apply()

            for (i in 0 until contenedor.childCount) {
                val vista = contenedor.getChildAt(i)
                if ((vista is TextView || vista is EditText)) {
                    (vista as TextView).setTextSize(TypedValue.COMPLEX_UNIT_SP, tamano)
                }
            }
        }

        // Añadir control del tamaño del texto al principio
        val controlTamano = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(16, 16, 16, 16)
            gravity = Gravity.RIGHT

            val texto = TextView(this@DisplayActivity).apply {
                text = "tamaño:"
                textSize = 18f
                setTextColor(Color.parseColor("#CC8800"))
                setPadding(0, 0, 24, 0)
            }

            val btnSmall = TextView(this@DisplayActivity).apply {
                text = "A"
                textSize = 14f
                setTextColor(Color.parseColor("#CC8800"))
                setPadding(16, 0, 16, 0)
                setOnClickListener { ajustarTamanoTexto(14f) }
            }

            val btnMedium = TextView(this@DisplayActivity).apply {
                text = "A"
                textSize = 18f
                setTextColor(Color.parseColor("#CC8800"))
                setPadding(16, 0, 16, 0)
                setOnClickListener { ajustarTamanoTexto(18f) }
            }

            val btnLarge = TextView(this@DisplayActivity).apply {
                text = "A"
                textSize = 24f
                setTextColor(Color.parseColor("#CC8800"))
                setPadding(16, 0, 16, 0)
                setOnClickListener { ajustarTamanoTexto(24f) }
            }

            addView(texto)
            addView(btnSmall)
            addView(btnMedium)
            addView(btnLarge)
        }

        // Añadir al inicio del contenedor
        contenedor.addView(controlTamano, 0)

        // Recuperar datos del intent
        val nombreFiesta = intent.getStringExtra("nombreFiesta") ?: "Fiesta desconocida"
        // Boolean para registrar el modo actual (Lectura o edición)
        var enModoEdicion = intent.getBooleanExtra("modoEdicion", false)

        // Crear y añadir TextView para el título de la fiesta
        val tituloTextView = TextView(this).apply {
            text = nombreFiesta
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                setTextColor(Color.WHITE)
            } else {
                setTextColor(Color.BLACK)
            }
            gravity = Gravity.CENTER
            setPadding(0, 24, 0, 24)
        }
        contenedor.addView(tituloTextView)

        // Crear y añadir TextView para el subtítulo
        val subtituloTextView = TextView(this).apply {
            text = "Oficio de la Misa"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                setTextColor(Color.WHITE)
            } else {
                setTextColor(Color.BLACK)
            }
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 95)
        }
        contenedor.addView(subtituloTextView)

        // Clase para recoger datos
        val dbHelper = SQLiteHelper(this)

        // Comprobar fiesta y tiempo
        val seleccion = dbHelper.getCodigoFiestaYTiempo(nombreFiesta)

        // Cuando proceda, mostrar comentarios
        if (seleccion?.first != null && enModoEdicion) {
            mostrarComentarios(seleccion.first)
        }

        /*
        'Ordinarium' del comienzo de la misa
         */

        // Cargar textos Ordinarium
        var textosCargados = dbHelper.getTextos(seleccion?.first, seleccion?.second, "ORDINARIUM")
        imprimirTextosLiturgicos(contenedor, textosCargados, enModoEdicion, "ORDINARIUM")
        // Cargar textos Accedendi
        textosCargados = dbHelper.getTextos(seleccion?.first, seleccion?.second, "ACCEDENDI")
        imprimirTextosLiturgicos(contenedor, textosCargados, enModoEdicion, "ACCEDENDI")
        // Cargar textos Confessio
        textosCargados = dbHelper.getTextos(seleccion?.first, seleccion?.second, "CONFESSIO")
        imprimirTextosLiturgicos(contenedor, textosCargados, enModoEdicion, "CONFESSIO")
        // Cargar textos Absolutio
        textosCargados = dbHelper.getTextos(seleccion?.first, seleccion?.second, "ABSOLUTIO")
        imprimirTextosLiturgicos(contenedor, textosCargados, enModoEdicion, "ABSOLUTIO")
        // Cargar textos AdAltare
        textosCargados = dbHelper.getTextos(seleccion?.first, seleccion?.second, "ADALTARE")
        imprimirTextosLiturgicos(contenedor, textosCargados, enModoEdicion, "ADALTARE")
        // Cargar textos AnteCrucem
        textosCargados = dbHelper.getTextos(seleccion?.first, seleccion?.second, "ANTECRUCEM")
        imprimirTextosLiturgicos(contenedor, textosCargados, enModoEdicion, "ANTECRUCEM")
        // Cargar textos ExtensionisCorporalis
        textosCargados = dbHelper.getTextos(seleccion?.first, seleccion?.second, "EXTENSIONISCORPORALIS")
        imprimirTextosLiturgicos(contenedor, textosCargados, enModoEdicion, "EXTENSIONISCORPORALIS")
        // Cargar textos Mixtionis
        textosCargados = dbHelper.getTextos(seleccion?.first, seleccion?.second, "MIXTIONIS")
        imprimirTextosLiturgicos(contenedor, textosCargados, enModoEdicion, "MIXTIONIS")

        /*
        Controlar impresión de secciones por tiempos y fiestas
         */
        when (seleccion?.second) {

            "ADV" -> {
                // Acción para Adviento

                // Cargar textos Praelegendum
                textosCargados = dbHelper.getTextos(seleccion?.first, seleccion?.second, "PRAELEGENDUM")
                imprimirTextosLiturgicos(contenedor, textosCargados, enModoEdicion, "PRAELEGENDUM")
                // Cargar textos PostGloriam
                textosCargados = dbHelper.getTextos(seleccion?.first, seleccion?.second, "POSTGLORIAM")
                imprimirTextosLiturgicos(contenedor, textosCargados, enModoEdicion, "POSTGLORIAM")
                // Cargar textos Profecias
                textosCargados = dbHelper.getTextos(seleccion?.first, seleccion?.second, "PROFECIA")
                imprimirTextosLiturgicos(contenedor, textosCargados, enModoEdicion, "PROFECIA")
                // Cargar textos Salmo
                textosCargados = dbHelper.getTextos(seleccion?.first, seleccion?.second, "SALMO")
                imprimirTextosLiturgicos(contenedor, textosCargados, enModoEdicion, "SALMO")
                // Cargar textos Apostol
                textosCargados = dbHelper.getTextos(seleccion?.first, seleccion?.second, "APOSTOL")
                imprimirTextosLiturgicos(contenedor, textosCargados, enModoEdicion, "APOSTOL")
                // Cargar textos Evangelio
                textosCargados = dbHelper.getTextos(seleccion?.first, seleccion?.second, "EVANGELIO")
                imprimirTextosLiturgicos(contenedor, textosCargados, enModoEdicion, "EVANGELIO")
                // Cargar textos Laudes
                textosCargados = dbHelper.getTextos(seleccion?.first, seleccion?.second, "LAUDES")
                imprimirTextosLiturgicos(contenedor, textosCargados, enModoEdicion, "LAUDES")
                // Cargar textos Sacrificium
                textosCargados = dbHelper.getTextos(seleccion?.first, seleccion?.second, "SACRIFICIUM")
                imprimirTextosLiturgicos(contenedor, textosCargados, enModoEdicion, "SACRIFICIUM")
                // Cargar textos IncipitMissa
                textosCargados = dbHelper.getTextos(seleccion?.first, seleccion?.second, "INCIPITMISSA")
                imprimirTextosLiturgicos(contenedor, textosCargados, enModoEdicion, "INCIPITMISSA")
                // Cargar textos OratioAdmonitionis
                textosCargados = dbHelper.getTextos(seleccion?.first, seleccion?.second, "ORATIOADMONITIONIS")
                imprimirTextosLiturgicos(contenedor, textosCargados, enModoEdicion, "ORATIOADMONITIONIS")
            }
            "NAV" -> {
                // Acción para Navidad
            }
            "DEP" -> {
                // Acción para Después de Epifanía
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

        // Manejo de preferencias guardadas para tamaño de texto (ya se han imprimido y se pueden ajustar)
        val tamanoGuardado = prefs.getFloat("tamanoTexto", 18f) // valor por defecto
        ajustarTamanoTexto(tamanoGuardado)

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

        // Botón para controlar el modo claro/oscuro
        val btnModo = findViewById<MaterialButton>(R.id.btnModo)
        // Listener del botón para controlar el modo claro/oscuro
        btnModo.setOnClickListener {
            val modoActual = AppCompatDelegate.getDefaultNightMode()
            val nuevoModo = if (modoActual == AppCompatDelegate.MODE_NIGHT_YES) {
                AppCompatDelegate.MODE_NIGHT_NO
            } else {
                AppCompatDelegate.MODE_NIGHT_YES
            }

            // Guardar preferencia
            prefs.edit().putBoolean("modoOscuro", nuevoModo == AppCompatDelegate.MODE_NIGHT_YES).apply()
            AppCompatDelegate.setDefaultNightMode(nuevoModo)

            recreate()
        }

        // Botón para facilitar la navegación del usuario
        val btnNav = findViewById<MaterialButton>(R.id.btnNav)
        btnNav.setOnClickListener {
            val nombresSecciones = indiceSecciones.keys.toList()

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Ir a sección...")
            builder.setItems(nombresSecciones.toTypedArray()) { _, which ->
                val nombre = nombresSecciones[which]
                val vistaDestino = indiceSecciones[nombre]
                vistaDestino?.let {
                    it.post {
                        findViewById<ScrollView>(R.id.scrollView).smoothScrollTo(0, it.top)
                    }
                }
            }
            builder.show()
        }

        // Botón para editar
        val btnEdit = findViewById<MaterialButton>(R.id.btnEdit)

        // Botón para guardar cambios
        val btnSave = findViewById<MaterialButton>(R.id.btnSave)

        // Controlar visibilidad de cada botón
        if (enModoEdicion) {
            btnEdit.visibility = View.GONE
            btnSave.visibility = View.VISIBLE
        } else {
            btnEdit.visibility = View.VISIBLE
            btnSave.visibility = View.GONE
        }

        // Listener para el botón para editar
        btnEdit.setOnClickListener {
            // Crear campo de texto para la contraseña
            val input = EditText(this).apply {
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                hint = "Contraseña"
            }

            val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
            if (prefs.getBoolean("adminAutenticado", false)) {
                // Ya autenticado, pasar a modo edición directamente
                recargarActividadModoEdit()
                return@setOnClickListener
            } else {
                // Crear campo de texto para la contraseña
                val input = EditText(this).apply {
                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    hint = "Contraseña"
                    setPadding(32, 24, 32, 24) // Espaciado interno
                }

                // Contenedor para aplicar márgenes al input
                val container = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(48, 32, 48, 0) // Padding exterior del contenedor
                    addView(input)
                }

                // Mostrar diálogo
                val dialog = AlertDialog.Builder(this)
                    .setTitle("Acceso restringido")
                    .setMessage("Introduce la contraseña de administrador para continuar.")
                    .setView(container)
                    .setPositiveButton("Aceptar") { _, _ ->
                        val enteredPassword = input.text.toString()
                        if (dbHelper.verificarContrasenna(enteredPassword)) {
                            Toast.makeText(this, "Acceso concedido", Toast.LENGTH_SHORT).show()
                            getSharedPreferences("prefs", MODE_PRIVATE).edit()
                                .putBoolean("adminAutenticado", true).apply()
                            recargarActividadModoEdit()
                        } else {
                            Toast.makeText(this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("Cancelar", null)
                    .create()
                // Esto lo hago para que los colores de los botones se ajusten al modo
                dialog.setOnShowListener {
                    val nightModeFlags =
                        resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                    val color = if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                        Color.parseColor("#80D8FF") // azul claro
                    } else {
                        Color.parseColor("#01579B") // azul oscuro
                    }

                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(color)
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(color)
                }
                dialog.show()
            }
        }

        // Listener para el botón para guardar cambios
        btnSave.setOnClickListener {
            val dbHelper = SQLiteHelper(this)

            if (textosControl.size != textosEditados.size) {
                Toast.makeText(this, "Error: las listas de control y edición no coinciden", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            for (i in textosControl.indices) {
                val (tabla, textoOriginal, _) = textosControl[i]
                val textoNuevo = textosEditados[i].text.toString()

                // Evita updates innecesarios
                if (textoNuevo != textoOriginal) {
                    dbHelper.actualizarTextoPorContenido(tabla, textoOriginal, textoNuevo)
                }

                // Actualizar la lista de control (opcional, por si acaso)
                textosControl[i] = Triple(tabla, textoOriginal, textoNuevo)
            }
            // Notificación de cambios guardados
            Toast.makeText(this, "Cambios guardados", Toast.LENGTH_SHORT).show()
            // Recargar en modo lectura
            recargarActividadModoLect()
        }
    }

    // Metodo para manejar la impresión de textos litúrgicos
    private fun imprimirTextosLiturgicos(contenedor: LinearLayout, textos: List<TextoLiturgico>, modoEdicion: Boolean, tabla: String) {
        for (texto in textos) {
            when (texto.tipo) {
                0 -> imprimirTextoNormal(contenedor, texto.contenido, modoEdicion, tabla)
                1 -> imprimirRubrica(contenedor, texto.contenido, modoEdicion, tabla)
                2 -> imprimirSemiRubrica(contenedor, texto.contenido, modoEdicion, tabla)
                3 -> imprimirRubricaCentrada(contenedor, texto.contenido, modoEdicion, tabla)
                4 -> imprimirRubricaCentradaNegrita(contenedor, texto.contenido, modoEdicion, tabla)
                5 -> imprimirTextoPueblo(contenedor, texto.contenido, modoEdicion, tabla)
                6 -> imprimirTextoCoro(contenedor, texto.contenido, modoEdicion, tabla)
                else -> imprimirTextoNormal(contenedor, texto.contenido, modoEdicion, tabla)
            }
        }
    }

    // Metodo para imprimir texto normal
    private fun imprimirTextoNormal(contenedor: LinearLayout, texto: String, modoEdicion: Boolean, tabla: String) {
        if (modoEdicion) {
            // En modo edición, usamos EditText
            val editText = EditText(this).apply {
                setText(texto)
                textSize = 18f
                setPadding(16, 8, 16, 8)
                val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                    setTextColor(Color.WHITE)
                } else {
                    setTextColor(Color.BLACK)
                }
                background = null // Quitar fondo para que parezca un TextView
            }
            contenedor.addView(editText)
            textosEditados.add(editText) // Guardamos para acceder luego si es necesario
            textosControl.add(Triple(tabla, texto, "")) // aún no sabemos el valor nuevo
        } else {
            // En modo lectura, usamos TextView
            val textView = TextView(this).apply {
                textSize = 18f
                setPadding(16, 8, 16, 8)
                val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                    setTextColor(Color.WHITE)
                } else {
                    setTextColor(Color.BLACK)
                }
            }

            // Verificar si el texto contiene el símbolo '✠'
            if (texto.contains("✠")) {
                val spannable = SpannableString(texto)
                val startIndex = texto.indexOf("✠")
                val endIndex = startIndex + 1 // "✠" ocupa 1 caracter

                // Aplicar color rubrica al símbolo
                spannable.setSpan(
                    android.text.style.ForegroundColorSpan(resources.getColor(R.color.rubrica, null)),
                    startIndex,
                    endIndex,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                textView.text = spannable
            } else {
                textView.text = texto
            }

            contenedor.addView(textView)
        }
    }

    // Metodo para imprimir rubrica
    private fun imprimirRubrica(contenedor: LinearLayout, texto: String, modoEdicion: Boolean, tabla: String) {
        if (modoEdicion) {
            // En modo edición, usamos EditText con estilo de rúbrica
            val editText = EditText(this).apply {
                setText(texto)
                textSize = 18f
                setTypeface(null, Typeface.ITALIC)
                setTextColor(resources.getColor(R.color.rubrica, null)) // Asegúrate de tener este color en `colors.xml`
                setPadding(16, 8, 16, 8)
                background = null // Sin fondo para simular TextView
            }
            contenedor.addView(editText)
            textosEditados.add(editText) // Guardamos referencia si queremos recuperar contenido luego
            textosControl.add(Triple(tabla, texto, "")) // aún no sabemos el valor nuevo
        } else {
            // En modo normal, usamos TextView
            val textView = TextView(this).apply {
                text = texto
                textSize = 18f
                setTypeface(null, Typeface.ITALIC)
                setTextColor(resources.getColor(R.color.rubrica, null)) // Asegúrate de tener este color en `colors.xml`
                setPadding(16, 8, 16, 8)
            }
            contenedor.addView(textView)
        }
    }

    // Metodo para imprimir rubrica centrada
    private fun imprimirRubricaCentrada(contenedor: LinearLayout, texto: String, modoEdicion: Boolean, tabla: String) {
        if (modoEdicion) {
            // En modo edición, usamos EditText con estilo de rúbrica centrada
            val editText = EditText(this).apply {
                setText(texto)
                textSize = 18f
                setTypeface(null, Typeface.ITALIC)
                setTextColor(resources.getColor(R.color.rubrica, null)) // Asegúrate de tener este color en `colors.xml`
                setPadding(16, 8, 16, 8)
                gravity = Gravity.CENTER // Centrar texto horizontalmente
                background = null // Sin fondo para simular TextView
            }
            contenedor.addView(editText)
            textosEditados.add(editText) // Guardamos referencia si queremos recuperar contenido luego
            textosControl.add(Triple(tabla, texto, "")) // aún no sabemos el valor nuevo
        } else {
            // En modo normal, usamos TextView
            val textView = TextView(this).apply {
                text = texto
                textSize = 18f
                setTypeface(null, Typeface.ITALIC)
                setTextColor(resources.getColor(R.color.rubrica, null)) // Asegúrate de tener este color en `colors.xml`
                setPadding(16, 8, 16, 8)
                // Centrar texto horizontalmente
                gravity = Gravity.CENTER
            }
            contenedor.addView(textView)
        }
    }

    // Metodo para imprimir rubrica centrada y en negrita
    private fun imprimirRubricaCentradaNegrita(contenedor: LinearLayout, texto: String, modoEdicion: Boolean, tabla: String) {
        if (modoEdicion) {
            // En modo edición, usamos EditText con estilo centrado y en negrita
            val editText = EditText(this).apply {
                setText(texto)
                textSize = 18f
                setTypeface(null, Typeface.BOLD)
                setTextColor(resources.getColor(R.color.rubrica, null)) // Asegúrate de tener este color en `colors.xml`
                setPadding(16, 8, 16, 8)
                gravity = Gravity.CENTER // Centrar texto horizontalmente
                background = null // Sin fondo para parecerse a TextView
            }
            contenedor.addView(editText)
            textosEditados.add(editText) // Guardamos referencia si queremos recuperar contenido luego
            textosControl.add(Triple(tabla, texto, "")) // aún no sabemos el valor nuevo
            indiceSecciones[texto] = editText // Agregamos título de sección
        } else {
            // En modo normal, usamos TextView
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
            indiceSecciones[texto] = textView // Agregamos título de sección
        }
    }


    // Metodo para imprimir rubrica (al principio) y texto normal
    @SuppressLint("ResourceType")
    private fun imprimirSemiRubrica(contenedor: LinearLayout, texto: String, modoEdicion: Boolean, tabla: String) {
        // Buscar el índice del primer punto o dos puntos
        val indiceFinRubrica = texto.indexOfFirst { it == '.' || it == ':' }
        // Si no se encuentra punto ni dos puntos, tratamos todo como rubrica
        val rubricaHasta = if (indiceFinRubrica != -1) indiceFinRubrica + 1 else texto.length

        if (modoEdicion) {
            // En modo edición, usamos EditText plano (sin formato parcial)
            val editText = EditText(this).apply {
                setText(texto)
                textSize = 18f
                setPadding(16, 8, 16, 8)
                val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                    setTextColor(Color.WHITE)
                } else {
                    setTextColor(Color.BLACK)
                }
                background = null // Sin fondo
            }
            contenedor.addView(editText)
            textosEditados.add(editText) // Guardamos referencia si queremos recuperar contenido luego
            textosControl.add(Triple(tabla, texto, "")) // aún no sabemos el valor nuevo
        } else {
            // En modo lectura, aplicamos estilos separados con Spannable
            val spannable = SpannableString(texto)

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

            // Necesario para controlar el color del texto según el modo
            var colorTexto = Color.BLACK;
            val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                colorTexto = Color.WHITE
            }

            spannable.setSpan(
                android.text.style.ForegroundColorSpan(colorTexto),
                rubricaHasta,
                texto.length,
                android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            // Verificar si el texto contiene el símbolo '✠'
            if (texto.contains("✠")) {
                val startIndex = texto.indexOf("✠")
                val endIndex = startIndex + 1 // "✠" ocupa 1 caracter

                // Aplicar color rubrica al símbolo
                spannable.setSpan(
                    android.text.style.ForegroundColorSpan(resources.getColor(R.color.rubrica, null)),
                    startIndex,
                    endIndex,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            val textView = TextView(this).apply {
                text = spannable
                textSize = 18f
                setPadding(16, 8, 16, 8)
            }

            contenedor.addView(textView)
        }
    }


    // Metodo para imprimir texto del pueblo
    private fun imprimirTextoPueblo(contenedor: LinearLayout, texto: String, modoEdicion: Boolean, tabla: String) {
        if (modoEdicion) {
            // En modo edición, usamos EditText con estilo del pueblo
            val editText = EditText(this).apply {
                setText(texto)
                textSize = 18f
                setPadding(125, 8, 16, 8)
                val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                    setTextColor(Color.WHITE)
                } else {
                    setTextColor(Color.BLACK)
                }
                typeface = Typeface.DEFAULT_BOLD
                background = null // Sin fondo para simular TextView
            }
            contenedor.addView(editText)
            textosEditados.add(editText) // Guardamos referencia si queremos recuperar contenido luego
            textosControl.add(Triple(tabla, texto, "")) // aún no sabemos el valor nuevo
        } else {
            // En modo normal, usamos TextView
            val textView = TextView(this).apply {
                text = texto
                textSize = 18f
                setPadding(125, 8, 16, 8)
                val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                    setTextColor(Color.WHITE)
                } else {
                    setTextColor(Color.BLACK)
                }
                typeface = Typeface.DEFAULT_BOLD
            }
            contenedor.addView(textView)
        }
    }

    // Metodo para imprimir texto del coro
    private fun imprimirTextoCoro(contenedor: LinearLayout, texto: String, modoEdicion: Boolean, tabla: String) {
        if (modoEdicion) {
            // En modo edición, usamos EditText con estilo del coro
            val editText = EditText(this).apply {
                setText(texto)
                textSize = 18f
                setPadding(16, 8, 16, 8)
                val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                    setTextColor(Color.WHITE)
                } else {
                    setTextColor(Color.BLACK)
                }
                typeface = Typeface.DEFAULT_BOLD
                background = null // Sin fondo para simular TextView
            }
            contenedor.addView(editText)
            textosEditados.add(editText) // Guardamos referencia si queremos recuperar contenido luego
            textosControl.add(Triple(tabla, texto, "")) // aún no sabemos el valor nuevo
        } else {
            // En modo normal, usamos TextView
            val textView = TextView(this).apply {
                text = texto
                textSize = 18f
                setPadding(16, 8, 16, 8)
                val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                    setTextColor(Color.WHITE)
                } else {
                    setTextColor(Color.BLACK)
                }
                typeface = Typeface.DEFAULT_BOLD
            }
            contenedor.addView(textView)
        }
    }


    // Metodo para cambiar a modo edición
    private fun recargarActividadModoEdit() {
        val intent = intent
        intent.putExtra("modoEdicion", true)
        finish()
        startActivity(intent)
    }

    // Metodo para cambiar a modo lectura
    private fun recargarActividadModoLect() {
        val intent = intent
        intent.putExtra("modoEdicion", false)
        finish()
        startActivity(intent)
    }

    // Metodo para mostrar comentarios y opciones para el usuario relativas
    private fun mostrarComentarios(idFiesta: String) {
        val contenedor = findViewById<LinearLayout>(R.id.contenedorTextos)

        // Le pongo el mismo color de fondo para que llame menos la atención
        contenedor.setBackgroundColor(Color.parseColor("#FFF8F0"))

        // Contenedor visual para toda la sección de comentarios
        val seccionComentarios = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 48, 24, 48)
            setBackgroundColor(Color.parseColor("#FFEFD5")) // Un color claro como papaya
        }

        // Título
        val titulo = TextView(this).apply {
            text = "Sección de Comentarios"
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.DKGRAY)
            setPadding(0, 0, 0, 8)
        }
        seccionComentarios.addView(titulo)

        // Subtítulo o ayuda
        val subtitulo = TextView(this).apply {
            text = "Aquí puedes ver o añadir notas personales relacionadas con esta fiesta."
            textSize = 14f
            setTextColor(Color.GRAY)
            setPadding(0, 0, 0, 16)
        }
        seccionComentarios.addView(subtitulo)

        val comentarios = ComentarioManager.obtenerComentarios(this, idFiesta)

        if (comentarios.isEmpty()) {
            val sinComentarios = TextView(this).apply {
                text = "No hay comentarios añadidos aún."
                textSize = 16f
                setTextColor(Color.DKGRAY)
                setPadding(0, 0, 0, 24)
            }
            seccionComentarios.addView(sinComentarios)
        } else {
            comentarios.forEachIndexed { index, comentario ->
                val fila = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(8, 4, 8, 4)
                    gravity = Gravity.CENTER_VERTICAL
                }

                val textView = TextView(this).apply {
                    text = comentario
                    textSize = 16f
                    setTextColor(Color.BLACK)
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }

                if (intent.getBooleanExtra("modoEdicion", false)) {
                    val btnEliminar = MaterialButton(this).apply {
                        text = "Eliminar"
                        setOnClickListener {
                            ComentarioManager.eliminarComentario(this@DisplayActivity, idFiesta, index)
                            recargarActividadModoEdit()
                        }
                        setBackgroundColor(Color.RED)
                        setTextColor(Color.WHITE)
                        cornerRadius = 24
                        textSize = 12f
                    }
                    fila.addView(textView)
                    fila.addView(btnEliminar)
                } else {
                    fila.addView(textView)
                }

                seccionComentarios.addView(fila)
            }
        }

        // Campo y botón para añadir comentario (solo en modo edición)
        if (intent.getBooleanExtra("modoEdicion", false)) {
            val campoComentario = EditText(this).apply {
                hint = "Escribe un nuevo comentario..."
                textSize = 16f
                setPadding(16, 16, 16, 16)
                setBackgroundColor(Color.WHITE)
                setTextColor(Color.DKGRAY)
                // **CORREGIR: EL COLOR NO SE VE BIEN**
            }

            val btnAgregar = MaterialButton(this).apply {
                text = "Añadir comentario"
                setBackgroundColor(Color.parseColor("#FFEFD5"))
                setTextColor(Color.WHITE)
                cornerRadius = 32
                setOnClickListener {
                    val texto = campoComentario.text.toString().trim()
                    if (texto.isNotEmpty()) {
                        ComentarioManager.agregarComentario(this@DisplayActivity, idFiesta, texto)
                        recargarActividadModoEdit()
                    } else {
                        Toast.makeText(context, "El comentario está vacío", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            seccionComentarios.addView(campoComentario)
            seccionComentarios.addView(btnAgregar)
        }

        // Añadir todo al contenedor principal
        contenedor.addView(seccionComentarios)
    }
}