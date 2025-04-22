package com.example.appmisalmozarabe.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import java.io.FileOutputStream
import com.example.appmisalmozarabe.domain.model.TextoLiturgico

class SQLiteHelper(private val context: Context) {

    companion object {
        private const val DB_NAME = "misal.db"
    }

    private var database: SQLiteDatabase? = null

    init {
        try {
            copyDatabaseIfNeeded()
            openDatabase()
            verificarTablas()
        } catch (e: Exception) {
            Log.e("SQLiteHelper", "Error durante la inicialización: ${e.message}")
        }
    }

    private fun copyDatabaseIfNeeded() {
        val dbPath = context.getDatabasePath(DB_NAME)
        if (!dbPath.exists()) {
            dbPath.parentFile?.mkdirs()
            try {
                context.assets.open(DB_NAME).use { inputStream ->
                    FileOutputStream(dbPath).use { outputStream ->
                        val buffer = ByteArray(1024)
                        var length: Int
                        while (inputStream.read(buffer).also { length = it } > 0) {
                            outputStream.write(buffer, 0, length)
                        }
                        outputStream.flush()
                    }
                }
                Log.d("SQLiteHelper", "Base de datos copiada desde assets.")
                Log.d("SQLiteHelper", "Ruta final del archivo: ${dbPath.path}")
                Log.d("SQLiteHelper", "Tamaño archivo: ${dbPath.length()} bytes")
            } catch (e: Exception) {
                Log.e("SQLiteHelper", "Error al copiar la base de datos: ${e.message}")
                throw e
            }
        } else {
            Log.d("SQLiteHelper", "Base de datos ya existe, no se copia.")
        }
    }

    private fun openDatabase() {
        val dbPath = context.getDatabasePath(DB_NAME)
        try {
            database = SQLiteDatabase.openDatabase(dbPath.path, null, SQLiteDatabase.OPEN_READWRITE)
            Log.d("SQLiteHelper", "Base de datos abierta correctamente.")
        } catch (e: Exception) {
            Log.e("SQLiteHelper", "Error al abrir la base de datos: ${e.message}")
            throw e
        }
    }

    private fun verificarTablas() {
        val cursor = database?.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null)
        cursor?.use {
            val tablas = mutableListOf<String>()
            while (it.moveToNext()) {
                tablas.add(it.getString(0))
            }
            Log.d("SQLiteHelper", "Tablas encontradas en DB: $tablas")

            if (!tablas.contains("TIEMPOS") || !tablas.contains("FIESTAS")) {
                throw IllegalStateException("Faltan tablas requeridas: TIEMPOS o FIESTAS")
            }
        }
    }

    /*
    Devuelve todos los tiempos litúrgicos
    */
    fun getAllTiempos(): List<String> {
        val list = mutableListOf<String>()
        val cursor = database?.rawQuery("SELECT NOMBRE FROM TIEMPOS ORDER BY ORDEN", null)
        cursor?.use {
            while (it.moveToNext()) {
                list.add(it.getString(0))
            }
        }
        return list
    }

    /*
    Devuelve todas las fiestas
     */
    fun getAllFiestas(): List<String> {
        val list = mutableListOf<String>()
        val cursor = database?.rawQuery("SELECT NOMBRE FROM FIESTAS ORDER BY ID", null)
        cursor?.use {
            while (it.moveToNext()) {
                list.add(it.getString(0))
            }
        }
        return list
    }

    /*
    Devuelve las fiestas asociadas a un tiempo litúrgico dado por su nombre
     */
    fun getFiestasPorTiempo(nombreTiempo: String): List<String> {
        val list = mutableListOf<String>()
        val cursor = database?.rawQuery(
            """
                SELECT FIESTAS.NOMBRE
                FROM FIESTAS
                JOIN TIEMPOS ON FIESTAS.TIEMPO = TIEMPOS.ID
                WHERE TIEMPOS.NOMBRE = ?
                ORDER BY FIESTAS.ID
            """.trimIndent(), arrayOf(nombreTiempo)
        )

        cursor?.use {
            while (it.moveToNext()) {
                list.add(it.getString(0))
            }
        }

        return list
    }

    fun close() {
        database?.close()
    }

    /*
    Devuelve el código de fiesta y de tiempo al recibir el nombre de una fiesta seleccionada
    */
    fun getCodigoFiestaYTiempo(nombreFiesta: String): Pair<String, String>? {
        val cursor = database?.rawQuery(
            """
            SELECT FIESTAS.ID, FIESTAS.TIEMPO
            FROM FIESTAS
            WHERE FIESTAS.NOMBRE = ?
        """.trimIndent(), arrayOf(nombreFiesta)
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val idFiesta = it.getString(0)
                val idTiempo = it.getString(1)
                return Pair(idFiesta, idTiempo)
            }
        }

        return null
    }

    fun getPraelegendum(idFiesta: String?, idTiempo: String?): List<TextoLiturgico> {
        val lista = mutableListOf<TextoLiturgico>()
        val cursor = database?.rawQuery(
            """
        SELECT TIPO, TEXTO
        FROM PRAELEGENDUM
        WHERE FIESTA = ? OR FIESTA IS NULL OR TIEMPO = ?
        ORDER BY ORDEN
        """.trimIndent(), arrayOf(idFiesta, idTiempo)
        )

        cursor?.use {
            while (it.moveToNext()) {
                val tipo = it.getInt(0)
                val texto = it.getString(1)
                lista.add(TextoLiturgico(tipo, texto))
            }
        }

        return lista
    }
}
