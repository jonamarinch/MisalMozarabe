import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:missale_mozarabicum/services/app_text_styles.dart';
import 'package:missale_mozarabicum/services/tipos_texto.dart';
import 'package:missale_mozarabicum/services/tipo_texto_style.dart';

/// Construye los estilos de texto de la app adaptándose al tema actual
AppTextStyles buildAppTextStyles(TextTheme base, {bool isDarkMode = false}) {
  TextStyle f(TextStyle s) => GoogleFonts.cardo(textStyle: s);

  // Colores que se adaptan al tema
  final primaryRed = isDarkMode ? const Color(0xFFFF6B6B) : const Color(0xFFB00020);
  final textColor = isDarkMode ? Colors.white : Colors.black87;

  return AppTextStyles({
    TipoTexto.normal: TipoTextoStyle(
      style: f(base.bodyLarge ?? const TextStyle(fontSize: 17)).copyWith(
        color: textColor,
      ),
    ),
    TipoTexto.rubrica: TipoTextoStyle(
      style: f(base.bodyLarge ?? const TextStyle(fontSize: 17)).copyWith(
        color: primaryRed,
        fontStyle: FontStyle.italic,
      ),
    ),
    TipoTexto.semirrubrica: TipoTextoStyle(
      style: f(base.bodyLarge ?? const TextStyle(fontSize: 17)).copyWith(
        color: primaryRed,
      ),
    ),
    TipoTexto.rubricaCentrada: TipoTextoStyle(
      style: f(base.bodyLarge ?? const TextStyle(fontSize: 16)).copyWith(
        color: primaryRed,
        fontWeight: FontWeight.w700,
      ),
      align: TextAlign.center,
    ),
    TipoTexto.rubricaCentradaNegritaMayusculas: TipoTextoStyle(
      style: f(base.titleMedium ?? const TextStyle(fontSize: 18)).copyWith(
        color: primaryRed,
        fontWeight: FontWeight.w700,
        letterSpacing: 1.0,
      ),
      align: TextAlign.center,
      uppercase: true,
    ),
    TipoTexto.pueblo: TipoTextoStyle(
      style: f(base.bodyLarge ?? const TextStyle(fontSize: 15)).copyWith(
        fontWeight: FontWeight.w700,
        color: textColor,
      ),
    ),
    TipoTexto.coro: TipoTextoStyle(
      style: f(base.bodyLarge ?? const TextStyle(fontSize: 15)).copyWith(
        fontWeight: FontWeight.w700,
        color: textColor,
      ),
    ),
    TipoTexto.titulo: TipoTextoStyle(
      style: f(base.headlineSmall ?? const TextStyle(fontSize: 22)).copyWith(
        fontWeight: FontWeight.w700,
        letterSpacing: 1.2,
        color: textColor,
      ),
      align: TextAlign.center,
    ),
  });
}

/// Versión simplificada que detecta automáticamente el modo
AppTextStyles buildAppTextStylesAuto(BuildContext context) {
  final brightness = Theme.of(context).brightness;
  final textTheme = Theme.of(context).textTheme;
  return buildAppTextStyles(textTheme, isDarkMode: brightness == Brightness.dark);
}