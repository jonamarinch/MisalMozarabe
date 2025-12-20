import 'package:flutter/material.dart';
import 'package:missale_mozarabicum/services/liturgical_calendar.dart';
import 'package:missale_mozarabicum/models/fiesta.dart' as fiesta;
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:table_calendar/table_calendar.dart';
import 'package:missale_mozarabicum/screens/fiesta_screen.dart';
import 'package:missale_mozarabicum/widgets/settings_botton.dart';
import 'package:missale_mozarabicum/providers/settings_selectors.dart';
import 'package:missale_mozarabicum/providers/calendar_locale_fallback.dart';

// Provider para seleccionar fecha
final selectedDateProvider = StateProvider<DateTime>((ref) => DateTime.now());
// Provider para la fiesta
final fiestaProvider = FutureProvider.autoDispose<fiesta.Fiesta?>((ref) async {
  final date = ref.watch(selectedDateProvider);
  final codigo = LiturgicalCalendar.getFiestaDesde(date);
  if (codigo.isEmpty) return null;
  return fiesta.getFiesta(codigo);
});

/// Pantalla principal donde se elige una fecha y se muestra la fiesta correspondiente
class HomeScreen extends ConsumerWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final selectedDate = ref.watch(selectedDateProvider);
    final fiestaAsync = ref.watch(fiestaProvider);
    final currentLocale = ref.watch(localeProvider);

    // Detectar tema actual
    final brightness = Theme.of(context).brightness;
    final isDark = brightness == Brightness.dark;

    // Usar el provider existente para manejar el locale del calendario
    final calendarLocale = ref.watch(calendarLocaleForTableCalendarProvider);

    // Configurar el listener para recordar el último locale válido
    rememberLastValidCalendarLocale(ref);

    // Colores explícitos basados en el tema
    final crossColor = isDark ? const Color(0xFFFF6B6B) : const Color(0xFFc00000);
    final textColor = isDark ? Colors.white : Colors.black87;
    final weekdayColor = isDark ? Colors.white : Colors.black87;
    final weekendColor = isDark ? Colors.white70 : Colors.black54;
    final selectedBorderColor = isDark ? const Color(0xFFB8AAA1) : Colors.blue;
    final selectedTextColor = isDark ? const Color(0xFFB8AAA1) : Colors.blue;
    final buttonColor = const Color(0xFFb8aaa1);

    return Scaffold(
      appBar: AppBar(
        title: const Text(
          "Missale Mozarabicum",
          style: TextStyle(
            fontFamily: 'Cardo',
            fontSize: 22,
            fontWeight: FontWeight.w700,
            letterSpacing: 1.2,
          ),
        ),
        actions: const [
          SettingsAppBarButton(),
        ],
      ),
      body: Padding(
        padding: const EdgeInsets.all(24.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            // Separador visual grande con cruz
            Padding(
              padding: const EdgeInsets.symmetric(vertical: 16.0),
              child: Row(
                children: [
                  Expanded(
                    child: Divider(
                      thickness: 2,
                      color: crossColor,
                      endIndent: 8,
                    ),
                  ),
                  Text(
                    '✠',
                    style: TextStyle(
                      fontSize: 32,
                      color: crossColor,
                    ),
                  ),
                  Expanded(
                    child: Divider(
                      thickness: 2,
                      color: crossColor,
                      indent: 8,
                    ),
                  ),
                ],
              ),
            ),

            // Calendario
            TableCalendar(
              locale: calendarLocale,
              firstDay: DateTime(2000),
              lastDay: DateTime(2100),
              focusedDay: selectedDate,
              selectedDayPredicate: (day) => isSameDay(day, selectedDate),
              onDaySelected: (day, focusedDay) {
                ref.read(selectedDateProvider.notifier).state = day;
              },
              calendarFormat: CalendarFormat.month,
              availableCalendarFormats: const {
                CalendarFormat.month: '',
              },
              sixWeekMonthsEnforced: true,
              headerVisible: true,
              headerStyle: HeaderStyle(
                titleCentered: true,
                formatButtonVisible: false,
                titleTextStyle: TextStyle(
                  fontSize: 17,
                  fontWeight: FontWeight.w600,
                  color: textColor,
                ),
                leftChevronIcon: Icon(
                  Icons.chevron_left,
                  color: textColor,
                ),
                rightChevronIcon: Icon(
                  Icons.chevron_right,
                  color: textColor,
                ),
              ),
              daysOfWeekStyle: DaysOfWeekStyle(
                weekdayStyle: TextStyle(
                  color: weekdayColor,
                  fontWeight: FontWeight.w600,
                ),
                weekendStyle: TextStyle(
                  color: weekendColor,
                  fontWeight: FontWeight.w600,
                ),
              ),
              calendarStyle: CalendarStyle(
                outsideDaysVisible: false,
                isTodayHighlighted: false,
                selectedDecoration: BoxDecoration(
                  color: Colors.transparent,
                  shape: BoxShape.circle,
                  border: Border.all(
                    color: selectedBorderColor,
                    width: 2,
                  ),
                ),
                todayDecoration: const BoxDecoration(
                  color: Colors.transparent,
                ),
                defaultTextStyle: TextStyle(
                  color: textColor,
                ),
                weekendTextStyle: TextStyle(
                  color: weekendColor,
                ),
                selectedTextStyle: TextStyle(
                  color: selectedTextColor,
                  fontWeight: FontWeight.w600,
                ),
                disabledTextStyle: TextStyle(
                  color: isDark ? Colors.grey.shade700 : Colors.grey.shade400,
                ),
              ),
            ),

            const SizedBox(height: 40),

            // Texto indicativo de la fiesta
            SizedBox(
              height: 50,
              child: fiestaAsync.when(
                data: (fiesta) {
                  if (fiesta == null) {
                    return _getNoFiestaText(currentLocale.languageCode, textColor);
                  }

                  final nombreFiesta = fiesta.getNombreForLanguage(currentLocale.languageCode);
                  return Text(
                    _getFiestaProximaText(currentLocale.languageCode, nombreFiesta),
                    style: TextStyle(
                      fontSize: 20,
                      fontFamily: 'Cardo',
                      color: textColor,
                    ),
                    textAlign: TextAlign.center,
                  );
                },
                loading: () => const SizedBox(height: 20),
                error: (e, _) => Text(
                  'Error: $e',
                  style: TextStyle(
                    color: isDark ? Colors.redAccent : Colors.red,
                  ),
                  textAlign: TextAlign.center,
                ),
              ),
            ),

            const SizedBox(height: 40),

            // Botón para acceder a la siguiente pantalla
            FractionallySizedBox(
              widthFactor: 0.4,
              child: ElevatedButton(
                style: ElevatedButton.styleFrom(
                  backgroundColor: buttonColor,
                  foregroundColor: Colors.white,
                  elevation: 3,
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(16),
                  ),
                  padding: const EdgeInsets.symmetric(vertical: 8, horizontal: 16),
                  textStyle: const TextStyle(
                    fontFamily: 'Cardo',
                    fontWeight: FontWeight.w600,
                    fontSize: 14,
                    letterSpacing: 1.1,
                  ),
                ),
                onPressed: fiestaAsync.maybeWhen(
                  data: (fiesta) => fiesta != null
                      ? () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (context) => FiestaScreen(fiesta: fiesta),
                      ),
                    );
                  }
                      : null,
                  orElse: () => null,
                ),
                child: Text(_getContinuarText(currentLocale.languageCode)),
              ),
            ),
          ],
        ),
      ),
    );
  }

  // Funciones auxiliares para los textos traducidos
  Widget _getNoFiestaText(String languageCode, Color textColor) {
    final text = _getNoFiestaString(languageCode);
    return Text(
      text,
      style: TextStyle(
        fontSize: 20,
        fontFamily: 'Cardo',
        color: textColor,
      ),
      textAlign: TextAlign.center,
    );
  }

  String _getNoFiestaString(String languageCode) {
    switch (languageCode) {
      case 'es':
        return "No se ha encontrado fiesta";
      case 'en':
        return "No feast found";
      case 'la':
        return "Festum non inventum";
      default:
        return "No se ha encontrado fiesta";
    }
  }

  String _getFiestaProximaText(String languageCode, String nombreFiesta) {
    switch (languageCode) {
      case 'es':
        return "Fiesta próxima: $nombreFiesta";
      case 'en':
        return "Next feast: $nombreFiesta";
      case 'la':
        return "Festum proximum: $nombreFiesta";
      default:
        return "Fiesta próxima: $nombreFiesta";
    }
  }

  String _getContinuarText(String languageCode) {
    switch (languageCode) {
      case 'es':
        return "Continuar";
      case 'en':
        return "Continue";
      case 'la':
        return "Procedere";
      default:
        return "Continuar";
    }
  }
}