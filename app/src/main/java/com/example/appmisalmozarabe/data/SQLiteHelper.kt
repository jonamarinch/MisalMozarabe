package com.example.appmisalmozarabe.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import java.io.FileOutputStream
import com.example.appmisalmozarabe.domain.model.TextoLiturgico
import java.security.MessageDigest
import java.util.Base64

// Clase de utilidad para manejar la base de datos SQLite del Misal Mozárabe
class SQLiteHelper(private val context: Context) {

    companion object {
        // Nombre del archivo de la base de datos incluida en assets
        private const val DB_NAME = "misal.db"
    }

    private var database: SQLiteDatabase? = null

    // Inicializador: copia la base de datos si es necesario y la abre
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
     * Copia la base de datos desde 'assets' al almacenamiento interno si aún no existe.
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
            } catch (e: Exception) {
                Log.e("SQLiteHelper", "Error al copiar la base de datos: ${e.message}")
                throw e
            }
        } else {
            Log.d("SQLiteHelper", "Base de datos ya existe, no se copia.")
        }
    }

    /*
     * Abre la base de datos en modo lectura-escritura.
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
     * Verifica que existan las tablas básicas necesarias: TIEMPOS y FIESTAS.
     */
    private fun verificarTablas() {
        val cursor = database?.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null)
        cursor?.use {
            val tablas = mutableListOf<String>()
            while (it.moveToNext()) {
                tablas.add(it.getString(0))
            }
            if (!tablas.contains("TIEMPOS") || !tablas.contains("FIESTAS")) {
                throw IllegalStateException("Faltan tablas requeridas: TIEMPOS o FIESTAS")
            }
        }
    }

    /*
     * Verificar claves: si el usuario existe y está configurado, y si la contraseña es válida
     */
    fun autenticarUsuario(nombre: String, clave: String): Boolean {
        val cursor = database?.rawQuery("SELECT hash_clave, salt FROM config WHERE nombre = ?", arrayOf(nombre))

        cursor?.use {
            if (it.moveToFirst()) {
                val hashGuardado = it.getString(0)
                val salt = it.getString(1)
                val hashInput = hashClave(clave, salt)
                return hashInput == hashGuardado
            }
        }

        return false
    }

    /*
     * Devuelve el hash SHA-256 de un texto (usado para proteger contraseñas).
     */
    private fun hashSha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /*
     * Devuelve el hash SHA-256 de una clave + salt
     */
    private fun hashClave(clave: String, salt: String): String {
        return hashSha256(salt + clave)
    }

    /*
     * Devuelve todos los nombres de los tiempos litúrgicos en orden.
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
     * Devuelve los nombres de las fiestas correspondientes a un tiempo litúrgico.
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

    /*
     * Cierra la conexión con la base de datos.
     */
    fun close() {
        database?.close()
    }

    /*
     * Dado el nombre de una fiesta, devuelve el nombre del tiempo litúrgico al que pertenece.
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
                return it.getString(0)
            }
        }

        return null
    }

    /*
     * Devuelve el par (ID de fiesta, ID de tiempo) correspondiente a un nombre de fiesta.
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
                return Pair(it.getString(0), it.getString(1))
            }
        }

        return null
    }

    /*
    * Devuelve el listado de idiomas disponibles.
    */
    fun getIdiomas(): List<String> {
        val list = mutableListOf<String>()
        val cursor = database?.rawQuery("SELECT NOMBRE FROM IDIOMAS ORDER BY ORDEN", null)
        cursor?.use {
            while (it.moveToNext()) {
                list.add(it.getString(0))
            }
        }
        return list
    }

    /*
     * Método de utilidad para generar un array con un valor repetido varias veces.
     * Útil para consultas SQL con múltiples parámetros iguales.
     */
    inline fun <reified T> T.repeatInSQLParams(times: Int): Array<T> = Array(times) { this }

    /*
     * Recupera los textos litúrgicos filtrados por fiesta o tiempo, desde una tabla concreta.
     * Si no se pasa ningún filtro, devuelve todos los textos de la tabla.
     */
    fun getTextos(idFiesta: String?, idTiempo: String?, from: String): List<TextoLiturgico> {
        if (from.isBlank() || !tableExists(from)) return emptyList() // Verifico si la tabla existe
        val lista = mutableListOf<TextoLiturgico>()

        if (from.isBlank()) return emptyList()

        val params = mutableListOf<String>().apply {
            if (idFiesta != null) {
                add(idFiesta)
                add(idFiesta)
                add(idFiesta)
                add(idFiesta)
            }
            if (idTiempo != null) {
                add(idTiempo)
            }
        }.toTypedArray()

        val whereClause = buildString {
            if (idFiesta != null) {
                append("(FIESTA = ? OR FIESTA IS NULL OR FIESTA2 = ? OR FIESTA3 = ? OR FIESTA4 = ?)")
                if (idTiempo != null) append(" OR ")
            }
            if (idTiempo != null) {
                append("TIEMPO = ?")
            }
            if (idFiesta == null && idTiempo == null) {
                append("1") // No hay filtros: devuelve todos
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
    Verificar si la tabla existe
     */
    fun tableExists(tableName: String): Boolean {
        val cursor = database?.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='table' AND name='$tableName'", null
        )
        val exists = cursor?.moveToFirst() ?: false
        cursor?.close()
        return exists
    }

    /*
     * Actualiza un texto específico dentro de una tabla, cambiando el contenido original por uno nuevo.
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