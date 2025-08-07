import 'package:flutter/material.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'dart:convert';

import '../utils/fiesta.dart'; // Ajusta según tu estructura
import '../utils/texto.dart'; // Asegúrate de tener el modelo

/// Pantalla que muestra los textos de una fiesta
class FiestaScreen extends StatefulWidget {
  final Fiesta fiesta;

  const FiestaScreen({Key? key, required this.fiesta}) : super(key: key);

  @override
  State<FiestaScreen> createState() => _FiestaScreenState();
}

/// Estado de la pantalla que gestiona carga y caché
class _FiestaScreenState extends State<FiestaScreen> {
  List<Texto> _textos = [];
  bool _isLoading = true;

  static const Duration cacheDuration = Duration(days: 30); // Tiempo válido

  @override
  void initState() {
    super.initState();
    _loadTextos();
  }

  // Carga textos desde caché o Firestore
  Future<void> _loadTextos() async {
    final prefs = await SharedPreferences.getInstance();
    final cacheKey = 'textos_${widget.fiesta.codigo}';
    final timestampKey = '${cacheKey}_timestamp';

    // Tiempo actual para comparar
    final now = DateTime.now();

    // Verifica si hay caché y si está caducado
    if (prefs.containsKey(cacheKey) && prefs.containsKey(timestampKey)) {
      final lastSaved = DateTime.tryParse(prefs.getString(timestampKey) ?? '');
      final isValid = lastSaved != null && now.difference(lastSaved) <= cacheDuration;

      if (isValid) {
        final jsonList = jsonDecode(prefs.getString(cacheKey)!);
        _textos = List<Texto>.from(jsonList.map((e) => Texto.fromFirestore(e)));
        setState(() {
          _isLoading = false;
        });
        return;
      }
    }

    // Si no hay caché válido, descarga desde Firestore
    final snapshot = await FirebaseFirestore.instance
        .collection('textos')
        .orderBy('orden')
        .get();

    final fiestaId = widget.fiesta.codigo;

    _textos = snapshot.docs
        .map((doc) => Texto.fromFirestore(doc.data() as Map<String, dynamic>))
        .where((texto) => texto.fiestas.contains(fiestaId))
        .toList();

    // Guardamos los textos en caché
    final jsonTextos = jsonEncode(_textos.map((t) => t.toJson()).toList());
    await prefs.setString(cacheKey, jsonTextos);

    setState(() {
      _isLoading = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.fiesta.nombre),
      ),
      body: _isLoading
      // Indicador de carga
          ? const Center(child: CircularProgressIndicator())
      // Texto si no hay resultados
          : _textos.isEmpty
          ? const Center(child: Text('No hay textos para esta fiesta.'))
      // Lista de textos
          : ListView.builder(
        itemCount: _textos.length,
        itemBuilder: (context, index) {
          final texto = _textos[index];
          return ListTile(
            title: Text(texto.txtEs),
            subtitle: Text('Tipo: ${texto.tipo}'),
          );
        },
      ),
    );
  }
}