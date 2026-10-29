/* Jimny Launcher WEB preview: no vehicle-system integration or privileged APIs. */
(function(root){
  "use strict";
  const locale = "es-CR";
  const allowedExternalHosts = new Set(["www.google.com","www.waze.com","music.youtube.com"]);
  function formatTime(date){
    if (!(date instanceof Date) || Number.isNaN(date.valueOf())) throw new TypeError("Fecha inválida");
    return new Intl.DateTimeFormat(locale,{hour:"2-digit",minute:"2-digit",hourCycle:"h23"}).format(date);
  }
  function formatDate(date){
    if (!(date instanceof Date) || Number.isNaN(date.valueOf())) throw new TypeError("Fecha inválida");
    return new Intl.DateTimeFormat(locale,{weekday:"long",day:"numeric",month:"long"}).format(date);
  }
  function isTrustedUrl(raw){
    try {const url = new URL(raw);return url.protocol==="https:" && allowedExternalHosts.has(url.hostname) && url.username==="" && url.password==="";}
    catch {return false;}
  }
  const api = {formatTime, formatDate, isTrustedUrl};
  if(typeof module!=="undefined" && module.exports){module.exports=api;}
  if(typeof document==="undefined")return;
  const $ = (id) => document.getElementById(id);
  function tick(){const now=new Date();$("clock").textContent=formatTime(now);$("date").textContent=formatDate(now);}
  tick();
  setInterval(tick,30_000);
  function hideWelcome(){const welcome=$("welcome"); if(welcome)welcome.classList.add("hidden");}
  const skip=$("skip-welcome");if(skip)skip.addEventListener("click",hideWelcome);
  setTimeout(hideWelcome,2300);
  document.querySelectorAll("a[target='_blank']").forEach((link)=>{
    if(!isTrustedUrl(link.href)){link.removeAttribute("href");link.setAttribute("aria-disabled","true");}
  });
})(typeof window!=="undefined"?window:globalThis);
