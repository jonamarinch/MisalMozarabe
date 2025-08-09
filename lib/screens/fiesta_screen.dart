import 'package:flutter/material.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'dart:convert';

import '../utils/fiesta.dart';
import '../utils/texto.dart';
import '../utils/control_estilos.dart';
import '../utils/tipo_texto_style.dart';
import '../utils/tipos_texto.dart';

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

  // static const Duration cacheDuration = Duration(days: 100); // Tiempo válido
  static const Duration cacheDuration = Duration(seconds: 100); // Tiempo válido

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
    final tiempoId = widget.fiesta.tiempo;

    _textos = snapshot.docs
        .map((doc) => Texto.fromFirestore(doc.data() as Map<String, dynamic>))
        .where((texto) =>
          (texto.fiestas.contains(fiestaId)) || // O está asociado a la misma fiesta o al mismo tiempo
          (texto.tiempos.contains(tiempoId))
        )
        .toList();

    Texto titulo = Texto.titulo(widget.fiesta.nombre.toUpperCase(), widget.fiesta.codigo);
    _textos.insert(0, titulo);

    // Guardamos los textos en caché
    final jsonTextos = jsonEncode(_textos.map((t) => t.toJson()).toList());
    await prefs.setString(cacheKey, jsonTextos);
    // Guardamos la fecha/hora de guardado
    await prefs.setString(timestampKey, DateTime.now().toIso8601String());

    print('Hecho - guardado en caché');

    setState(() {
      _isLoading = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
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

          // Resolver estilo por tipo (tu función debería devolver TextStyle, no ThemeData)
          final TipoTextoStyle cfg = ControlEstilos.estiloPorTipo(context, TipoTexto.values[texto.tipo-1]);
          var text = cfg.uppercase ? texto.txt.toUpperCase() : texto.txt;

          if (texto.tipo == 1) {
            return ListTile(
              title: buildTextoNormalConCruces(text, cfg),
            );
          }
          else if (texto.tipo == 3) {
            final cfg3 = cfg.copyWith(
              // Asegura un color base explícito para el tramo "negro"
              style: cfg.style.copyWith(color: Colors.black),
              // Resalta hasta el primer punto o punto y coma (ajusta a tu necesidad)
              highlightUntilPattern: RegExp(r'[.;]'),
              // Color del tramo inicial
              highlightColor: Colors.red,
            );

            return ListTile(
              title: buildTextoConEstilo(text, cfg3),
            );
          }
          else {
            return ListTile(
              title: Padding(
                padding: texto.tipo == 7
                    ? const EdgeInsets.only(left: 30) // sangría de todo el bloque
                    : EdgeInsets.zero,
                child: Text(text, style: cfg.style, textAlign: cfg.align),
              ),
            );
          }
        },
      ),
    );
  }
}

Widget buildTextoConEstilo(String raw, TipoTextoStyle cfg) {
  final text = cfg.uppercase ? raw.toUpperCase() : raw;

  // Si no hay patrón, devolvemos un Text normal (sin spans)
  if (cfg.highlightUntilPattern == null || cfg.highlightColor == null) {
    return Text(text, style: cfg.style, textAlign: cfg.align);
  }

  final m = cfg.highlightUntilPattern!.firstMatch(text);
  if (m == null) {
    // nada que resaltar
    return Text(text, style: cfg.style, textAlign: cfg.align);
  }

  final cut = m.end; // incluye el símbolo (. o ;)
  final before = text.substring(0, cut);
  final after  = text.substring(cut);

  final baseColor = cfg.style.color; // color para el resto

  return Text.rich(
    TextSpan(children: [
      TextSpan(
        text: before,
        style: cfg.style.copyWith(color: cfg.highlightColor),
      ),
      if (after.isNotEmpty)
        TextSpan(
          text: after,
          style: cfg.style.copyWith(color: baseColor),
        ),
    ]),
    textAlign: cfg.align,
  );
}

Widget buildTextoNormalConCruces(String raw, TipoTextoStyle cfg) {
  final text = cfg.uppercase ? raw.toUpperCase() : raw;

  final base = cfg.style; // tu estilo normal
  final rojo = base.copyWith(color: Colors.red);

  return Text.rich(
    TextSpan(children: _resaltarOcurrencias(
      text: text,
      pattern: RegExp('✠'), // U+2720
      base: base,
      highlight: rojo,
    )),
    textAlign: cfg.align,
  );
}

List<TextSpan> _resaltarOcurrencias({
  required String text,
  required RegExp pattern,
  required TextStyle base,
  required TextStyle highlight,
}) {
  final spans = <TextSpan>[];
  int start = 0;
  for (final m in pattern.allMatches(text)) {
    if (m.start > start) {
      spans.add(TextSpan(text: text.substring(start, m.start), style: base));
    }
    spans.add(TextSpan(text: m.group(0), style: highlight));
    start = m.end;
  }
  if (start < text.length) {
    spans.add(TextSpan(text: text.substring(start), style: base));
  }
  return spans;
}