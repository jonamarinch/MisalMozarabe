package com.example.appmisalmozarabe.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import java.io.File
import java.io.FileOutputStream

class SQLiteHelper(private val context: Context) {

    companion object {
        private const val DB_NAME = "misal.db"
    }

    private var database: SQLiteDatabase? = null

    init {
        copyDatabaseIfNeeded()
        openDatabase()
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
            } catch (e: Exception) {
                Log.e("SQLiteHelper", "Error al copiar la base de datos: ${e.message}")
            }
        } else {
            Log.d("SQLiteHelper", "La base de datos ya existe, no se copia.")
        }
    }

    private fun openDatabase() {
        val dbPath = context.getDatabasePath(DB_NAME)
        database = SQLiteDatabase.openDatabase(dbPath.path, null, SQLiteDatabase.OPEN_READWRITE)
    }

    fun getAllFiestas(): List<String> {
        val list = mutableListOf<String>()
        val cursor = database?.rawQuery("SELECT NOMBRE FROM FIESTAS ORDER BY ID", null)
        cursor?.use {
            if (it.moveToFirst()) {
                do {
                    list.add(it.getString(0))
                } while (it.moveToNext())
            }
        }
        return list
    }

    fun close() {
        database?.close()
    }
}
