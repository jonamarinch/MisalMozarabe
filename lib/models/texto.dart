// Modelo de datos para representar un texto a imprimir
class Texto {
  final double orden;
  final bool ordinario;
  final int tipo;
  final String txt_es;
  final String txt_en;
  final String txt_la;
  final List<String> fiestas;
  final List<String> tiempos;

  // Constructor con todos los campos requeridos para garantizar inmutabilidad.
  Texto({
    required this.orden,
    required this.ordinario,
    required this.tipo,
    required this.txt_es,
    required this.txt_en,
    required this.txt_la,
    required this.fiestas,
    required this.tiempos,
  });

  // Constructor para títulos
  factory Texto.titulo(String textoEs, String textoEn, String textoLa, String codigo) {
    return Texto(
      orden: 0, // Orden de los títulos
      ordinario: false, // El título es un propio
      tipo: 8, // Tipo 'Título'
      txt_es: textoEs,
      txt_en: textoEn,
      txt_la: textoLa,
      fiestas: [codigo], // Asociado a la fiesta actual
      tiempos: [''],
    );
  }

  // Método para obtener el texto según el idioma
  String getTextForLanguage(String languageCode) {
    switch (languageCode) {
      case 'es':
        return txt_es;
      case 'en':
        return txt_en;
      case 'la':
        return txt_la;
      default:
        return txt_es; // Por defecto español
    }
  }

  /// Convierte el objeto Texto en un mapa JSON (para guardar en caché)
  Map<String, dynamic> toJson() => {
    'orden': orden,
    'ordinario': ordinario,
    'tipo': tipo,
    'txt_es': txt_es,
    'txt_en': txt_en,
    'txt_la': txt_la,
    'fiestas': fiestas,
    'tiempos': tiempos,
  };

  // Fábrica para crear un Texto a partir de un documento de Firestore
  factory Texto.fromFirestore(Map<String, dynamic> data) {
    return Texto(
      orden: data['orden'].toDouble() ?? 0.0,
      ordinario: data['ordinario'] ?? true,
      tipo: data['tipo'] ?? 0,
      txt_es: data['txt_es'] ?? '',
      txt_en: data['txt_en'] ?? '',
      txt_la: data['txt_la'] ?? '',
      fiestas: List<String>.from(data['fiestas'] ?? []),
      tiempos: List<String>.from(data['tiempos'] ?? []),
    );
  }
}