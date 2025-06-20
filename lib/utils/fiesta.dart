// Modelo de datos para representar una fiesta litúrgica
class Fiesta {
  /// Código identificador único de la fiesta (ej: 'cua1', 'pas3', etc.)
  final String codigo;
  /// Nombre de la fiesta (ej: 'Domingo tercero de Cuaresma')
  final String nombre;
  /// Tiempo litúrgico al que pertenece (ej: 'Cuaresma', 'Pascua', 'Adviento')
  final String tiempo;
  /// Constructor de la clase Fiesta
  Fiesta({required this.codigo, required this.nombre, required this.tiempo});
  /// Fábrica para crear una Fiesta a partir de un documento de Firestore
  factory Fiesta.fromFirestore(String id, Map<String, dynamic> data) {
    return Fiesta(
      codigo: id,
      nombre: data['nom_es'] ?? '',
      tiempo: data['tiempo'] ?? '',
    );
  }
  /// Convierte el objeto Fiesta en un mapa JSON (para guardar en caché)
  Map<String, dynamic> toJson() => {
    'codigo': codigo,
    'nombre': nombre,
    'tiempo': tiempo,
  };
  /// Crea una Fiesta a partir de un mapa JSON (al leer de caché local)
  factory Fiesta.fromJson(Map<String, dynamic> json) {
    return Fiesta(
      codigo: json['codigo'],
      nombre: json['nombre'],
      tiempo: json['tiempo'],
    );
  }
}