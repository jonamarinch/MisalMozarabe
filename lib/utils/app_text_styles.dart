import 'package:flutter/material.dart';
import 'tipos_texto.dart';
import 'tipo_texto_style.dart';

@immutable
class AppTextStyles extends ThemeExtension<AppTextStyles> {
  final Map<TipoTexto, TipoTextoStyle> estilos;

  const AppTextStyles(this.estilos);

  TipoTextoStyle byTipo(TipoTexto tipo) => estilos[tipo]!;

  @override
  AppTextStyles copyWith({Map<TipoTexto, TipoTextoStyle>? estilos}) =>
      AppTextStyles(estilos ?? this.estilos);

  @override
  ThemeExtension<AppTextStyles> lerp(
      covariant ThemeExtension<AppTextStyles>? other,
      double t,
      ) {
    if (other is! AppTextStyles) return this;
    return this; // no interpolamos estilos
  }
}