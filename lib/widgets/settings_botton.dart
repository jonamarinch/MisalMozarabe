import 'package:flutter/material.dart';
import 'settings_dialog.dart';

/// Botón flotante para abrir la configuración
class SettingsFloatingButton extends StatelessWidget {
  const SettingsFloatingButton({super.key});

  @override
  Widget build(BuildContext context) {
    return FloatingActionButton(
      onPressed: () => _showSettingsDialog(context),
      backgroundColor: const Color(0xFFB8AAA1),
      foregroundColor: Colors.white,
      elevation: 4,
      child: const Icon(Icons.settings),
    );
  }

  void _showSettingsDialog(BuildContext context) {
    showDialog(
      context: context,
      barrierDismissible: true,
      builder: (context) => const SettingsDialog(),
    );
  }
}

/// Botón de configuración para el AppBar
class SettingsAppBarButton extends StatelessWidget {
  const SettingsAppBarButton({super.key});

  @override
  Widget build(BuildContext context) {
    return IconButton(
      onPressed: () => _showSettingsDialog(context),
      icon: const Icon(Icons.settings),
      tooltip: 'Configuración',
    );
  }

  void _showSettingsDialog(BuildContext context) {
    showDialog(
      context: context,
      barrierDismissible: true,
      builder: (context) => const SettingsDialog(),
    );
  }
}