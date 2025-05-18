package com.example.appmisalmozarabe.util

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/*
 * Objeto para manejar registros de modificaciones o logs
 */
object Logger {

    private const val FILE_NAME = "cambios_log.txt"

    fun logCambio(context: Context, usuario: String, tabla: String, original: String, nuevo: String) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val logEntry = "[$timestamp] Usuario: $usuario | Tabla: $tabla | Original: \"${original.take(100)}\" | Nuevo: \"${nuevo.take(100)}\"\n"

        try {
            val file = File(context.filesDir, FILE_NAME)
            file.appendText(logEntry)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}