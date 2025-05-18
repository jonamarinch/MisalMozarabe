package com.example.appmisalmozarabe.presentation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.appmisalmozarabe.R
import com.example.appmisalmozarabe.data.SQLiteHelper
import com.example.appmisalmozarabe.databinding.ActivityMainBinding
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Manejo de preferencias guardadas
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        val nightModeSaved = prefs.getBoolean("modoOscuro", false)
        AppCompatDelegate.setDefaultNightMode(
            if (nightModeSaved) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar la Toolbar manualmente si no está en el binding
        setSupportActionBar(binding.root.findViewById(R.id.toolbar))

        // Obtener parámetros del Intent
        val tiempoParam = intent.getStringExtra("tiempo")
        val fiestaParam = intent.getStringExtra("fiesta")

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
        // Spinner para seleccionar idiomas
        val spinnerIdiomas = findViewById<Spinner>(R.id.spinnerIdiomas)
        // Clase para recoger datos
        val dbHelper = SQLiteHelper(this)


        // Cargar tiempos litúrgicos
        val tiempos = dbHelper.getAllTiempos()
        val tiemposAdapter = ArrayAdapter(this, R.layout.spinner_item, tiempos)
        tiemposAdapter.setDropDownViewResource(R.layout.spinner_item)
        spinnerTiempos.adapter = tiemposAdapter

        // Establecer selección inicial para tiempo si viene en parámetros
        tiempoParam?.let { tiempo ->
            val posicion = tiempos.indexOfFirst { it == tiempo }
            if (posicion >= 0) {
                spinnerTiempos.setSelection(posicion)
            }
        }

        // Variable para almacenar la última fiesta seleccionada
        var fiestaSeleccionada: String? = fiestaParam // Inicializar con el parámetro si se vuelve del Display

        // Listener para el primer spinner (tiempos litúrgicos)
        spinnerTiempos.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val tiempoSeleccionado = parent.getItemAtPosition(position).toString()
                // Al seleccionar un tiempo, se actualizan las opciones para las fiestas:
                val fiestas = dbHelper.getFiestasPorTiempo(tiempoSeleccionado)
                val fiestasAdapter = ArrayAdapter(this@MainActivity, R.layout.spinner_item, fiestas)
                fiestasAdapter.setDropDownViewResource(R.layout.spinner_item)
                spinnerFiestas.adapter = fiestasAdapter

                // Establecer selección inicial para fiesta después de cargar el adapter
                fiestaSeleccionada?.let { fiesta ->
                    val posicion = fiestas.indexOfFirst { it == fiesta }
                    if (posicion >= 0) {
                        spinnerFiestas.setSelection(posicion)
                    }
                }
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

        // Cargar idiomas desde la tabla LINGUAE
        val idiomas = dbHelper.getIdiomas()
        val idiomasAdapter = ArrayAdapter(this, R.layout.spinner_item, idiomas)
        idiomasAdapter.setDropDownViewResource(R.layout.spinner_item)
        spinnerIdiomas.adapter = idiomasAdapter

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
