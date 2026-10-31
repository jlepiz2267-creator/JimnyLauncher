# Matte Launcher — Motorola

Launcher Android nativo (Java), inspirado en una referencia monocromática: negro mate, texto blanco, reloj grande y únicamente las aplicaciones que el usuario selecciona.

## Funciones (v0.1)
- Reloj y fecha automáticos en español (Costa Rica), respetando el formato horario del sistema.
- Indicador de batería real.
- Lista de aplicaciones instaladas para marcar/desmarcar y guardar las que se muestran.
- Pantalla de inicio sin cajón de aplicaciones y sin iconos de colores.
- Se guarda la selección localmente, sin pedir permiso de contactos, archivos o ubicación.
- Botón para elegir Matte Launcher como pantalla de inicio predeterminada; reversible desde Ajustes Android.
- Código separado de cualquier proyecto de Android Auto.

## Compilación
Abrir esta carpeta como proyecto Android en Android Studio con JDK 17 y Android SDK API 35, o ejecutar `gradle :app:assembleDebug` con Gradle 8.11.1. El archivo de salida queda en `app/build/outputs/apk/debug/app-debug.apk`.

En GitHub, ejecutar **Actions > Compilar Matte Launcher APK > Run workflow** y descargar el artefacto `MatteLauncher-debug-apk` cuando el trabajo termine correctamente. Los APK debug se firman de prueba para instalación personal, **no son builds de producción**.

## Instalación en Motorola
1. Abrir el APK descargado e instalarlo (puede ser necesario permitir instalaciones desde tu gestor de archivos).
2. Abrir **Matte Launcher** y tocar **EDITAR APLICACIONES**; seleccionar las apps y pulsar **GUARDAR**.
3. Tocar **PONER COMO INICIO** y seleccionar el launcher, o entrar en `Ajustes > Apps > Aplicaciones predeterminadas > App de inicio` (nombres según versión de Motorola/Android).
4. Para regresar a Motorola, cambiar la app de inicio predeterminada en los ajustes del sistema.

**Importante:** Ocultar apps en este launcher no las desinstala, bloquea ni protege con contraseña: siguen accesibles desde Configuración y otros medios. No es modo quiosco ni control parental.
