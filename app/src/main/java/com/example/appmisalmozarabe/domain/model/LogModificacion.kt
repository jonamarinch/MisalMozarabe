package com.example.appmisalmozarabe.domain.model

// Pequeña clase para manejar los logs de las modificaciones
data class LogModificacion(
    val fecha: String,
    val usuario: String,
    val tabla: String,
    val textoOriginal: String,
    val textoNuevo: String,
    val fiesta: String
)