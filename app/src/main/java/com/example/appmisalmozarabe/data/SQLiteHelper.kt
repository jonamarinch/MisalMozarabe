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
    Devuelve la contraseña establecida
    */
    fun getContrasenna(): String? {
        val cursor = database?.rawQuery(
            "SELECT CLAVE FROM CONFIG WHERE ID = 1", null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                return it.getString(0)
            }
        }

        return null
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
    Devuelve el nombre del tiempo al recibir el nombre de una fiesta
    */
    fun getTiempoFromFiesta(nombreFiesta: String): String? {
        val cursor = database?.rawQuery(
            """
            SELECT TIEMPOS.NOMBRE
            FROM TIEMPOS
            JOIN FIESTAS ON FIESTAS.TIEMPO = TIEMPOS.ID
            WHERE FIESTAS.NOMBRE = ?
        """.trimIndent(), arrayOf(nombreFiesta)
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val nomTiempo = it.getString(0)
                return nomTiempo
            }
        }

        return null
    }

    /*
    Devuelve el código de fiesta y de tiempo al recibir el nombre de una fiesta
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

    /*
     * Duplica un parámetro [n] veces en un Array.
     * Para consultas SQL con parámetros repetidos.
     */
    inline fun <reified T> T.repeatInSQLParams(times: Int): Array<T> = Array(times) { this }

    /*
    Devuelve los textos litúrgicos asociados a una fiesta o tiempo litúrgico en orden
    @param idFiesta ID de la fiesta litúrgica (puede ser null)
    @param idTiempo ID del tiempo litúrgico (puede ser null)
    @param from Nombre de la tabla de donde obtener los textos
    @return Lista de textos litúrgicos ordenados
    */
    fun getTextos(idFiesta: String?, idTiempo: String?, from: String): List<TextoLiturgico> {
        val lista = mutableListOf<TextoLiturgico>()

        // Validación básica
        if (from.isBlank()) return emptyList()

        // Preparamos los parámetros
        val params = mutableListOf<String>().apply {
            if (idFiesta != null) {
                add(idFiesta)
                add(idFiesta)
            }
            if (idTiempo != null) {
                add(idTiempo)
            }
        }.toTypedArray()

        // Construimos la consulta dinámicamente
        val whereClause = buildString {
            if (idFiesta != null) {
                append("(FIESTA = ? OR FIESTA IS NULL OR FIESTA2 = ?)")
                if (idTiempo != null) {
                    append(" OR ")
                }
            }
            if (idTiempo != null) {
                append("TIEMPO = ?")
            }
            if (idFiesta == null && idTiempo == null) {
                append("1") // Devuelve todos si no hay filtros
            }
        }

        val query = """
        SELECT TIPO, TEXTO
        FROM $from
        WHERE $whereClause
        ORDER BY ORDEN
    """.trimIndent()

        val cursor = database?.rawQuery(query, params)

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
