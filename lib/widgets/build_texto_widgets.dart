import 'package:flutter/material.dart';
import '../utils/tipo_texto_style.dart';

Widget buildTextoConEstilo(String raw, TipoTextoStyle cfg) {
  final text = cfg.uppercase ? raw.toUpperCase() : raw;

  final cruzPattern = RegExp('✠');
  final cruzStyle = cfg.style.copyWith(color: const Color(0xFFB00020));

  if (cfg.highlightUntilPattern == null || cfg.highlightColor == null) {
    return Text.rich(
      TextSpan(children: _resaltarOcurrencias(
        text: text,
        pattern: cruzPattern,
        base: cfg.style,
        highlight: cruzStyle,
      )),
      textAlign: cfg.align,
    );
  }

  final m = cfg.highlightUntilPattern!.firstMatch(text);
  if (m == null) {
    return Text.rich(
      TextSpan(children: _resaltarOcurrencias(
        text: text,
        pattern: cruzPattern,
        base: cfg.style,
        highlight: cruzStyle,
      )),
      textAlign: cfg.align,
    );
  }

  final cut = m.end;
  final before = text.substring(0, cut);
  final after = text.substring(cut);

  return Text.rich(
    TextSpan(children: [
      ..._resaltarOcurrencias(
        text: before,
        pattern: cruzPattern,
        base: cfg.style.copyWith(color: cfg.highlightColor),
        highlight: cruzStyle,
      ),
      if (after.isNotEmpty)
        ..._resaltarOcurrencias(
          text: after,
          pattern: cruzPattern,
          base: cfg.style,
          highlight: cruzStyle,
        ),
    ]),
    textAlign: cfg.align,
  );
}

Widget buildTextoConCruces(String raw, TipoTextoStyle cfg) {
  final text = cfg.uppercase ? raw.toUpperCase() : raw;

  final base = cfg.style;
  final rojo = base.copyWith(color: const Color(0xFFB00020));

  return Text.rich(
    TextSpan(children: _resaltarOcurrencias(
      text: text,
      pattern: RegExp('✠'),
      base: base,
      highlight: rojo,
    )),
    textAlign: cfg.align,
  );
}

List<TextSpan> _resaltarOcurrencias({
  required String text,
  required RegExp pattern,
  required TextStyle base,
  required TextStyle highlight,
}) {
  final spans = <TextSpan>[];
  int start = 0;
  for (final m in pattern.allMatches(text)) {
    if (m.start > start) {
      spans.add(TextSpan(text: text.substring(start, m.start), style: base));
    }
    spans.add(TextSpan(text: m.group(0), style: highlight));
    start = m.end;
  }
  if (start < text.length) {
    spans.add(TextSpan(text: text.substring(start), style: base));
  }
  return spans;
}