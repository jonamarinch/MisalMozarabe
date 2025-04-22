package com.example.appmisalmozarabe.domain.model

// Pequeña clase para manejar los textos litúrgicos y su tipo
data class TextoLiturgico(
    val tipo: Int,     // Ej: 0 = texto normal, 1 = rubrica, etc.
    val contenido: String
)