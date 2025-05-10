package com.example.appmisalmozarabe.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import java.io.FileOutputStream
import com.example.appmisalmozarabe.domain.model.TextoLiturgico
import java.security.MessageDigest

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

    /*
    Esta función copia la base de datos predefinida desde la carpeta `assets` a la ubicación interna del dispositivo,
    sólo si aún no existe. Esto permite usar una base de datos ya preparada en lugar de crearla desde cero.
     */
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

    /*
    Metodo para abrir la base de datos
     */
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

    /*
    Metodo para comprobar que existen las tablas de fiestas y tiempos
     */
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
     * Comprueba si la contraseña introducida es correcta comparando su hash con el almacenado.
     */
    fun verificarContrasenna(input: String): Boolean {
        val cursor = database?.rawQuery(
            "SELECT CLAVE FROM CONFIG WHERE ID = 1", null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val hashGuardado = it.getString(0).trim() // Se asegura de quitar espacios accidentales
                val hashInput = hashSha256(input)
                // 🔍 Debug de comparación
                Log.d("HashDebug", "Input: $hashInput")
                Log.d("HashDebug", "Guardado: $hashGuardado")
                return hashInput == hashGuardado
            }
        }

        return false
    }

    /*
     * Genera el hash SHA-256 de un String dado (por ejemplo, una contraseña).
     * Este valor se puede usar para comparar con el almacenado en la base de datos.
     */
    private fun hashSha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
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

    /*
    Metodo para actualizar los textos con las modificaciones del usuario
     */
    fun actualizarTextoPorContenido(tabla: String, textoOriginal: String, textoNuevo: String) {
        val sql = """
        UPDATE $tabla
        SET TEXTO = ?
        WHERE TEXTO = ?
    """.trimIndent()

        database?.execSQL(sql, arrayOf(textoNuevo, textoOriginal))
    }
}