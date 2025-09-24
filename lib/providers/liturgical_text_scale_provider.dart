// lib/providers/liturgical_text_scale_provider.dart
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:missale_mozarabicum/models/app_settings.dart';

/// Provider específico para la escala de texto litúrgico
/// Solo se aplica a los textos de las fiestas, no a la UI general
final liturgicalTextScaleProvider = Provider<double>((ref) {
  final async = ref.watch(settingsProvider);
  return async.maybeWhen(
    data: (s) => s.textScale,
    orElse: () => 1.0,
  );
});

/// Provider para determinar si usar fuente del sistema en textos litúrgicos
final liturgicalUseSystemFontProvider = Provider<bool>((ref) {
  final async = ref.watch(settingsProvider);
  return async.maybeWhen(
    data: (s) => s.useSystemFont,
    orElse: () => false,
  );
});