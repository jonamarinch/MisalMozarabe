import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'app_text_styles.dart';
import 'tipos_texto.dart';
import 'tipo_texto_style.dart';

AppTextStyles buildAppTextStyles(TextTheme base) {
  TextStyle f(TextStyle s) => GoogleFonts.cardo(textStyle: s);

  return AppTextStyles({
    TipoTexto.normal: TipoTextoStyle(
      style: f(base.bodyLarge ?? const TextStyle(fontSize: 16)),
    ),
    TipoTexto.rubrica: TipoTextoStyle(
      style: f(base.bodyMedium ?? const TextStyle(fontSize: 15)).copyWith(
        color: const Color(0xFFB00020),
        fontStyle: FontStyle.italic,
      ),
    ),
    TipoTexto.semirrubrica: TipoTextoStyle(
      style: f(base.bodyMedium ?? const TextStyle(fontSize: 15)).copyWith(
        color: const Color(0xFFB00020),
      ),
    ),
    TipoTexto.rubricaCentrada: TipoTextoStyle(
      style: f(base.bodyLarge ?? const TextStyle(fontSize: 16)).copyWith(
        color: const Color(0xFFB00020),
        fontStyle: FontStyle.italic,
      ),
      align: TextAlign.center,
    ),
    TipoTexto.rubricaCentradaNegritaMayusculas: TipoTextoStyle(
      style: f(base.titleMedium ?? const TextStyle(fontSize: 18)).copyWith(
        color: const Color(0xFFB00020),
        fontWeight: FontWeight.w700,
        letterSpacing: 1.0,
      ),
      align: TextAlign.center,
      uppercase: true,
    ),
    TipoTexto.pueblo: TipoTextoStyle(
      style: f(base.bodyLarge ?? const TextStyle(fontSize: 16)).copyWith(
        fontWeight: FontWeight.w700,
      ),
    ),
    TipoTexto.coro: TipoTextoStyle(
      style: f(base.bodyLarge ?? const TextStyle(fontSize: 16)).copyWith(
        fontWeight: FontWeight.w700,
      ),
      // No existe algo tipo "textIndent" en TextStyle, así que tendrías que agregarlo manualmente en el builder:
      // text = '     $text'; // agrega espacios al inicio
    ),
    TipoTexto.titulo: TipoTextoStyle(
      style: f(base.headlineSmall ?? const TextStyle(fontSize: 22)).copyWith(
        fontWeight: FontWeight.w700,
        letterSpacing: 1.2,
      ),
      align: TextAlign.center,
    ),
  });
}