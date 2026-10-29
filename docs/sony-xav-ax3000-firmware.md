# Sony XAV-AX3000 — estudio de viabilidad de firmware e interfaz

**Estado:** investigación sin modificaciones al equipo.
**Dispositivo observado:** Sony XAV-AX3000 instalado en Suzuki Jimny JB74.
**Versión observada en fotografías (28-10-2026):** CPU 1.27 / MCU 1.27.

## Hallazgos verificados

- Sony publica **1.27**, del **26-01-2022**, como actualización oficial para XAV-AX3000. Indica no actualizar si ya se tiene esa versión.
- El paquete oficial `XAV-AX3000_v127.zip` incluye `MSO1270.FIR`, `MMC1270.U88` y `MDS1270.up6`. No se conoce su formato interno ni se ha verificado que acepte archivos modificados.
- La actualización de firmware se hace por USB; que exista la opción **Actualizar** no demuestra que se puedan instalar binarios propios.
- No está demostrada ninguna vía segura de recuperación, desbloqueo del cargador de arranque, firma de imágenes, ni un SDK oficial para reemplazar el firmware del estéreo.

**Fuente oficial:** https://www.sony-latin.com/es/electronics/support/software/00274540

## Qué queremos construir

Un panel Jimny de tonos negros/naranjas con pantalla de bienvenida «BIENVENIDO, JUAN», ilustración de Jimny blanco, hora, mapa, acceso a YouTube Music y controles sencillos. La prioridad es **no perder** audio, Bluetooth, cámara de reversa, mandos y Android Auto.

## Tres capas distintas

1. **Aplicación de teléfono (APK):** interfaz gráfica, sin alterar la radio; ya existe un prototipo en este repositorio.
2. **Proyección mediante Android Auto:** Android Auto no acepta cualquier launcher Android en su pantalla y restringe las aplicaciones a categorías y componentes admitidos. Una aplicación con solamente `ACTION_MAIN` y `CATEGORY_LAUNCHER` no declara soporte Android Auto. Usar Fermata o mirroring es otra capa distinta, no un firmware.
3. **Firmware de la Sony:** software de bajo nivel de la unidad (CPU/MCU). No se puede afirmar que un proyecto externo se instale o sustituya la interfaz hasta conocer el SoC, arranque, formato de imagen, firmas y procedimiento de recuperación.

**Documentación de Android Auto:** https://developer.android.com/training/cars/platforms/android-auto

## Auditoría del proyecto actual

- `app/src/main/AndroidManifest.xml` registra `MainActivity` como actividad **LAUNCHER de Android** para teléfono. No incluye una declaración compatible con ninguna de las categorías admitidas por Android Auto.
- `app/src/main/java/com/jimny/launcher/MainActivity.kt` ya implementa colores naranja/negro, saludo a Juan, ilustración del Jimny, accesos a Maps/Waze/YouTube Music y pantalla de bienvenida temporal.
- Su `openApp()` usa `getLaunchIntentForPackage()`, una operación típica de teléfono que no garantiza abrir otra app dentro de Android Auto.

## Propuesta de trabajo sin riesgo de bloqueo

### Fase 1 — prototipo visual y Android Auto

- Mantener intacto el firmware de Sony.
- Continuar el APK para su ejecución en el teléfono y validar qué se proyecta al usar Fermata/mirroring, **solo con el vehículo estacionado**.
- Investigar las categorías y APIs admitidas por Android Auto para casos genuinamente compatibles. No afirmar compatibilidad hasta probarla.
- Compilar el APK con GitHub Actions y probarlo desde el teléfono; ningún flujo debe generar archivos `.FIR`, `.U88` o `.up6` personalizados para flashear.

### Fase 2 — estudio de firmware **solo lectura**

- Identificar variantes regionales y etiqueta exacta de la radio, placa y SoC, sin desarmar ni intervenir todavía.
- Examinar únicamente el paquete oficial de actualización descargado de Sony: nombres, checksums, cabeceras, estructura y componentes, sin redistribuir sus binarios propietarios.
- Estudiar documentación pública sobre arranque seguro, firmas, diagnóstico y procedimiento de restauración específico para esta placa.
- Preparar pruebas aisladas y revisar los riesgos, licencias y límites técnicos.

### Fase 3 — decisión de viabilidad

**Condiciones mínimas antes de siquiera considerar una prueba de firmware propia:**
1. Modelo y hardware exactos confirmados.
2. Copia de seguridad completa y comprobada de los datos propios del dispositivo, si es técnicamente accesible.
3. Método de recuperación/reprogramación verificable y probado fuera del vehículo.
4. Integridad de audio, pantalla, cámara y controles garantizable mediante pruebas.
5. Compatibilidad y firma de actualizaciones entendidas; permisos y licencias revisados.

Si falta cualquiera de estas condiciones, **NO instalar firmware modificado**.

## Resultado de esta investigación

Podemos crear código, documentación, analizadores de archivos y prototipos por GitHub. **No podemos prometer un firmware alternativo instalable en la XAV-AX3000 con la información disponible.** La vía práctica inmediata es JimnyLauncher como aplicación de teléfono y una ruta validada de proyección, con el equipo estacionado.
