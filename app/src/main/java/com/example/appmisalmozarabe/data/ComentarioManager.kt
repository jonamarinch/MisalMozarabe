package com.example.appmisalmozarabe.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

// Objeto singleton para gestionar los comentarios asociados a cada fiesta litúrgica.
// Utiliza un archivo JSON interno para persistencia de datos.
object ComentarioManager {

    // Nombre del archivo donde se guardan los comentarios
    private const val FILE_NAME = "comentarios.json"

    // Devuelve el archivo de comentarios ubicado en el directorio interno de la app
    private fun getArchivo(context: Context): File {
        return File(context.filesDir, FILE_NAME)
    }

    // Carga el contenido del archivo JSON y lo devuelve como un objeto JSONObject
    // Si el archivo no existe, lo inicializa como un objeto vacío "{}"
    private fun cargarJSON(context: Context): JSONObject {
        val archivo = getArchivo(context)
        if (!archivo.exists()) {
            // Si no existe, lo crea con contenido vacío
            archivo.writeText("{}")
        }
        return JSONObject(archivo.readText(Charsets.UTF_8))
    }

    // Guarda un objeto JSON en el archivo correspondiente
    private fun guardarJSON(context: Context, json: JSONObject) {
        val archivo = getArchivo(context)
        archivo.writeText(json.toString(), Charsets.UTF_8)
    }

    // Devuelve la lista de comentarios asociados a una fiesta concreta
    // Si no hay comentarios, devuelve una lista vacía
    fun obtenerComentarios(context: Context, idFiesta: String): List<String> {
        val json = cargarJSON(context)
        val array = json.optJSONArray(idFiesta) ?: return emptyList() // Si no hay array, devuelve lista vacía
        return List(array.length()) { i -> array.getString(i) }
    }

    // Añade un nuevo comentario al array correspondiente al id de la fiesta
    fun agregarComentario(context: Context, idFiesta: String, comentario: String) {
        val json = cargarJSON(context)
        // Recupera o crea el array de comentarios
        val array = json.optJSONArray(idFiesta) ?: JSONArray()
        array.put(comentario) // Añade el nuevo comentario
        json.put(idFiesta, array) // Actualiza el objeto JSON
        guardarJSON(context, json) // Guarda el archivo actualizado
    }

    // Elimina un comentario de una posición específica dentro del array de comentarios de la fiesta
    fun eliminarComentario(context: Context, idFiesta: String, indice: Int) {
        val json = cargarJSON(context)
        val array = json.optJSONArray(idFiesta) ?: return // Si no hay array, no hace nada

        // Verifica que el índice esté dentro de los límites
        if (indice in 0 until array.length()) {
            val nuevaArray = JSONArray()
            // Copia todos los comentarios excepto el que se desea eliminar
            for (i in 0 until array.length()) {
                if (i != indice) {
                    nuevaArray.put(array.getString(i))
                }
            }
            json.put(idFiesta, nuevaArray) // Reemplaza el array en el objeto JSON
            guardarJSON(context, json) // Guarda los cambios
        }
    }
}