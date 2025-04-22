package com.example.appmisalmozarabe.presentation

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appmisalmozarabe.R
import com.example.appmisalmozarabe.data.SQLiteHelper
import com.example.appmisalmozarabe.databinding.ActivityMainBinding
import com.google.android.material.button.MaterialButton

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

        // Variable para almacenar la última fiesta seleccionada
        var fiestaSeleccionada: String? = null

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
                fiestaSeleccionada = parent.getItemAtPosition(position).toString()
                binding.selectedOption.text = "Fiesta: $fiestaSeleccionada"
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Botón principal
        val botonContinuar = findViewById<MaterialButton>(R.id.btnContinue)

        // Listener para el botón principal
        botonContinuar.setOnClickListener {
            if (fiestaSeleccionada != null) {
                val intent = Intent(this, DisplayActivity::class.java)
                intent.putExtra("nombreFiesta", fiestaSeleccionada)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Por favor, selecciona una fiesta", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
