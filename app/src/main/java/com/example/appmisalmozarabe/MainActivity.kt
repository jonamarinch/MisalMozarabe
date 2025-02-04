package com.example.appmisalmozarabe

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.appmisalmozarabe.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar la Toolbar manualmente si no está en el binding
        setSupportActionBar(binding.root.findViewById(R.id.toolbar))

        // Configurar botón flotante
        binding.fab.setOnClickListener { view ->
            // Llamamos a la nueva actividad AboutActivity
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }

        val spinner = findViewById<Spinner>(R.id.spinnerOptions)
        val selectedOption = findViewById<TextView>(R.id.selectedOption)


        // Cargar opciones desde resources
        val adapter = ArrayAdapter.createFromResource(
            this, R.array.spinner_options, android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter


        // Manejar selección del usuario
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                val option = parent.getItemAtPosition(position).toString()
                selectedOption.text = "Opción seleccionada: $option"
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedOption.text = "Selecciona una opción"
            }
        }
    }

    // Método que se llama cuando se crea el menú de opciones
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)


        // Set default language to 'Español' when the menu is created
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val selectedLanguage = preferences.getString("selected_language", "español")

        // Set the default selected item
        if (selectedLanguage == "español") {
            menu.findItem(R.id.action_lang1).setChecked(true)
        } else if (selectedLanguage == "language2") {
            menu.findItem(R.id.action_lang2).setChecked(true)
        } else if (selectedLanguage == "language3") {
            menu.findItem(R.id.action_lang3).setChecked(true)
        }

        return true
    }

    // Método que maneja la selección de un item del menú
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = preferences.edit()

        when (item.itemId) {
            R.id.action_lang1 ->             // Set 'Español' as the selected language
                editor.putString("selected_language", "español")

            R.id.action_lang2 ->             // Set another language as selected
                editor.putString("selected_language", "language2")

            R.id.action_lang3 ->             // Set another language as selected
                editor.putString("selected_language", "language3")

            else -> return super.onOptionsItemSelected(item)
        }
        editor.apply()

        // You can also change the UI language here based on selection if needed
        return true
    }
}
