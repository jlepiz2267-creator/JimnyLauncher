const test = require("node:test");
const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const base = path.join(__dirname,"..","docs","dashboard");
const api = require(path.join(base,"dashboard.js"));
const html = fs.readFileSync(path.join(base,"index.html"),"utf8");
const css = fs.readFileSync(path.join(base,"dashboard.css"),"utf8");

test("fecha y hora válidas en español de Costa Rica",()=>{
 const d=new Date(2026,9,28,20,34);
 assert.match(api.formatTime(d),/^20:34$/);
 assert.match(api.formatDate(d).toLowerCase(),/28/);
 assert.match(api.formatDate(d).toLowerCase(),/octubre/);
});
test("fechas inválidas se rechazan",()=>{
 assert.throws(()=>api.formatTime(new Date("incorrecto")),TypeError);
 assert.throws(()=>api.formatDate("2026-10-28"),TypeError);
});
test("accesos externos solo HTTPS y dominios permitidos",()=>{
 for(const url of ["https://www.google.com/maps","https://www.waze.com/live-map/","https://music.youtube.com/"])
  assert.equal(api.isTrustedUrl(url),true,url);
 for(const url of ["javascript:alert(1)","http://music.youtube.com","https://music.youtube.com.ejemplo.org","https://malicioso.com","https://user:pass@music.youtube.com","https://www.google.com.evil.com/","no es url"])
  assert.equal(api.isTrustedUrl(url),false,url);
});
test("pantalla completa disponible, bienvenida y controles accesibles",()=>{
 for(const id of ["dashboard","clock","date","welcome","skip-welcome"])
  assert.match(html,new RegExp('id="'+id+'"'));
 assert.match(html,/BIENVENIDO, JUAN/);
 assert.match(html,/JIMNY LAUNCHER/);
 assert.match(html,/target="_blank" rel="noopener noreferrer"/);
 assert.match(html,/vehículo estacionado/);
 assert.match(css,/max-height:490px/);
 assert.match(css,/prefers-reduced-motion/);
});
test("sitio sin scripts remotos ni permisos sensibles",()=>{
 assert.doesNotMatch(html,/<script[^>]+https?:\/\//i);
 assert.doesNotMatch(html,/navigator\.geolocation|requestPermission|mediaDevices/);
});
