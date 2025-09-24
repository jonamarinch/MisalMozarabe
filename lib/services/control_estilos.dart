// lib/services/control_estilos.dart
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:missale_mozarabicum/services/tipo_texto_style.dart';
import 'package:missale_mozarabicum/services/tipos_texto.dart';
import 'package:missale_mozarabicum/services/estilos_factory.dart';
import 'package:missale_mozarabicum/providers/liturgical_text_scale_provider.dart';
import 'app_text_styles.dart';

class ControlEstilos {
  /// Obtiene el estilo para un tipo de texto específico
  /// usando la configuración actual del usuario
  static TipoTextoStyle estiloPorTipo(
      BuildContext context,
      WidgetRef ref,
      TipoTexto tipo,
      ) {
    final useSystemFont = ref.watch(liturgicalUseSystemFontProvider);
    final textScale = ref.watch(liturgicalTextScaleProvider);

    // Construir los estilos con la configuración actual
    final appStyles = buildAppTextStylesWithSettings(
      context,
      useSystemFont: useSystemFont,
      textScale: textScale,
    );

    return appStyles.byTipo(tipo);
  }

  /// Versión legacy que usa la extensión del tema (mantenemos por compatibilidad)
  static TipoTextoStyle estiloPorTipoLegacy(BuildContext context, TipoTexto tipo) {
    final ext = Theme.of(context).extension<AppTextStyles>();
    if (ext == null) {
      throw Exception('AppTextStyles no está en el Theme');
    }
    return ext.byTipo(tipo);
  }
}