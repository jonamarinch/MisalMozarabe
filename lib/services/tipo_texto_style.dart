import 'package:flutter/material.dart';

class TipoTextoStyle {
  final TextStyle style;
  final TextAlign align;
  final bool uppercase;

  /// Si se define, resalta desde el inicio hasta el final del primer match.
  final RegExp? highlightUntilPattern;

  /// Color del resaltado inicial.
  final Color? highlightColor;

  const TipoTextoStyle({
    required this.style,
    this.align = TextAlign.start,
    this.uppercase = false,
    this.highlightUntilPattern,
    this.highlightColor,
  });

  TipoTextoStyle copyWith({
    TextStyle? style,
    TextAlign? align,
    bool? uppercase,
    RegExp? highlightUntilPattern,
    Color? highlightColor,
  }) {
    return TipoTextoStyle(
      style: style ?? this.style,
      align: align ?? this.align,
      uppercase: uppercase ?? this.uppercase,
      highlightUntilPattern: highlightUntilPattern ?? this.highlightUntilPattern,
      highlightColor: highlightColor ?? this.highlightColor,
    );
  }
}