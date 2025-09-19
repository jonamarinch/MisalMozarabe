import 'package:flutter/cupertino.dart';

/// Clase utilitaria para generar y consultar fechas litúrgicas
class LiturgicalCalendar {
  /// Calcula la fecha del Domingo de Pascua para un año dado
  static DateTime _easterDate(int year) {
    final a = year % 19;
    final b = year ~/ 100;
    final c = year % 100;
    final d = b ~/ 4;
    final e = b % 4;
    final f = (b + 8) ~/ 25;
    final g = (b - f + 1) ~/ 3;
    final h = (19 * a + b - d - g + 15) % 30;
    final i = c ~/ 4;
    final k = c % 4;
    final l = (32 + 2 * e + 2 * i - h - k) % 7;
    final m = (a + 11 * h + 22 * l) ~/ 451;
    final month = (h + l - 7 * m + 114) ~/ 31;
    final day = ((h + l - 7 * m + 114) % 31) + 1;
    return DateTime(year, month, day);
  }

  /// Genera un mapa con fechas clave y códigos de fiestas
  static Map<DateTime, String> generarCalendario(int year) {
    debugPrint('🔵 [LiturgicalCalendar] Generando calendario para el año $year');
    final Map<DateTime, String> calendario = {};

    // Fechas base
    final pascua = _easterDate(year);
    final cuaresmaInicio = pascua.subtract(const Duration(days: 42));
    final pentecostes = pascua.add(const Duration(days: 50));
    final navidad = DateTime(year, 12, 25);
    final adv1 = _primerDomingoAdviento(year, navidad);
    final epifania = DateTime(year, 1, 6);

    // Agregar domingos de Adviento
    calendario[adv1] = 'adv1';
    for (int i = 1; i < 6; i++) {
      calendario[adv1.add(Duration(days: i * 7))] = 'adv${i + 1}';
    }

    // Agregar domingos de Cuaresma
    calendario[cuaresmaInicio] = 'cua1';
    for (int i = 1; i < 5; i++) {
      calendario[cuaresmaInicio.add(Duration(days: i * 7))] = 'cua${i + 1}';
    }

    // Agregar domingos de Pascua
    calendario[pascua] = 'pas1';
    for (int i = 1; i < 7; i++) {
      calendario[pascua.add(Duration(days: i * 7))] = 'cua${i + 1}';
    }

    // Agregar Pentecostés y domingos después de Pentecostés
    calendario[pentecostes] = 'pen1';
    for (int i = 0; i < 7; i++) {
      calendario[pentecostes.add(Duration(days: i * 7))] = 'dpe${i + 1}';
    }

    // Agregar Epifanía
    calendario[epifania] = 'nav4';
    // Encontrar el primer domingo después del 6 de enero
    DateTime primerDomingoPostEpifania = epifania;
    while (primerDomingoPostEpifania.weekday != DateTime.sunday) {
      primerDomingoPostEpifania = primerDomingoPostEpifania.add(Duration(days: 1));
    }
    // Agregar domingos después de Epifanía hasta el primer domingo de Cuaresma
    calendario[primerDomingoPostEpifania] = 'dep1';
    for (int i = 1; i < 9; i++) {
      calendario[primerDomingoPostEpifania.add(Duration(days: i * 7))] = 'dep${i + 1}';
    }

    // Otras fechas clave de Semana Santa
    final domingoRamos = pascua.subtract(Duration(days: 7));
    calendario[domingoRamos] = 'ses1';
    final lunesSanto = domingoRamos.add(Duration(days: 1));
    calendario[lunesSanto] = 'ses2';
    final martesSanto = domingoRamos.add(Duration(days: 2));
    calendario[martesSanto] = 'ses3';
    final miercolesSanto = domingoRamos.add(Duration(days: 3));
    calendario[miercolesSanto] = 'ses4';
    final juevesSanto = domingoRamos.add(Duration(days: 4));
    calendario[juevesSanto] = 'ses5';
    final viernesSanto = domingoRamos.add(Duration(days: 5));
    calendario[viernesSanto] = 'ses6';
    final vigiliaPascual = domingoRamos.add(Duration(days: 6));
    calendario[vigiliaPascual] = 'ses7';

    // Otras fechas clave de Navidad
    calendario[navidad] = 'nav1';
    final circ = DateTime(year, 1, 1);
    calendario[circ] = 'nav2';
    final initAnn = DateTime(year, 1, 2);
    calendario[initAnn] = 'nav2';

    // Debug
    debugPrint('🟣 [LiturgicalCalendar] Fechas generadas:');
    calendario.forEach((fecha, codigo) {
      debugPrint('    📆 ${fecha.toIso8601String()} → $codigo');
    });

    return calendario;
  }

  /// Devuelve el nombre de la fiesta más próxima igual o posterior a la fecha
  static String getFiestaDesde(DateTime fecha) {
    debugPrint('🟠 [LiturgicalCalendar] Buscando fiesta desde: ${fecha.toIso8601String()}');

    final calendario = generarCalendario(fecha.year);
    final f = DateTime(fecha.year, fecha.month, fecha.day);
    final ordenadas = calendario.keys.toList()..sort();

    for (final d in ordenadas) {
      final normalizado = DateTime(d.year, d.month, d.day);
      if (!normalizado.isBefore(f)) {
        final cod = calendario[d]!;
        debugPrint('✅ [LiturgicalCalendar] Fiesta encontrada: $cod en $normalizado');
        return cod;
      }
    }

    debugPrint('❌ [LiturgicalCalendar] No se encontró ninguna fiesta desde esa fecha');
    return 'No encontrada';
  }

  /// Calcula el primer domingo de Adviento (6 domingos antes de Navidad)
  static DateTime _primerDomingoAdviento(int year, navidad) {
    final domingoNavidad = navidad.subtract(Duration(days: navidad.weekday % 7));
    return domingoNavidad.subtract(const Duration(days: 35));
  }
}