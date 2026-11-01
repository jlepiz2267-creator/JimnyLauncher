# Matte Launcher — v0.2 para Motorola

Launcher Android nativo, negro mate y tipografía blanca. Proyecto independiente de JimnyLauncher.

## Funciones
- Reloj, fecha y batería en pantalla principal.
- Íconos originales de las apps instaladas en la lista y el selector.
- Lista editable: únicamente las aplicaciones seleccionadas aparecen en el inicio.
- Clima local: temperatura, condición y **sensación térmica** desde Open-Meteo.
- Actualización del clima al abrir el launcher, con caché de 30 minutos y botón de actualización manual.
- Avisos del clima optativos (cada ~2 horas, según programación de Android). Requiere permiso de notificaciones.
- Vistas previas de notificaciones ACTIVAS de Microsoft Teams, WhatsApp / WhatsApp Business y Outlook.
- Pulsar una vista previa intenta abrir su notificación original; si ya no existe, abre la aplicación.

## Privacidad y permisos
- **Ubicación aproximada:** se solicita cuando pulsás ACTIVAR CLIMA y sirve para la consulta meteorológica. Se guarda redondeada aproximadamente a 1 km y se envía a Open-Meteo por HTTPS. No requiere ubicación precisa ni permiso de ubicación en segundo plano.
- **Acceso a notificaciones:** se activa MANUALMENTE en Ajustes de Android desde la sección VISTAS PREVIAS. El permiso del sistema habilita a un listener a leer todas las notificaciones; el código filtra localmente solo Teams, Outlook y WhatsApp. No se guardan textos en disco, ni se envían por internet.
- Solo es posible mostrar notificaciones realmente publicadas por las apps, no el historial de mensajes. Si Android oculta contenido o las notificaciones están desactivadas, la vista previa puede estar vacía. Algunos perfiles laborales bloquean esta función.
- Los avisos del clima pueden desactivarse en la propia pantalla principal.
- Las apps ocultas siguen instaladas y no están bloqueadas.

## Instalar y compilar
Usar Android Studio o `gradle :app:assembleDebug` desde `MotorolaMatte/` con JDK 17, API 35 y Gradle 8.11.1. En GitHub Actions, descargar el ZIP generado por **Compilar Matte Launcher APK** y extraer el APK.

El applicationId no cambia respecto de v0.1; Android debería permitir actualizar la instalación anterior **si la firma APK es la misma**. Los APK debug de diferentes runners normalmente no comparten firma, así que tal vez sea necesario desinstalar la versión anterior. Antes, anotá las aplicaciones seleccionadas porque las preferencias se perderán al desinstalar.

## Limitaciones actuales
El clima se estima para la última coordenada aproximada obtenida al usar el launcher; en segundo plano se usa esa coordenada guardada. Android puede retrasar o cancelar trabajos periódicos por ahorro de batería y al reiniciar se reprogramarán la próxima vez que se abra el launcher. El APK requiere pruebas físicas en el Motorola.


## v0.4: diagnóstico de Google Play Protect
La captura enviada el 1 de noviembre muestra **Matte Launcher** (edición Full) bloqueada por Play Protect. No hay evidencia de que se haya intentado instalar el paquete independiente **Lite** o **Básico**. El bloqueo no demuestra por sí solo que la aplicación sea maliciosa y tampoco se resuelve cambiando únicamente el ícono.

Se agregan tres APK diferenciados:
- `app-full-debug.apk`: funciones completas; necesita acceso al servicio de notificaciones si se habilitan vistas previas.
- `app-lite-debug.apk`: clima, sin servicio de lectura de mensajes; requiere ubicación aproximada, Internet y permiso de notificaciones para avisos del clima.
- `app-basic-debug.apk`: versión mínima **sin permisos declarados**; sin clima ni vistas previas, mantiene el launcher negro mate, reloj, íconos y selección de apps. Instalable independientemente con package `cr.jlepiz.mattelauncher.basic`.

GitHub Actions falla si detecta un permiso Android en la versión Básica o un servicio `NotificationFeed` en Básica/Lite. Compilar correctamente solo verifica el binario, **no garantiza que Play Protect acepte una instalación lateral**. No se debe desactivar ni eludir Play Protect. Para distribuir públicamente, generar una firma release consistente fuera del repositorio y utilizar un canal de pruebas/distribución oficial compatible con Google Play.
