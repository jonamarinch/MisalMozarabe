import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:missale_mozarabicum/models/app_settings.dart';

/// Escala de texto actual (derivada de settingsProvider)
final currentTextScaleProvider = Provider<double>((ref) {
  final async = ref.watch(settingsProvider);
  return async.maybeWhen(
    data: (s) => s.textScale,
    orElse: () => 1.0,
  );
});

/// Locale actual (derivada de settingsProvider)
final localeProvider = Provider<Locale>((ref) {
  final async = ref.watch(settingsProvider);
  return async.maybeWhen(
    data: (s) => s.locale,
    orElse: () => const Locale('es', 'ES'),
  );
});