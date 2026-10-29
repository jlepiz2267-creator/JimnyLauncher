# JimnyLauncher v0.8 — recuperar la interfaz original

La versión v0.7 redujo involuntariamente la interfaz a un diseño provisional y omitió la imagen del Jimny y la pantalla de bienvenida. **v0.8 recupera el panel visual v0.5** y conserva la corrección de seguridad v0.7.

## Qué incluye

- Bienvenida `BIENVENIDO, JUAN` al abrir.
- Tablero negro/naranja con el Jimny blanco, tarjeta de reloj, accesos a Google Maps, Waze y YouTube Music.
- Nuevo botón `VER MAPA` para abrir el mapa integrado opcional y regresar al panel desde la misma app. El mapa carga desde la red; sin conexión puede aparecer vacío.
- Permisos limitados a `INTERNET`. No se declara ningún servicio de acceso a notificaciones.
- No puede incrustar el reproductor real de YouTube Music sin integración adicional; los botones abren aplicaciones del teléfono y no la interfaz de Android Auto.

## Descargar e instalar en el teléfono

1. En GitHub, abrir **Actions** y elegir la ejecución más reciente de la versión v0.8 que tenga **todos los trabajos en verde**.
2. En la sección **Artifacts**, descargar `JimnyLauncher-v08-APK`; extraer el archivo `app-debug.apk`.
3. Revisar su informe de verificación de permisos y firma.
4. Solo si Play Protect permite instalar normalmente, completar la instalación.
5. Si aparece `La aplicación no se instaló` por una firma distinta a la de v0.7, desinstalar **solo Jimny Launcher** y volver a instalar v0.8. No desinstalar Fermata, Maps ni Android Auto.
6. Probar con el carro estacionado; primero ver el panel de inicio, luego el botón `VER MAPA`, y por último los accesos.

**No desactivar Play Protect ni continuar si bloquea la instalación.** Una compilación correcta en GitHub no garantiza aprobación por Play Protect ni proyección en Sony XAV-AX3000.

## Alcance de las pruebas

El CI ejecuta pruebas estáticas de regresión visual, compila y analiza el código, audita permisos y firma, instala la app y genera una captura en emulador Android 15. No es una prueba física en Samsung ni de Android Auto. Este proyecto no modifica el firmware de Sony.
