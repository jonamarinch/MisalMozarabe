package com.example.appmisalmozarabe.presentation

import android.Manifest
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
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.text.LineBreaker
import android.os.Build
import android.os.Environment
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.appmisalmozarabe.R
import com.google.android.material.button.MaterialButton
import com.example.appmisalmozarabe.data.SQLiteHelper
import com.example.appmisalmozarabe.data.ComentarioManager
import com.example.appmisalmozarabe.domain.model.TextoLiturgico
import java.io.File

class DisplayActivity : AppCompatActivity() {
    // Lista para recoger todos los campos EditText mostrados en modo lectura y en modo edicion
    val textosControl = mutableListOf<Triple<String, String, String>>() // (tabla, original, nuevo)
    // Lista para recoger todos los campos EditText mostrados en modo edición
    val textosEditados= mutableListOf<EditText>()
    // Índice con las secciones
    private val indiceSecciones = mutableMapOf<String, View>()
    // Fuente Times New Roman
    private lateinit var tf: Typeface

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

        // Inicializar fuente
        tf = ResourcesCompat.getFont(this, R.font.times_new_roman)!!

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

        // Controlar si se muestra el botón flotante
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        btnLogout.visibility = if (enModoEdicion) View.VISIBLE else View.GONE

        // Crear y añadir TextView para el título de la fiesta
        val tituloTextView = TextView(this).apply {
            text = nombreFiesta
            textSize = 18f
            setTypeface(tf, Typeface.BOLD)
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
            setTypeface(tf, Typeface.BOLD)
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
        if (textosCargados.isNotEmpty()) imprimirTextosLiturgicos(contenedor, textosCargados, enModoEdicion, "ORDINARIUM")
        // Cargar textos Accedendi
        textosCargados = dbHelper.getTextos(seleccion?.first, seleccion?.second, "ACCEDENDI")
        if (textosCargados.isNotEmpty()) imprimirTextosLiturgicos(contenedor, textosCargados, enModoEdicion, "ACCEDENDI")
        // Cargar textos Confessio
        textosCargados = dbHelper.getTextos(seleccion?.first, seleccion?.second, "CONFESSIO")
        if (textosCargados.isNotEmpty()) imprimirTextosLiturgicos(contenedor, textosCargados, enModoEdicion, "CONFESSIO")
        // Cargar textos Absolutio
        textosCargados = dbHelper.getTextos(seleccion?.first, seleccion?.second, "ABSOLUTIO")
        if (textosCargados.isNotEmpty()) imprimirTextosLiturgicos(contenedor, textosCargados, enModoEdicion, "ABSOLUTIO")
        // Cargar textos AdAltare
        textosCargados = dbHelper.getTextos(seleccion?.first, seleccion?.second, "ADALTARE")
        if (textosCargados.isNotEmpty()) imprimirTextosLiturgicos(contenedor, textosCargados, enModoEdicion, "ADALTARE")
        // Cargar textos AnteCrucem
        textosCargados = dbHelper.getTextos(seleccion?.first, seleccion?.second, "ANTECRUCEM")
        if (textosCargados.isNotEmpty()) imprimirTextosLiturgicos(contenedor, textosCargados, enModoEdicion, "ANTECRUCEM")
        // Cargar textos ExtensionisCorporalis
        textosCargados = dbHelper.getTextos(seleccion?.first, seleccion?.second, "EXTENSIONISCORPORALIS")
        if (textosCargados.isNotEmpty()) imprimirTextosLiturgicos(contenedor, textosCargados, enModoEdicion, "EXTENSIONISCORPORALIS")
        // Cargar textos Mixtionis
        textosCargados = dbHelper.getTextos(seleccion?.first, seleccion?.second, "MIXTIONIS")
        if (textosCargados.isNotEmpty()) imprimirTextosLiturgicos(contenedor, textosCargados, enModoEdicion, "MIXTIONIS")

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

        // Botón para seleccionar lengua
        val btnLengua = findViewById<MaterialButton>(R.id.btnLengua)
        btnLengua.setOnClickListener {
            val dbHelper = SQLiteHelper(this)
            val idiomas = dbHelper.getIdiomas().toTypedArray()

            // Mostrar diálogo con selección de idioma
            AlertDialog.Builder(this)
                .setTitle("Seleccionar idioma")
                .setItems(idiomas) { _, which ->
                    val idiomaSeleccionado = idiomas[which]
                    Toast.makeText(this, "Idioma seleccionado: $idiomaSeleccionado", Toast.LENGTH_SHORT).show()
                    // Aquí puedes guardar el idioma en SharedPreferences si lo vas a usar en toda la app
                    getSharedPreferences("prefs", MODE_PRIVATE).edit()
                        .putString("idiomaSeleccionado", idiomaSeleccionado).apply()
                }
                .setNegativeButton("Cancelar", null)
                .show()
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

        // Diálogo para cambiar contraseña, importante para el botón de editar
        fun mostrarDialogoCambiarContra(usuario: String) {
            val inputClave1 = EditText(this).apply {
                hint = "Nueva contraseña"
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            val inputClave2 = EditText(this).apply {
                hint = "Repite la contraseña"
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }

            val layout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(48, 32, 48, 0)
                addView(inputClave1)
                addView(inputClave2)
            }

            AlertDialog.Builder(this)
                .setTitle("Crear nueva clave")
                .setMessage("Introduce la nueva contraseña dos veces.")
                .setView(layout)
                .setPositiveButton("Cambiar Contraseña") { _, _ ->
                    val pass1 = inputClave1.text.toString()
                    val pass2 = inputClave2.text.toString()

                    if (pass1 == pass2) {
                        // Verificar requisitos de contraseña
                        if (!esContrasenaSegura(pass1)) {
                            Toast.makeText(this, "Contraseña insegura. Debe tener al menos 8 caracteres, mayúscula, minúscula, número y símbolo.", Toast.LENGTH_LONG).show()
                            return@setPositiveButton
                        }
                        val dbHelper = SQLiteHelper(this)
                        val exito = dbHelper.cambiarContra(usuario, pass1)
                        if (exito) {
                            // ✅ Exportar base de datos modificada como respaldo interno
                            exportDatabaseConPermiso()
                        } else {
                            Toast.makeText(this, "Error: el usuario ya existe", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        // Listener para el botón para editar
        btnEdit.setOnClickListener {
            val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
            if (prefs.getBoolean("adminAutenticado", false)) {
                // Ya autenticado, pasar a modo edición directamente
                recargarActividadModoEdit()
                return@setOnClickListener
            } else {
                // Crear campo de texto para el nombre de usuario
                val inputUsuario = EditText(this).apply {
                    hint = "Usuario"
                    inputType = InputType.TYPE_CLASS_TEXT
                    setPadding(32, 24, 32, 12)
                }
                // Crear campo de texto para la contraseña
                val inputContra = EditText(this).apply {
                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    hint = "Contraseña"
                    setPadding(32, 24, 32, 24) // Espaciado interno
                }

                // Contenedor para aplicar márgenes al input
                val container = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(48, 32, 48, 0) // Padding exterior del contenedor
                    addView(inputUsuario)
                    addView(inputContra)
                }

                // Variable para manejar el usuario en caso de querer cambiar la contraseña
                var nomUsuario = ""
                // Mostrar diálogo
                val dialog = AlertDialog.Builder(this)
                    .setTitle("Acceso restringido")
                    .setMessage("Introduce usuario y contraseña de administrador para continuar.")
                    .setView(container)
                    .setPositiveButton("Aceptar") { _, _ ->
                        val enteredUser = inputUsuario.text.toString()
                        nomUsuario = enteredUser
                        val enteredPassword = inputContra.text.toString()
                        if (dbHelper.autenticarUsuario(enteredUser, enteredPassword)) {
                            Toast.makeText(this, "Acceso concedido", Toast.LENGTH_SHORT).show()
                            getSharedPreferences("prefs", MODE_PRIVATE).edit()
                                .putBoolean("adminAutenticado", true).apply()
                            recargarActividadModoEdit()
                        } else {
                            Toast.makeText(this, "Claves incorrectas", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNeutralButton("Cambiar Contraseña") { _, _ ->
                        val enteredUser = inputUsuario.text.toString()
                        nomUsuario = enteredUser
                        val enteredPassword = inputContra.text.toString()
                        if (dbHelper.autenticarUsuario(enteredUser, enteredPassword)) {
                            Toast.makeText(this, "Acceso concedido", Toast.LENGTH_SHORT).show()
                            getSharedPreferences("prefs", MODE_PRIVATE).edit()
                                .putBoolean("adminAutenticado", true).apply()
                            mostrarDialogoCambiarContra(nomUsuario)
                        } else {
                            Toast.makeText(this, "Claves incorrectas", Toast.LENGTH_SHORT).show()
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
            val usuario = prefs.getString("usuarioAdmin", "desconocido") ?: "desconocido"

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
                    // LLAMADA A LA FUNCIÓN REGISTRAR LOG
                    registrarLog(this, usuario, tabla, textoOriginal, textoNuevo)
                }

                // Actualizar la lista de control (opcional, por si acaso)
                textosControl[i] = Triple(tabla, textoOriginal, textoNuevo)
            }
            // ✅ Exportar base de datos modificada como respaldo interno
            exportDatabaseConPermiso()
            // Recargar en modo lectura
            recargarActividadModoLect()
        }

        // Listener del botón para el logout
        btnLogout.setOnClickListener {
            prefs.edit().remove("adminAutenticado").remove("usuarioAdmin").apply()
            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
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
                setTypeface(tf, Typeface.NORMAL)
                justificationMode = LineBreaker.JUSTIFICATION_MODE_INTER_WORD
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
                setTypeface(tf, Typeface.NORMAL)
                justificationMode = LineBreaker.JUSTIFICATION_MODE_INTER_WORD
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

                // Controlar modo claro/oscuro
                val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                var colorApropiado = resources.getColor(R.color.rubrica, null)
                if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                    colorApropiado = resources.getColor(R.color.rubrica_d, null)
                }

                // Aplicar color rubrica al símbolo
                spannable.setSpan(
                    android.text.style.ForegroundColorSpan(colorApropiado),
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
                setTypeface(tf, Typeface.ITALIC)
                justificationMode = LineBreaker.JUSTIFICATION_MODE_INTER_WORD

                // Controlar modo claro/oscuro para el color de la rúbrica
                val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                setTextColor(resources.getColor(R.color.rubrica, null))
                if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                    setTextColor(resources.getColor(R.color.rubrica_d, null))
                }
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
                setTypeface(tf, Typeface.ITALIC)
                justificationMode = LineBreaker.JUSTIFICATION_MODE_INTER_WORD
                // Controlar modo claro/oscuro para el color de la rúbrica
                val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                setTextColor(resources.getColor(R.color.rubrica, null))
                if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                    setTextColor(resources.getColor(R.color.rubrica_d, null))
                }
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
                setTypeface(tf, Typeface.ITALIC)
                justificationMode = LineBreaker.JUSTIFICATION_MODE_INTER_WORD
                // Controlar modo claro/oscuro para el color de la rúbrica
                val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                setTextColor(resources.getColor(R.color.rubrica, null))
                if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                    setTextColor(resources.getColor(R.color.rubrica_d, null))
                }
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
                setTypeface(tf, Typeface.ITALIC)
                justificationMode = LineBreaker.JUSTIFICATION_MODE_INTER_WORD
                // Controlar modo claro/oscuro para el color de la rúbrica
                val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                setTextColor(resources.getColor(R.color.rubrica, null))
                if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                    setTextColor(resources.getColor(R.color.rubrica_d, null))
                }
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
                setTypeface(tf, Typeface.BOLD)
                justificationMode = LineBreaker.JUSTIFICATION_MODE_INTER_WORD
                // Controlar modo claro/oscuro para el color de la rúbrica
                val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                setTextColor(resources.getColor(R.color.rubrica, null))
                if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                    setTextColor(resources.getColor(R.color.rubrica_d, null))
                }
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
                setTypeface(tf, Typeface.BOLD)
                justificationMode = LineBreaker.JUSTIFICATION_MODE_INTER_WORD
                // Controlar modo claro/oscuro para el color de la rúbrica
                val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                setTextColor(resources.getColor(R.color.rubrica, null))
                if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                    setTextColor(resources.getColor(R.color.rubrica_d, null))
                }
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
                setTypeface(tf, Typeface.NORMAL)
                justificationMode = LineBreaker.JUSTIFICATION_MODE_INTER_WORD
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

            // Controlar modo claro/oscuro
            val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            var colorApropiado = resources.getColor(R.color.rubrica, null)
            if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                colorApropiado = resources.getColor(R.color.rubrica_d, null)
            }

            // Aplicar color rubrica al símbolo
            spannable.setSpan(
                android.text.style.ForegroundColorSpan(colorApropiado),
                0,
                rubricaHasta,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            // El resto del texto (opcional: puedes aplicar estilo "normal" explícitamente si quieres)

            // Necesario para controlar el color del texto según el modo
            var colorTexto = Color.BLACK;
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

                // Controlar modo claro/oscuro
                val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                var colorApropiado = resources.getColor(R.color.rubrica, null)
                if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                    colorApropiado = resources.getColor(R.color.rubrica_d, null)
                }

                // Aplicar color rubrica al símbolo
                spannable.setSpan(
                    android.text.style.ForegroundColorSpan(colorApropiado),
                    startIndex,
                    endIndex,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            val textView = TextView(this).apply {
                text = spannable
                textSize = 18f
                setTypeface(tf, Typeface.NORMAL)
                justificationMode = LineBreaker.JUSTIFICATION_MODE_INTER_WORD
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
                setTypeface(tf, Typeface.BOLD)
                justificationMode = LineBreaker.JUSTIFICATION_MODE_INTER_WORD
                setPadding(125, 8, 16, 8)
                val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                    setTextColor(Color.WHITE)
                } else {
                    setTextColor(Color.BLACK)
                }
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
                setTypeface(tf, Typeface.BOLD)
                justificationMode = LineBreaker.JUSTIFICATION_MODE_INTER_WORD
                setPadding(125, 8, 16, 8)
                val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                    setTextColor(Color.WHITE)
                } else {
                    setTextColor(Color.BLACK)
                }
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
                setTypeface(tf, Typeface.BOLD)
                justificationMode = LineBreaker.JUSTIFICATION_MODE_INTER_WORD
                setPadding(16, 8, 16, 8)
                val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                    setTextColor(Color.WHITE)
                } else {
                    setTextColor(Color.BLACK)
                }
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
                setTypeface(tf, Typeface.BOLD)
                justificationMode = LineBreaker.JUSTIFICATION_MODE_INTER_WORD
                setPadding(16, 8, 16, 8)
                val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                    setTextColor(Color.WHITE)
                } else {
                    setTextColor(Color.BLACK)
                }
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
        // contenedor.setBackgroundColor(Color.parseColor("#FFF8F0"))

        // Contenedor visual para toda la sección de comentarios
        val seccionComentarios = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 48, 24, 48)
            // setBackgroundColor(Color.parseColor("#FFEFD5")) // Un color claro como papaya
        }

