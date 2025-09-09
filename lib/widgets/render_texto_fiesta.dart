import 'package:flutter/material.dart';
import '../utils/texto.dart';
import '../utils/control_estilos.dart';
import '../utils/tipo_texto_style.dart';
import '../utils/tipos_texto.dart';
import 'build_texto_widgets.dart'; // si buildTextoConEstilo, etc. se aíslan

Widget renderTextoFiesta({
  required BuildContext context,
  required Texto texto,
  required int index,
  required List<Texto> textos,
}) {
  final TipoTextoStyle cfg = ControlEstilos.estiloPorTipo(context, TipoTexto.values[texto.tipo - 1]);
  final text = cfg.uppercase ? texto.txt.toUpperCase() : texto.txt;
  final siguiente = index + 1 < textos.length ? textos[index + 1] : null;
  final tipoActual = texto.tipo;
  final tipoSiguiente = siguiente?.tipo;

  final casoEspecial = (tipoActual == 4 && tipoSiguiente == 5) || (tipoActual == 5 && tipoSiguiente == 4);

  if (texto.tipo == 1) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16),
      child: buildTextoConCruces(text, cfg),
    );
  } else if (texto.tipo == 3) {
    final cfg3 = cfg.copyWith(
      style: cfg.style.copyWith(color: Colors.black),
      highlightUntilPattern: RegExp(r'[.:;]'),
      highlightColor: Color(0xFFB00020),
    );
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16),
      child: buildTextoConEstilo(text, cfg3),
    );
  } else if (texto.tipo == 4 || texto.tipo == 5 || texto.tipo == 8) {
    return Padding(
      padding: casoEspecial
          ? const EdgeInsets.only(top: 25, bottom: 0, left: 16, right: 16)
          : const EdgeInsets.only(top: 25, bottom: 25, left: 16, right: 16),
      child: Text(text, style: cfg.style, textAlign: cfg.align),
    );
  } else {
    return Padding(
      padding: texto.tipo == 7
          ? const EdgeInsets.only(left: 30, right: 16)
          : const EdgeInsets.symmetric(horizontal: 16),
      child: Text(text, style: cfg.style, textAlign: cfg.align),
    );
  }
}