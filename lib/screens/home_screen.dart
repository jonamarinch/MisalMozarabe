import 'package:flutter/material.dart';
import 'package:flutter_riverpod/legacy.dart';
import 'package:missale_mozarabicum/utils/liturgical_calendar.dart';
import '../main.dart';
import '../utils/fiesta.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

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
      appBar: AppBar(title: const Text("Missale Mozarabicum")),
      body: Padding(
        padding: const EdgeInsets.all(24.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [

            // Calendario
            CalendarDatePicker(
              initialDate: selectedDate,
              firstDate: DateTime(2000),
              lastDate: DateTime(2100),
              onDateChanged: (date) {
                ref.read(selectedDateProvider.notifier).state = date;
              },
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
                  style: const TextStyle(fontSize: 16, fontStyle: FontStyle.italic),
                  textAlign: TextAlign.center,
                ),
                loading: () => const CircularProgressIndicator(),
                error: (e, _) => Text('Error: $e'),
              ),
            ),

            const SizedBox(height: 40),

            // Botón para acceder a la siguiente pantalla
            ElevatedButton(
              onPressed: () {
                // TODO: Navegar a la siguiente pantalla
              },
              child: const Text("Continuar"),
            )
          ],
        ),
      ),
    );
  }
}