package com.example.appmisalmozarabe

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.appmisalmozarabe.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializamos el binding para la ActivityAbout
        val binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar el comportamiento del FAB
        binding.fab.setOnClickListener { view ->
            // Llamamos a la MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

    }
}