        // Título
        val titulo = TextView(this).apply {
            text = "Sección de Comentarios"
            textSize = 20f
            setTypeface(tf, Typeface.BOLD)
            justificationMode = LineBreaker.JUSTIFICATION_MODE_INTER_WORD
            // setTextColor(Color.DKGRAY)
            setPadding(0, 0, 0, 8)
        }
        seccionComentarios.addView(titulo)

        // Subtítulo o ayuda
        val subtitulo = TextView(this).apply {
            text = "Aquí puedes ver o añadir notas personales relacionadas con esta fiesta."
            textSize = 14f
            // setTextColor(Color.GRAY)
            setPadding(0, 0, 0, 16)
        }
        seccionComentarios.addView(subtitulo)

        val comentarios = ComentarioManager.obtenerComentarios(this, idFiesta)

        if (comentarios.isEmpty()) {
            val sinComentarios = TextView(this).apply {
                text = "No hay comentarios añadidos aún."
                textSize = 16f
                // setTextColor(Color.GRAY)
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
                    // setTextColor(Color.BLACK)
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
                setBackgroundColor(Color.argb(100, 200, 200, 200))
                setTextColor(Color.DKGRAY)
                // **CORREGIR: EL COLOR NO SE VE BIEN**
            }

            val btnAgregar = MaterialButton(this).apply {
                text = "Añadir comentario"
                setBackgroundColor(Color.parseColor("#7e9e80"))
                setTextColor(Color.BLACK)
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

    // Para comprobar que la contraseña es segura
    fun esContrasenaSegura(contra: String): Boolean {
        val longitudValida = contra.length >= 8
        val tieneMayuscula = contra.any { it.isUpperCase() }
        val tieneMinuscula = contra.any { it.isLowerCase() }
        val tieneNumero = contra.any { it.isDigit() }
        val tieneEspecial = contra.any { "!@#\$%^&*()_+-=[]{}/<>?".contains(it) }

        return longitudValida && tieneMayuscula && tieneMinuscula && tieneNumero && tieneEspecial
    }


    // Para la exportación de los datos guardados
    private fun exportDatabaseConPermiso() {
        val permiso = Manifest.permission.WRITE_EXTERNAL_STORAGE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val concedido = ContextCompat.checkSelfPermission(this, permiso) == PackageManager.PERMISSION_GRANTED
            if (!concedido) {
                ActivityCompat.requestPermissions(this, arrayOf(permiso), 1234)
                return
            }
        }

        val ok = exportDatabase(this)
        val msg = if (ok) "Cambios guardados y copia realizada" else "Cambios guardados, pero error al hacer copia"
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    // Para la exportación de los datos guardados
    fun exportDatabase(context: Context): Boolean {
        return try {
            val sourceFile = context.getDatabasePath("misal.db")
            val exportDir = File(Environment.getExternalStorageDirectory(), "backup-dir")
            if (!exportDir.exists()) exportDir.mkdirs()

            val destFile = File(exportDir, "misal_backup.db")
            sourceFile.copyTo(destFile, overwrite = true)

            Log.d("EXPORT", "BD exportada a: ${destFile.absolutePath}")
            true
        } catch (e: Exception) {
            Log.e("EXPORT", "Error al exportar: ${e.message}")
            false
        }
    }

    // Metodo para manejar la respuesta al permiso (exportar BD)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1234) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                exportDatabaseConPermiso()
            } else {
                Toast.makeText(this, "Permiso denegado: no se podrá guardar la copia", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Metodo para registrar log y por tanto las modificaciones sobre la BD
    private fun registrarLog(context: Context, usuario: String, tabla: String, original: String, nuevo: String) {
        val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date())

        val entradaLog = "[$timestamp] Usuario: $usuario | Tabla: $tabla | Original: \"${original.take(100)}\" | Nuevo: \"${nuevo.take(100)}\"\n"

        try {
            val archivo = java.io.File(context.filesDir, "cambios_log.txt")
            archivo.appendText(entradaLog)
        } catch (e: Exception) {
            android.util.Log.e("LOG", "Error al guardar log: ${e.message}")
        }
    }
}