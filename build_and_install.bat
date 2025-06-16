@echo off
setlocal

echo 🚀 Construyendo APK directamente con Gradle...
cd android
call gradlew.bat assembleDebug
if errorlevel 1 (
    echo ❌ ERROR: Falló la compilación con Gradle. Abortando.
    pause
    exit /b 1
)
cd ..

set APK_SRC=android\app\build\outputs\apk\debug\app-debug.apk
set APK_DEST=build\app\outputs\flutter-apk\app-debug.apk

echo 📁 Asegurando carpeta de salida...
mkdir "build\app\outputs\flutter-apk" 2>nul

if exist "%APK_SRC%" (
    echo 📦 Copiando APK generado por Gradle a flutter-apk...
    copy /Y "%APK_SRC%" "%APK_DEST%"
) else (
    echo ❌ ERROR: El archivo APK no fue encontrado en %APK_SRC%
    pause
    exit /b 1
)

set APK2_SRC=android\app\build\outputs\apk\release\app-release.apk
set APK2_DEST=build\app\outputs\flutter-apk\app-release.apk

echo 📁 Asegurando carpeta de salida...
mkdir "build\app\outputs\flutter-apk" 2>nul

if exist "%APK2_SRC%" (
    echo 📦 Copiando APK generado por Gradle a flutter-apk...
    copy /Y "%APK2_SRC%" "%APK2_DEST%"
) else (
    echo ❌ ERROR: El archivo APK no fue encontrado en %APK2_SRC%
    pause
    exit /b 1
)

echo 📲 Instalando con flutter install...
flutter install

pause