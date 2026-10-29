# Instalación segura de JimnyLauncher v0.7 para Android

## Por qué se creó esta variante

La versión 0.6 incluía un `NotificationListenerService` para obtener controles de música de otras aplicaciones. Play Protect identificó la solicitud de **acceso a notificaciones** como permiso sensible y bloqueó la instalación desde el administrador de archivos del teléfono. **No implica automáticamente que haya código malicioso**, pero sí que debemos minimizar los permisos.

La v0.7 retira el servicio y su declaración del manifiesto, así como las referencias del reproductor a ese acceso. **Único permiso solicitado:** `INTERNET`, para mostrar el mapa online de OpenStreetMap. No solicita acceso a notificaciones, SMS, accesibilidad, cámara, micrófono o ubicación GPS.

## Qué funciona y qué se elimina

- Sigue: interfaz negra/naranja, reloj, mapa web en la pantalla, botones para abrir Maps, Waze y YouTube Music desde el teléfono.
- Eliminado: leer qué canción está activa y cambiar de canción dentro de JimnyLauncher (esas funciones necesitaban el permiso sensible).
- No es firmware Sony ni aplicación certificada para Android Auto.

## Instalación en el teléfono

1. En GitHub, abrir Actions y elegir la última ejecución **exitosa** de `Compilar y verificar JimnyLauncher`, con la revisión de v0.7.
2. Descargar el artefacto `JimnyLauncher-v07-APK`; descomprimir y extraer `app-debug.apk`.
3. Revisar el reporte `apk-verificacion.txt`. Verificar que Play Protect **no bloquee** la instalación; si muestra otro bloqueo, **detenerse y solicitar revisión**.
4. Instalar con las medidas normales de seguridad de Android. Probar primero en el teléfono, no en la Sony.
5. Si aparece un conflicto de firma con una versión previa, no forzar una actualización; el APK de pruebas puede estar firmado con otra clave y requerir desinstalar la versión anterior.

**No desactivar Play Protect ni omitir una advertencia de seguridad.** La eliminación del permiso sensible reduce el riesgo de bloqueo, pero no garantiza la aprobación de Google Play Protect en todos los dispositivos.

## Pruebas de GitHub Actions

El flujo verifica el manifiesto y el APK para rechazar accesos sensibles, prueba la interfaz web, compila el APK, comprueba la firma y lo instala y ejecuta en un emulador Android 15. Estas pruebas no demuestran compatibilidad con el teléfono Samsung concreto o con Android Auto. No es una certificación de seguridad.

Para una distribución permanente, se recomienda configurar firma de lanzamiento propia y un proceso de publicación confiable en vez de depender de una firma de depuración que puede cambiar en cada ejecución.

Fuentes: https://developers.google.com/android/play-protect/warning-dev-guidance?hl=es-419 y https://developer.android.com/reference/android/service/notification/NotificationListenerService
