import 'package:flutter/material.dart';
import 'package:flutter_riverpod/legacy.dart';
import 'package:missale_mozarabicum/utils/liturgical_calendar.dart';
import '../main.dart';
import '../utils/fiesta.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:table_calendar/table_calendar.dart';
import 'package:missale_mozarabicum/screens/fiesta_screen.dart';

// Provider para seleccionar fecha
final selectedDateProvider = StateProvider<DateTime>((ref) => DateTime.now());
// Provider para la fiesta
final fiestaProvider = FutureProvider<Fiesta?>((ref) async {
  final date = ref.watch(selectedDateProvider);
  final codigo = await LiturgicalCalendar.getFiestaDesde(date);
  return FiestaRepository.getFiesta(codigo);
});

/// Pantalla principal donde se elige una fecha y se muestra la fiesta correspondiente
class HomeScreen extends ConsumerWidget {
  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final selectedDate = ref.watch(selectedDateProvider);
    final fiestaAsync = ref.watch(fiestaProvider);

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
                  const Expanded(
                    child: Divider(
                      thickness: 2,
                      color: Color(0xFFc00000),
                      endIndent: 8,
                    ),
                  ),
                  const Text(
                    '✠',
                    style: TextStyle(
                      fontSize: 32,
                      color: Color(0xFFc00000),
                    ),
                  ),
                  const Expanded(
                    child: Divider(
                      thickness: 2,
                      color: Color(0xFFc00000),
                      indent: 8,
                    ),
                  ),
                ],
              ),
            ),

            // Calendario
            TableCalendar(
              locale: ref.watch(localeProvider).toString(),
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

              headerVisible: true, // Controla la visibilidad de la cabecera (mes, botones)

              headerStyle: HeaderStyle(
                titleCentered: true,
              ),

              calendarStyle: CalendarStyle(
                outsideDaysVisible: false,
                isTodayHighlighted: false,
                selectedDecoration: BoxDecoration(
                  color: Colors.transparent,
                  shape: BoxShape.circle,
                  border: Border.all(color: Colors.blue),
                ),
                todayDecoration: BoxDecoration(
                  color: Colors.transparent,
                ),
                defaultTextStyle: TextStyle(color: Colors.black87),
                weekendTextStyle: TextStyle(color: Colors.black54),
                selectedTextStyle: TextStyle(color: Colors.blue),
              ),
            ),

            const SizedBox(height: 40),

            // Texto indicativo de la fiesta
            SizedBox(
              height: 50,
              child: fiestaAsync.when(
                data: (fiesta) => Text(
                  fiesta == null
                      ? "No se ha encontrado fiesta"
                      : "Fiesta próxima: ${fiesta.nombre}",
                  style: const TextStyle(
                    fontSize: 20,
                    fontFamily: 'Cardo',
                  ),
                  textAlign: TextAlign.center,
                ),
                loading: () => const CircularProgressIndicator(),
                error: (e, _) => Text('Error: $e'),
              ),
            ),

            const SizedBox(height: 40),

            // Botón para acceder a la siguiente pantalla
            FractionallySizedBox(
              widthFactor: 0.4, // Ocupa el 40% del ancho del padre
              child: ElevatedButton(
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color(0xFFb8aaa1), // Color acorde a tu estética
                  foregroundColor: Colors.white, // Color del texto
                  elevation: 3,
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(16), // Esquinas redondeadas
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
                child: const Text("Continuar"),
              ),
            ),
          ],
        ),
      ),
    );
  }
}