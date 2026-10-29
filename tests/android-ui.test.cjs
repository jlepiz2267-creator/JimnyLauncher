const test = require("node:test");
const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const base = path.join(__dirname,"..");
const ui = fs.readFileSync(path.join(base,"app/src/main/java/com/jimny/launcher/MainActivity.kt"),"utf8");
const manifest = fs.readFileSync(path.join(base,"app/src/main/AndroidManifest.xml"),"utf8");
const gradle = fs.readFileSync(path.join(base,"app/build.gradle.kts"),"utf8");
const workflow = fs.readFileSync(path.join(base,".github/workflows/android.yml"),"utf8");

test("la interfaz naranja y bienvenida originales siguen presentes",()=>{
 for (const value of [
  "BIENVENIDO, JUAN","Hola, Juan","JIMNY  /  DRIVE","SUZUKI JIMNY JB74",
  "TU JIMNY BLANCO","R.drawable.jimny_white","YouTube Music","Google Maps","Waze",
  "JIMNY LAUNCHER V0.8.1"
 ]) assert.ok(ui.includes(value), `Falta ${value}`);
});
test("la pantalla inicial es el dashboard completo y no el mapa simplificado",()=>{
 assert.match(ui,/root\.addView\(body, LinearLayout\.LayoutParams\(-1, 0, 1f\)\)/);
 assert.match(ui,/dashboard = body/);
 assert.match(ui,/val mapLayout = LinearLayout/);
 assert.match(ui,/visibility = View\.GONE/);
 assert.match(ui,/setOnClickListener \{ showMap\(\) \}/);
 assert.match(ui,/setOnClickListener \{ showDashboard\(\) \}/);
 assert.match(ui,/override fun onBackPressed/);
});
test("el diseño no solicita escuchar notificaciones ni accesos invasivos",()=>{
 assert.doesNotMatch(manifest,/NotificationListenerService|BIND_NOTIFICATION_LISTENER_SERVICE|READ_SMS|BIND_ACCESSIBILITY_SERVICE/);
 assert.doesNotMatch(ui,/NotificationListenerService|MediaSessionManager|getActiveSessions|Settings\.ACTION_NOTIFICATION_LISTENER_SETTINGS/);
 assert.match(manifest,/android\.permission\.INTERNET/);
});
test("se mantiene mapa de carga opcional y sin flashear firmware",()=>{
 assert.match(ui,/file:\/\/\/android_asset\/map.html/);
 assert.match(ui,/if \(web\.url == null\)/);
 assert.doesNotMatch(ui,/\.FIR|\.U88|\.up6|flashFirmware/);
});
test("puede elegir la foto original sin solicitar permiso de galería",()=>{
 assert.match(ui,/Intent\\.ACTION_OPEN_DOCUMENT/);
 assert.match(ui,/chosen_original_car_uri/);
 assert.match(ui,/TU JIMNY ORIGINAL/);
 assert.match(ui,/displayOriginalCarPhoto/);
 assert.doesNotMatch(manifest,/READ_MEDIA_IMAGES|READ_EXTERNAL_STORAGE|MANAGE_EXTERNAL_STORAGE/);
});
test("la compilación corresponde al APK restaurado v0.8.1",()=>{
 assert.match(gradle,/versionCode = 9/);
 assert.match(gradle,/versionName = "0\.8\.1"/);
 assert.match(workflow,/JimnyLauncher-v081-APK/);
});
