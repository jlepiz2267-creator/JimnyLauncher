# JimnyLauncher

Panel personalizado para Suzuki Jimny JB74. Proyecto experimental **independiente del firmware de Sony**.

## Dos versiones

- **App Android 0.5:** `app/src/main/` es un APK para el **teléfono**. Saludo «BIENVENIDO, JUAN», ilustración Jimny blanco, hora, Maps, Waze y YouTube Music. Puede abrir apps instaladas en el teléfono; **no es una aplicación certificada de Android Auto**.
- **Panel web 0.5:** `docs/dashboard/index.html`, diseño negro/naranja en horizontal, bienvenida y tres enlaces externos. Se puede abrir en un navegador compatible desde teléfono u ordenador. La proyección mediante el navegador de Fermata, si existe en tu instalación, **no está garantizada**.

## Instalación del APK desde el teléfono, sin PC

1. En GitHub, abrir la pestaña **Actions** y la última ejecución exitosa de `Compilar y verificar JimnyLauncher`.
2. Desplazarse hasta **Artifacts** y descargar `JimnyLauncher-v05-APK`.
3. Descomprimir ZIP y abrir `app-debug.apk` en el teléfono. La instalación puede requerir confirmar la fuente de aplicaciones en Ajustes.
4. Abrir **Jimny Launcher** desde el teléfono. Comprobar los botones de Maps, Waze y YouTube Music.
5. No confundir la app del teléfono con una app del menú de Android Auto.

La app de prueba usa firma de depuración de GitHub Actions, que puede **cambiar entre compilaciones**. Si la actualización indica conflicto de firma, desinstalar la versión de pruebas anterior e instalar la nueva; se perderán los datos locales de esa app. **No desinstalar Android Auto, Fermata, Maps ni otras aplicaciones.**

## Panel web

Abrir `docs/dashboard/index.html` directamente desde un navegador moderno, con sus archivos CSS y JS en la misma carpeta. No requiere permisos de ubicación. La hora y fecha se toman del dispositivo.

Para publicación mediante GitHub Pages, configurar el repositorio en **Settings → Pages → Deploy from a branch**, seleccionar rama principal y carpeta `/docs`. Si se configura así, el panel queda en `https://jlepiz2267-creator.github.io/JimnyLauncher/dashboard/`. **Esta dirección no debe darse por activa antes de verificar GitHub Pages.** El navegador integrado de Fermata, cuando está disponible, puede intentarse **solo con el vehículo estacionado**. Es un experimento, no una integración garantizada.

## Pruebas automáticas

GitHub Actions ejecuta:
- Pruebas de JavaScript y validación de enlaces seguros con Node.
- Compilación del APK, pruebas unitarias de Gradle y `lintDebug`.
- Verificación de firma, SHA-256 e inspección de permisos del APK.
- Instalación, arranque y captura de pantalla de Android 15 en emulador.

Las pruebas automáticas **no sustituyen** comprobar Android 17 + Android Auto + adaptador inalámbrico + Sony XAV-AX3000 real. No hay acceso a esos dispositivos desde el CI.

## Firmware Sony

XAV-AX3000 CPU/MCU 1.27 (según fotos compartidas). Sony indica que 1.27 es la última versión oficial y que no se necesita actualizar si ya está instalada.

Fuente: https://www.sony-latin.com/es/electronics/support/software/00274540

**No flashear firmware modificado, no borrar la actualización oficial y no copiar archivos experimentales a una memoria USB para la radio.** Este repositorio no contiene firmware Sony. Antes de modificar firmware es indispensable una vía de recuperación y pruebas en hardware controlado.

## Seguridad

Usar cualquier video, navegador o pantalla experimental únicamente con el vehículo estacionado. La navegación y la música durante la conducción deben usar las aplicaciones nativas permitidas por Android Auto. Los enlaces externos son servicios terceros con sus propias condiciones de uso.

Licencia de código: ver LICENSE.
