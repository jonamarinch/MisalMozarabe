// en calendar_locale_fallback.dart
import 'dart:ui' as ui;
import 'package:flutter/widgets.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:missale_mozarabicum/providers/settings_selectors.dart';

// Devuelve un locale string seguro para TableCalendar
String _safeInitialCalendarLocale() {
  final ui.Locale sys = WidgetsBinding.instance.platformDispatcher.locales.isNotEmpty
      ? WidgetsBinding.instance.platformDispatcher.locales.first
      : WidgetsBinding.instance.platformDispatcher.locale;

  final lang = sys.languageCode.toLowerCase();
  final country = (sys.countryCode?.isEmpty ?? true) ? null : sys.countryCode;

  // si el sistema está en latín, usa 'en_US' como inicial para el calendario
  if (lang == 'la') return 'en_US';

  // Si no es latín, construye lang_COUNTRY si hay, si no lang
  return country != null ? '${lang}_$country' : lang;
}

// Valor inicial basado en el sistema (mejor que 'es_ES' fijo)
final previousCalendarLocaleProvider =
StateProvider<String>((_) => _safeInitialCalendarLocale());

final calendarLocaleForTableCalendarProvider = Provider<String>((ref) {
  final current = ref.watch(localeProvider);
  final remembered = ref.watch(previousCalendarLocaleProvider);
  return current.languageCode == 'la' ? remembered : current.toString();
});

void rememberLastValidCalendarLocale(WidgetRef ref) {
  ref.listen<Locale>(localeProvider, (prev, next) {
    if (next.languageCode != 'la') {
      ref.read(previousCalendarLocaleProvider.notifier).state = next.toString();
    }
  });
}