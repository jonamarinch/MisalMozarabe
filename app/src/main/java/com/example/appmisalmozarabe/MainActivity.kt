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
import com.example.appmisalmozarabe.data.SQLiteHelper
import com.example.appmisalmozarabe.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar la Toolbar manualmente si no está en el binding
        setSupportActionBar(binding.root.findViewById(R.id.toolbar))

        /* Configurar botón flotante
        binding.fab.setOnClickListener { view ->
            // Llamamos a la nueva actividad AboutActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }*/

        // Spinner para seleccionar tiempo litúrgico
        val spinnerTiempos = findViewById<Spinner>(R.id.spinnerTiempos)
        // Spinner para seleccionar fiesta litúrgica
        val spinnerFiestas = findViewById<Spinner>(R.id.spinnerFiestas)
        // Clase para recoger datos
        val dbHelper = SQLiteHelper(this)


        // Cargar tiempos litúrgicos
        val tiempos = dbHelper.getAllTiempos()
        val tiemposAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tiempos)
        tiemposAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTiempos.adapter = tiemposAdapter


        // Listener para el primer spinner (tiempos litúrgicos)
        spinnerTiempos.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val tiempoSeleccionado = parent.getItemAtPosition(position).toString()
                // Al seleccionar un tiempo, se actualizan las opciones para las fiestas:
                val fiestas = dbHelper.getFiestasPorTiempo(tiempoSeleccionado)
                val fiestasAdapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_item, fiestas)
                fiestasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerFiestas.adapter = fiestasAdapter
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Opción vacía
            }
        }

        // Listener para el segundo spinner (fiestas litúrgicas)
        spinnerFiestas.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val fiestaSeleccionada = parent.getItemAtPosition(position).toString()
                binding.selectedOption.text = "Fiesta: $fiestaSeleccionada"
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
}
