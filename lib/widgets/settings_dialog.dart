import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:missale_mozarabicum/models/app_settings.dart';

/// Ventana modal para configurar la aplicación
class SettingsDialog extends ConsumerWidget {
  const SettingsDialog({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final settingsAsync = ref.watch(settingsProvider);
    final settings = settingsAsync.value ?? const AppSettings();
    final settingsNotifier = ref.read(settingsProvider.notifier);

    // Detectar tema
    final brightness = Theme.of(context).brightness;
    final isDark = brightness == Brightness.dark;

    // Definir colores basados en el tema
    final dialogBg = isDark ? const Color(0xFF1E1E1E) : Colors.white;
    final headerBg = isDark ? const Color(0xFF2D2D2D) : const Color(0xFFf9efee);
    final contentBg = isDark ? const Color(0xFF1E1E1E) : const Color(0xFFfff8f7);
    final textColor = isDark ? Colors.white : Colors.black87;
    final iconColor = isDark ? Colors.white70 : Colors.black87;
    final borderColor = isDark ? Colors.grey.shade800 : Colors.grey.shade300;
    final itemBg = isDark ? const Color(0xFF2D2D2D) : Colors.white;

    return Dialog(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      backgroundColor: dialogBg,
      child: Container(
        width: MediaQuery.of(context).size.width * 0.85,
        constraints: BoxConstraints(
          maxHeight: MediaQuery.of(context).size.height * 0.7,
          maxWidth: 400,
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            // Encabezado
            Container(
              padding: const EdgeInsets.all(20),
              decoration: BoxDecoration(
                color: headerBg,
                borderRadius: const BorderRadius.vertical(top: Radius.circular(16)),
              ),
              child: Row(
                children: [
                  Icon(Icons.settings, color: iconColor, size: 24),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Text(
                      'Configuración',
                      style: TextStyle(
                        fontSize: 20,
                        fontWeight: FontWeight.w600,
                        fontFamily: 'Cardo',
                        color: textColor,
                      ),
                    ),
                  ),
                  IconButton(
                    onPressed: () => Navigator.of(context).pop(),
                    icon: const Icon(Icons.close),
                    color: iconColor,
                  ),
                ],
              ),
            ),

            // Contenido
            Flexible(
              child: Container(
                color: contentBg,
                child: SingleChildScrollView(
                  padding: const EdgeInsets.all(20),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      _SectionHeader(
                        icon: Icons.language,
                        title: 'Idioma',
                        textColor: textColor,
                        iconColor: iconColor,
                      ),
                      const SizedBox(height: 12),
                      _LanguageSelector(
                        currentLocale: settings.locale,
                        onChanged: settingsNotifier.setLocale,
                        borderColor: borderColor,
                        itemBg: itemBg,
                        textColor: textColor,
                        isDark: isDark,
                      ),
                      /*
                      const SizedBox(height: 24),
                      _SectionHeader(
                        icon: Icons.palette,
                        title: 'Apariencia',
                        textColor: textColor,
                        iconColor: iconColor,
                      ),
                      const SizedBox(height: 12),
                      _ThemeSelector(
                        currentTheme: settings.themeMode,
                        onChanged: settingsNotifier.setThemeMode,
                        borderColor: borderColor,
                        itemBg: itemBg,
                        textColor: textColor,
                        iconColor: iconColor,
                        isDark: isDark,
                      ),
                      */
                      const SizedBox(height: 24),
                      _SectionHeader(
                        icon: Icons.text_fields,
                        title: 'Texto',
                        textColor: textColor,
                        iconColor: iconColor,
                      ),
                      const SizedBox(height: 12),
                      _TextScaleSlider(
                        currentScale: settings.textScale,
                        onChanged: settingsNotifier.setTextScale,
                        borderColor: borderColor,
                        itemBg: itemBg,
                        textColor: textColor,
                        isDark: isDark,
                      ),
                      const SizedBox(height: 16),
                      _SystemFontSwitch(
                        useSystemFont: settings.useSystemFont,
                        onChanged: settingsNotifier.setUseSystemFont,
                        borderColor: borderColor,
                        itemBg: itemBg,
                        textColor: textColor,
                        iconColor: iconColor,
                        isDark: isDark,
                      ),
                      const SizedBox(height: 32),
                      Center(
                        child: TextButton.icon(
                          onPressed: () async {
                            await settingsNotifier.resetToDefaults();
                            if (context.mounted) {
                              ScaffoldMessenger.of(context).showSnackBar(
                                const SnackBar(
                                  content: Text('Configuración restablecida'),
                                  duration: Duration(seconds: 2),
                                ),
                              );
                            }
                          },
                          icon: Icon(Icons.restore, color: iconColor),
                          label: Text(
                            'Restablecer valores por defecto',
                            style: TextStyle(color: textColor),
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _SectionHeader extends StatelessWidget {
  final IconData icon;
  final String title;
  final Color textColor;
  final Color iconColor;

  const _SectionHeader({
    required this.icon,
    required this.title,
    required this.textColor,
    required this.iconColor,
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Icon(icon, size: 20, color: iconColor),
        const SizedBox(width: 8),
        Text(
          title,
          style: TextStyle(
            fontSize: 16,
            fontWeight: FontWeight.w600,
            fontFamily: 'Cardo',
            color: textColor,
          ),
        ),
      ],
    );
  }
}

class _LanguageSelector extends StatelessWidget {
  final Locale currentLocale;
  final Function(Locale) onChanged;
  final Color borderColor;
  final Color itemBg;
  final Color textColor;
  final bool isDark;

  const _LanguageSelector({
    required this.currentLocale,
    required this.onChanged,
    required this.borderColor,
    required this.itemBg,
    required this.textColor,
    required this.isDark,
  });

  @override
  Widget build(BuildContext context) {
    final languages = [
      {'code': 'es_ES', 'name': 'Español', 'locale': const Locale('es', 'ES')},
      {'code': 'en_US', 'name': 'English', 'locale': const Locale('en', 'US')},
      {'code': 'la_VA', 'name': 'Latinum', 'locale': const Locale('la', 'VA')},
    ];

    final activeColor = isDark ? const Color(0xFFB8AAA1) : Colors.black87;

    return Container(
      decoration: BoxDecoration(
        border: Border.all(color: borderColor),
        borderRadius: BorderRadius.circular(8),
        color: itemBg,
      ),
      child: Column(
        children: languages.map((lang) {
          final locale = lang['locale'] as Locale;
          final isSelected = currentLocale == locale;

          return ListTile(
            leading: Radio<Locale>(
              value: locale,
              groupValue: currentLocale,
              onChanged: (value) => value != null ? onChanged(value) : null,
              activeColor: activeColor,
            ),
            title: Text(
              lang['name'] as String,
              style: TextStyle(
                fontWeight: isSelected ? FontWeight.w600 : FontWeight.normal,
                color: textColor,
              ),
            ),
            onTap: () => onChanged(locale),
            dense: true,
          );
        }).toList(),
      ),
    );
  }
}

class _ThemeSelector extends StatelessWidget {
  final ThemeMode currentTheme;
  final Function(ThemeMode) onChanged;
  final Color borderColor;
  final Color itemBg;
  final Color textColor;
  final Color iconColor;
  final bool isDark;

  const _ThemeSelector({
    required this.currentTheme,
    required this.onChanged,
    required this.borderColor,
    required this.itemBg,
    required this.textColor,
    required this.iconColor,
    required this.isDark,
  });

  @override
  Widget build(BuildContext context) {
    final themes = [
      {'mode': ThemeMode.system, 'name': 'Seguir sistema', 'icon': Icons.settings_system_daydream},
      {'mode': ThemeMode.light, 'name': 'Modo claro', 'icon': Icons.light_mode},
      {'mode': ThemeMode.dark, 'name': 'Modo oscuro', 'icon': Icons.dark_mode},
    ];

    final activeColor = isDark ? const Color(0xFFB8AAA1) : Colors.black87;

    return Container(
      decoration: BoxDecoration(
        border: Border.all(color: borderColor),
        borderRadius: BorderRadius.circular(8),
        color: itemBg,
      ),
      child: Column(
        children: themes.map((theme) {
          final mode = theme['mode'] as ThemeMode;
          final isSelected = currentTheme == mode;

          return ListTile(
            leading: Radio<ThemeMode>(
              value: mode,
              groupValue: currentTheme,
              onChanged: (value) => value != null ? onChanged(value) : null,
              activeColor: activeColor,
            ),
            title: Row(
              children: [
                Icon(theme['icon'] as IconData, size: 20, color: iconColor),
                const SizedBox(width: 8),
                Text(
                  theme['name'] as String,
                  style: TextStyle(
                    fontWeight: isSelected ? FontWeight.w600 : FontWeight.normal,
                    color: textColor,
                  ),
                ),
              ],
            ),
            onTap: () => onChanged(mode),
            dense: true,
          );
        }).toList(),
      ),
    );
  }
}

class _TextScaleSlider extends StatelessWidget {
  final double currentScale;
  final Function(double) onChanged;
  final Color borderColor;
  final Color itemBg;
  final Color textColor;
  final bool isDark;

  const _TextScaleSlider({
    required this.currentScale,
    required this.onChanged,
    required this.borderColor,
    required this.itemBg,
    required this.textColor,
    required this.isDark,
  });

  @override
  Widget build(BuildContext context) {
    final activeColor = isDark ? const Color(0xFFB8AAA1) : Colors.black87;
    final inactiveColor = isDark ? Colors.grey.shade700 : Colors.grey.shade300;

    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        border: Border.all(color: borderColor),
        borderRadius: BorderRadius.circular(8),
        color: itemBg,
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text('Tamaño del texto', style: TextStyle(color: textColor)),
              Text(
                '${(currentScale * 100).round()}%',
                style: TextStyle(fontWeight: FontWeight.w600, color: textColor),
              ),
            ],
          ),
          const SizedBox(height: 8),
          SliderTheme(
            data: SliderTheme.of(context).copyWith(
              activeTrackColor: activeColor,
              thumbColor: activeColor,
              inactiveTrackColor: inactiveColor,
            ),
            child: Slider(
              value: currentScale,
              min: 0.8,
              max: 1.5,
              divisions: 7,
              onChanged: onChanged,
            ),
          ),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text('Pequeño', style: TextStyle(fontSize: 12, color: textColor.withOpacity(0.6))),
              Text('Grande', style: TextStyle(fontSize: 12, color: textColor.withOpacity(0.6))),
            ],
          ),
        ],
      ),
    );
  }
}

class _SystemFontSwitch extends StatelessWidget {
  final bool useSystemFont;
  final Function(bool) onChanged;
  final Color borderColor;
  final Color itemBg;
  final Color textColor;
  final Color iconColor;
  final bool isDark;

  const _SystemFontSwitch({
    required this.useSystemFont,
    required this.onChanged,
    required this.borderColor,
    required this.itemBg,
    required this.textColor,
    required this.iconColor,
    required this.isDark,
  });

  @override
  Widget build(BuildContext context) {
    final activeColor = isDark ? const Color(0xFFB8AAA1) : Colors.black87;

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      decoration: BoxDecoration(
        border: Border.all(color: borderColor),
        borderRadius: BorderRadius.circular(8),
        color: itemBg,
      ),
      child: Row(
        children: [
          Icon(Icons.font_download, size: 20, color: iconColor),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Usar fuente del sistema',
                  style: TextStyle(fontWeight: FontWeight.w500, color: textColor),
                ),
                Text(
                  'Utiliza la fuente predeterminada del dispositivo',
                  style: TextStyle(fontSize: 12, color: textColor.withOpacity(0.6)),
                ),
              ],
            ),
          ),
          Switch(
            value: useSystemFont,
            onChanged: onChanged,
            activeColor: activeColor,
          ),
        ],
      ),
    );
  }
}