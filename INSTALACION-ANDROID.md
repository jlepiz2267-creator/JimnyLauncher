# Instalación de prueba en Android

El flujo `.github/workflows/android.yml` genera el archivo `app-debug.apk` y lo publica dentro del artefacto **JimnyLauncher-v05-APK**.

1. Abrir **Actions** y elegir la ejecución más reciente de **Compilar y verificar JimnyLauncher** en la rama `main`.
2. Confirmar que las pruebas, la compilación y la auditoría de firma terminaron correctamente.
3. Descargar **JimnyLauncher-v05-APK**, descomprimir el ZIP y comprobar el archivo `apk-verificacion.txt`.
4. Instalar `app-debug.apk` solamente si el dispositivo y Play Protect lo permiten. **No desactivar Play Protect ni forzar una instalación bloqueada.**

La firma de depuración la genera Android Gradle Plugin automáticamente. Este APK no es una versión de distribución. Las ejecuciones distintas pueden usar certificados de depuración diferentes y no actualizar una instalación previa.

La instalación en el emulador verifica el arranque básico, pero no demuestra compatibilidad con todos los dispositivos ni garantiza aceptación por Play Protect. El APK es una aplicación Android independiente, no un firmware ni un reemplazo de la interfaz Android Auto del Sony XAV-AX3000.
