// Modelo de datos para representar un texto a imprimir
class Texto {
  final double orden;
  final bool ordinario;
  final int tipo;
  final String txt;
  final List<String> fiestas;
  final List<String> tiempos;

  // Constructor con todos los campos requeridos para garantizar inmutabilidad.
  Texto({
    required this.orden,
    required this.ordinario,
    required this.tipo,
    required this.txt,
    required this.fiestas,
    required this.tiempos,
  });

  // Constructor para títulos
  factory Texto.titulo(String texto, String codigo) {
    return Texto(
      orden: 0, // Orden de los títulos
      ordinario: false, // El título es un propio
      tipo: 8, // Tipo 'Título'
      txt: texto,
      fiestas: [codigo], // Asociado a la fiesta actual
      tiempos: [''],
    );
  }

  /// Convierte el objeto Texto en un mapa JSON (para guardar en caché)
  Map<String, dynamic> toJson() => {
    'orden': orden,
    'ordinario': ordinario,
    'tipo': tipo,
    'txt_es': txt,
    'fiestas': fiestas,
    'tiempos': tiempos,
  };

  // Fábrica para crear un Texto a partir de un documento de Firestore
  factory Texto.fromFirestore(Map<String, dynamic> data) {
    return Texto(
      orden: data['orden'].toDouble() ?? 0.0,
      ordinario: data['ordinario'] ?? true,
      tipo: data['tipo'] ?? 0,
      txt: data['txt_es'] ?? '', // !!!!AQUÍ EN SU MOMENTO TENDRÉ QUE CONTROLAR EL IDIOMA
      fiestas: List<String>.from(data['fiestas'] ?? []),
      tiempos: List<String>.from(data['tiempos'] ?? []),
    );
  }
}