import 'package:flutter/material.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'dart:convert';
import 'package:missale_mozarabicum/models/fiesta.dart';
import 'package:missale_mozarabicum/models/texto.dart';
import 'package:missale_mozarabicum/widgets/render_texto_fiesta.dart';
import 'package:missale_mozarabicum/widgets/settings_botton.dart';
import 'package:missale_mozarabicum/providers/settings_selectors.dart';

/// Pantalla que muestra los textos de una fiesta
class FiestaScreen extends ConsumerStatefulWidget {
  final Fiesta fiesta;

  const FiestaScreen({Key? key, required this.fiesta}) : super(key: key);

  @override
  ConsumerState<FiestaScreen> createState() => _FiestaScreenState();
}

/// Estado de la pantalla que gestiona carga y caché
class _FiestaScreenState extends ConsumerState<FiestaScreen> {
  List<Texto> _textos = [];
  bool _isLoading = true;

  // static const Duration cacheDuration = Duration(days: 100); // Tiempo válido
  static const Duration cacheDuration = Duration(seconds: 100); // Tiempo válido

  @override
  void initState() {
    super.initState();
    _loadTextos();
  }

  // Carga textos desde caché o Firestore
  Future<void> _loadTextos() async {
    print('[TAG] Entrando en _loadTextos');
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
        print('[TAG] Cargando desde caché');
        final jsonList = jsonDecode(prefs.getString(cacheKey)!);
        _textos = List<Texto>.from(jsonList.map((e) => Texto.fromFirestore(e)));
        setState(() {
          _isLoading = false;
        });
        return;
      }
    }

    // Si no hay caché válido, descarga desde Firestore
    final fiestaId = widget.fiesta.codigo;
    final tiempoId = widget.fiesta.tiempo;

    print('[TAG] Consultando Firestore: fiestaId=$fiestaId, tiempoId=$tiempoId');

    // Firestore queries
    final textosSnap = await FirebaseFirestore.instance
        .collection('textos')
        .where('asoc', arrayContainsAny: [fiestaId, tiempoId])
        .orderBy('orden')
        .get();

    print('[TAG] textosSnap.docs.length: ${textosSnap.docs.length}');

    // Eliminar duplicados (asumiendo que la combinación de 'orden' y 'txt' es única)
    final uniqueDocs = {
      for (var doc in textosSnap.docs) doc.id: doc,
    }.values.toList();

    print('[TAG] uniqueDocs.length: ${uniqueDocs.length}');

    _textos = uniqueDocs
        .map((doc) => Texto.fromFirestore(doc.data()))
        .toList();

    print('[TAG] _textos.length: ${_textos.length}');

    // Añadir el título al principio si lo deseas
    Texto titulo = Texto.titulo(widget.fiesta.nombre.toUpperCase(), widget.fiesta.codigo);
    _textos.insert(0, titulo);

    // Guardamos los textos en caché
    final jsonTextos = jsonEncode(_textos.map((t) => t.toJson()).toList());
    await prefs.setString(cacheKey, jsonTextos);
    // Guardamos la fecha/hora de guardado
    await prefs.setString(timestampKey, DateTime.now().toIso8601String());

    print('[TAG] Hecho - guardado en caché');

    setState(() {
      _isLoading = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    final textScale = ref.watch(currentTextScaleProvider); // Escala de texto

    return MediaQuery(
      // Aplicar escala de texto
      data: MediaQuery.of(context).copyWith(
        textScaler: TextScaler.linear(textScale),
      ),
      child: Scaffold(
        appBar: AppBar(
          // Botón de configuración en el AppBar
          actions: const [
            SettingsAppBarButton(),
          ],
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

            return renderTextoFiesta(
              context: context,
              texto: _textos[index],
              index: index,
              textos: _textos,
            );
          },
        ),
      ),
    );
  }
}