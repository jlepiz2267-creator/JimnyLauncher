#!/usr/bin/env bash
# La action android-emulator-runner puede ejecutar cada línea de "script" en una shell nueva.
# Ejecutar este archivo en una única shell conserva las variables locales y los errores.
set -Eeuo pipefail

APK_DIR="${GITHUB_WORKSPACE:-$PWD}/app-distribucion"
LOG="${GITHUB_WORKSPACE:-$PWD}/arranque-android.txt"
SCREEN="${GITHUB_WORKSPACE:-$PWD}/captura-emulador.png"

on_error() {
  code=$?
  echo "ERROR en prueba Android (código $code, línea ${BASH_LINENO[0]:-?})" | tee -a "$LOG"
  adb logcat -d -t 500 '*:E' | tail -100 >> "$LOG" 2>&1 || true
  exit "$code"
}
trap on_error ERR

echo "Iniciando prueba de arranque de Jimny Launcher v0.5" | tee "$LOG"
echo "Contenido del artefacto:" | tee -a "$LOG"
find "$APK_DIR" -maxdepth 10 -type f | sort | tee -a "$LOG"
APK="$(find "$APK_DIR" -type f -name 'app-debug.apk' -print -quit)"
if [[ -z "$APK" || ! -s "$APK" ]]; then
  echo "No se encontró app-debug.apk en el artefacto descargado" | tee -a "$LOG"
  exit 1
fi

echo "APK hallado: $APK" | tee -a "$LOG"
sha256sum "$APK" | tee -a "$LOG"
echo "Sistema Android:" | tee -a "$LOG"
adb shell getprop ro.build.version.release | tee -a "$LOG"
adb shell getprop ro.build.version.sdk | tee -a "$LOG"

echo "Instalación:" | tee -a "$LOG"
adb install -r "$APK" | tee -a "$LOG"
echo "Paquete instalado:" | tee -a "$LOG"
adb shell pm path com.jimny.launcher | tee -a "$LOG"
echo "Iniciando actividad principal:" | tee -a "$LOG"
adb shell am start -W -n com.jimny.launcher/.MainActivity | tee -a "$LOG"
sleep 5

PID="$(adb shell pidof -s com.jimny.launcher | tr -d '\r')"
if [[ -z "$PID" ]]; then
  echo "La app no siguió ejecutándose luego del arranque" | tee -a "$LOG"
  exit 1
fi
echo "Proceso activo: $PID" | tee -a "$LOG"
adb shell dumpsys activity activities | grep -m 2 'com.jimny.launcher' | tee -a "$LOG" || true
adb exec-out screencap -p > "$SCREEN"
test -s "$SCREEN"
echo "Prueba OK: instalación, arranque, proceso activo y captura de pantalla" | tee -a "$LOG"
