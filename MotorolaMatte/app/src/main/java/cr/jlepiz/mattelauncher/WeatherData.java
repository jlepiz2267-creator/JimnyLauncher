package cr.jlepiz.mattelauncher;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

/** Datos meteorológicos de Open-Meteo. No se usa una clave de API. */
final class WeatherData {
    static final String PREFS = "matte_weather";
    static final String ALERTS = "alerts_enabled";
    static final String CHANNEL = "matte_weather_updates";
    static final long REFRESH_MS = 30L * 60 * 1000;
    private static final long ALERT_INTERVAL_MS = 2L * 60 * 60 * 1000;
    private WeatherData() {}

    static final class Report {
        final double temperature, apparent, precipitation;
        final int weatherCode;
        final long timestamp;
        Report(double t, double a, double p, int code, long ts) {
            temperature=t; apparent=a; precipitation=p; weatherCode=code; timestamp=ts;
        }
        String description() {
            if (weatherCode >= 95) return "Tormenta";
            if (weatherCode >= 80 && weatherCode <= 82) return "Chubascos";
            if (weatherCode >= 61 && weatherCode <= 67) return "Lluvia";
            if (weatherCode >= 51 && weatherCode <= 57) return "Llovizna";
            if (weatherCode >= 45 && weatherCode <= 48) return "Neblina";
            if (weatherCode == 3) return "Nublado";
            if (weatherCode == 2) return "Parcialmente nublado";
            if (weatherCode == 1) return "Mayormente despejado";
            if (weatherCode == 0) return "Despejado";
            return "Condiciones actuales";
        }
    }

    static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
    static boolean hasCoords(Context c) {
        return prefs(c).contains("lat") && prefs(c).contains("lon");
    }
    static void saveCoords(Context c, double latitude, double longitude) {
        // Conservamos una coordenada aproximada, no la posición GPS precisa.
        double lat = Math.round(latitude * 100.0) / 100.0;
        double lon = Math.round(longitude * 100.0) / 100.0;
        prefs(c).edit().putLong("lat", Double.doubleToRawLongBits(lat))
            .putLong("lon", Double.doubleToRawLongBits(lon)).apply();
    }
    static Report saved(Context c) {
        SharedPreferences p=prefs(c);
        long ts=p.getLong("weather_ts", 0);
        if (ts==0) return null;
        return new Report(Double.longBitsToDouble(p.getLong("t",0)),
            Double.longBitsToDouble(p.getLong("a",0)),
            Double.longBitsToDouble(p.getLong("p",0)),
            p.getInt("code", -1), ts);
    }
    static boolean alerts(Context c) { return prefs(c).getBoolean(ALERTS, false); }
    static void setAlerts(Context c, boolean enabled) { prefs(c).edit().putBoolean(ALERTS, enabled).apply(); }

    /** Ejecutar únicamente en un hilo de trabajo. */
    static Report fetch(Context context, boolean offerNotification) throws Exception {
        SharedPreferences p=prefs(context);
        if (!hasCoords(context)) throw new IllegalStateException("Ubicación no configurada");
        double lat=Double.longBitsToDouble(p.getLong("lat", 0));
        double lon=Double.longBitsToDouble(p.getLong("lon", 0));
        String url=String.format(Locale.US,
            "https://api.open-meteo.com/v1/forecast?latitude=%.2f&longitude=%.2f&current=temperature_2m,apparent_temperature,precipitation,weather_code&timezone=auto&forecast_days=1",lat,lon);
        HttpURLConnection connection=(HttpURLConnection)new URL(url).openConnection();
        connection.setConnectTimeout(9000);
        connection.setReadTimeout(9000);
        connection.setRequestProperty("Accept", "application/json");
        StringBuilder result=new StringBuilder();
        try {
            if (connection.getResponseCode()!=200) throw new java.io.IOException("HTTP "+connection.getResponseCode());
            try (BufferedReader reader=new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"))) {
                String line;
                while ((line=reader.readLine())!=null) {
                    result.append(line);
                    if (result.length()>65000) throw new java.io.IOException("Respuesta demasiado grande");
                }
            }
        } finally { connection.disconnect(); }
        JSONObject current=new JSONObject(result.toString()).getJSONObject("current");
        Report report=new Report(current.getDouble("temperature_2m"),current.getDouble("apparent_temperature"),
            current.optDouble("precipitation",0),current.optInt("weather_code",-1),System.currentTimeMillis());
        p.edit().putLong("t",Double.doubleToRawLongBits(report.temperature))
            .putLong("a",Double.doubleToRawLongBits(report.apparent))
            .putLong("p",Double.doubleToRawLongBits(report.precipitation))
            .putInt("code",report.weatherCode).putLong("weather_ts",report.timestamp).apply();
        if (offerNotification) notifyWeather(context, report);
        return report;
    }

    static void notifyWeather(Context context, Report report) {
        if (!alerts(context)) return;
        if (Build.VERSION.SDK_INT >= 33 &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED) return;
        SharedPreferences p=prefs(context);
        long last=p.getLong("last_notice",0);
        if (System.currentTimeMillis()-last < ALERT_INTERVAL_MS) return;
        NotificationManager nm=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm==null || !nm.areNotificationsEnabled()) return;
        nm.createNotificationChannel(new NotificationChannel(CHANNEL,"Actualizaciones del clima",NotificationManager.IMPORTANCE_DEFAULT));
        Intent intent=new Intent(context,MainActivity.class);
        PendingIntent pending=PendingIntent.getActivity(context, 702, intent,
            PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        String content=String.format(Locale.forLanguageTag("es-CR"),
            "%s · %.0f °C · sensación %.0f °C",report.description(),report.temperature,report.apparent);
        Notification note=new Notification.Builder(context,CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("Clima en tu zona")
            .setContentText(content)
            .setAutoCancel(true).setContentIntent(pending).build();
        nm.notify(702,note);
        p.edit().putLong("last_notice",System.currentTimeMillis()).apply();
    }
}
