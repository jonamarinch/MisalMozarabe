// Modelo de datos para representar un texto a imprimir
class Texto {
  final int orden;
  final bool ordinario;
  final int tipo;
  final String txtEs;
  final List<String> fiestas;

  // Constructor con todos los campos requeridos para garantizar inmutabilidad.
  Texto({
    required this.orden,
    required this.ordinario,
    required this.tipo,
    required this.txtEs,
    required this.fiestas,
  });

  /// Convierte el objeto Texto en un mapa JSON (para guardar en caché)
  Map<String, dynamic> toJson() => {
    'orden': orden,
    'ordinario': ordinario,
    'tipo': tipo,
    'txt_es': txtEs,
    'fiestas': fiestas,
  };

  // Fábrica para crear un Texto a partir de un documento de Firestore
  factory Texto.fromFirestore(Map<String, dynamic> data) {
    return Texto(
      orden: data['orden'] ?? 0,
      ordinario: data['ordinario'] ?? false,
      tipo: data['tipo'] ?? 0,
      txtEs: data['txt_es'] ?? '',
      fiestas: List<String>.from(data['fiestas'] ?? []),
    );
  }
}