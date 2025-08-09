import 'package:flutter/material.dart';
import '../utils/tipo_texto_style.dart';
import '../utils/tipos_texto.dart';
import 'app_text_styles.dart';

class ControlEstilos {
  static TipoTextoStyle estiloPorTipo(BuildContext context, TipoTexto tipo) {
    final ext = Theme.of(context).extension<AppTextStyles>();
    if (ext == null) {
      throw Exception('AppTextStyles no está en el Theme');
    }
    return ext.byTipo(tipo);
  }
}


/*
/// Clase utilitaria para controlar los estilos
class ControlEstilos {
  /// Devuelve un ThemeData configurado según el tipo de texto
  static ThemeData obtenerTheme(int tipo) {
    // Tema base que heredará las propiedades no especificadas
    final baseTheme = ThemeData.light();

    switch (tipo) {
      // NORMAL:
      case 1:
        return obtenerThemeNormal();
      // RÚBRICA:
      case 2:
        return obtenerThemeRubrica();
      // SEMIRRÚBRICA:
      case 3:
        return obtenerThemeSemirrubrica();
      // RÚBRICA CENTRADA:
      case 4:
        return obtenerThemeRubricaCentr();
      // RÚBRICA CENTRADA, EN NEGRITA Y EN MAYÚSCULAS:
      case 5:
        return obtenerThemeSuperrubrica();
      // PUEBLO:
      case 6:
        return obtenerThemePueblo();
      // CORO:
      case 7:
        return obtenerThemeCoro();
      // TÍTULO:
      case 8:
        return obtenerThemeCoro();
      // Por defecto:
      default:
        return obtenerThemeNormal();
    }
  }

  // Devolver tipo 1 NORMAL
  static ThemeData obtenerThemeNormal() {
    return ThemeData.light().copyWith(
      listTileTheme: ListTileThemeData(
        titleTextStyle: const TextStyle(
          fontSize: 18,
          color: Color(0xFF000000),
          fontWeight: FontWeight.normal,
        ),
      ),
    );
  }
  // Devolver tipo 2 RÚBRICA
  static ThemeData obtenerThemeRubrica() {
    return ThemeData.light().copyWith(
      listTileTheme: ListTileThemeData(
        titleTextStyle: const TextStyle(
          fontSize: 18,
          color: Color(0xFFc00000),
          fontStyle: FontStyle.italic,
        ),
      ),
    );
  }
  // Devolver tipo 3 SEMIRRÚBRICA
  static ThemeData obtenerThemeSemirrubrica() {
    return ThemeData.light().copyWith(
      listTileTheme: ListTileThemeData(
        titleTextStyle: const TextStyle(
          fontSize: 18,
          color: Color(0xFFc00000),
          fontStyle: FontStyle.italic,
        ),
      ),
    );
  }
  // Devolver tipo 4 RÚBRICA CENTRADA
  static ThemeData obtenerThemeRubricaCentr() {
    return ThemeData.light().copyWith(
      listTileTheme: ListTileThemeData(
        titleTextStyle: const TextStyle(
          fontSize: 20,
          fontWeight: FontWeight.bold,
        ),
        // Puedes añadir más propiedades aquí
        tileColor: Colors.blue[50],
      ),
    );
  }
  // Devolver tipo 5 RÚBRICA CENTRADA, EN NEGRITA Y EN MAYÚSCULAS
  static ThemeData obtenerThemeSuperrubrica() {
    return ThemeData.light().copyWith(
      listTileTheme: ListTileThemeData(
        titleTextStyle: const TextStyle(
          fontSize: 20,
          color: Color(0xFFc00000),
          backgroundColor: null,
          fontWeight: FontWeight.bold,
        ),
      ),
    );
  }
  // Devolver tipo 6 PUEBLO
  static ThemeData obtenerThemePueblo() {
    return ThemeData.light().copyWith(
      listTileTheme: ListTileThemeData(
        titleTextStyle: const TextStyle(
          fontSize: 20,
          fontWeight: FontWeight.bold,
        ),
        // Puedes añadir más propiedades aquí
        tileColor: Colors.blue[50],
      ),
    );
  }
  // Devolver tipo 7 CORO
  static ThemeData obtenerThemeCoro() {
    return ThemeData.light().copyWith(
      listTileTheme: ListTileThemeData(
        titleTextStyle: const TextStyle(
          fontSize: 20,
          fontWeight: FontWeight.bold,
        ),
        // Puedes añadir más propiedades aquí
        tileColor: Colors.blue[50],
      ),
    );
  }
  // Devolver tipo 8 TÍTULO
  static ThemeData obtenerThemeTitulo() {
    return ThemeData.light().copyWith(
      listTileTheme: ListTileThemeData(
        titleTextStyle: const TextStyle(
          fontFamily: 'Cardo',
          fontSize: 22,
          color: Color(0xFF000000),
          backgroundColor: null,
          fontWeight: FontWeight.w700,
          letterSpacing: 1.2,
        ),
      ),
    );
  }
}
*/