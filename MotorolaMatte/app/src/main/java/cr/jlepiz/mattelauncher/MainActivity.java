package cr.jlepiz.mattelauncher;

import android.app.Activity;
import android.Manifest;
import android.location.Location;
import android.location.LocationManager;
import android.widget.ImageView;
import android.app.role.RoleManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Launcher independiente y sin permisos invasivos. Solo presenta y abre
 * las aplicaciones seleccionadas explícitamente por la persona usuaria.
 */
public final class MainActivity extends Activity {
    private static final int BLACK = Color.rgb(10, 10, 10);
    private static final int WHITE = Color.rgb(250, 250, 250);
    private static final int MUTED = Color.rgb(156, 156, 156);
    private static final int BORDER = Color.rgb(39, 39, 39);
    private static final String PREFS = "matte_preferences";
    private static final String KEY_SELECTION = "selected_packages_v1";
    private boolean editing = false;
    private boolean weatherLoading = false;
    private TextView weatherCurrent, weatherDetails;
    private LinearLayout previewContainer;
    private static final int REQUEST_LOCATION=74;
    private static final int REQUEST_WEATHER_NOTIFICATIONS=75;

    private static final class AppEntry {
        final String packageName;
        final String label;
        final ComponentName activity;

        AppEntry(String packageName, String label, ComponentName activity) {
            this.packageName = packageName;
            this.label = label;
            this.activity = activity;
        }
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BLACK);
        getWindow().setNavigationBarColor(BLACK);
        if (Build.VERSION.SDK_INT >= 29) {
            getWindow().setNavigationBarContrastEnforced(false);
            getWindow().setStatusBarContrastEnforced(false);
        }
        if (Build.VERSION.SDK_INT >= 30) {
            getWindow().setDecorFitsSystemWindows(false);
        }
        renderHome();
    }

    @Override
    protected void onResume() {
        super.onResume();
        NotificationFeed.attach(this);
        WeatherJobService.schedule(this);
        if (!editing) renderHome();
    }

    @Override protected void onDestroy() {
        NotificationFeed.detach(this);
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode==REQUEST_LOCATION && hasWeatherLocationPermission()) loadWeather(true);
        if (requestCode==REQUEST_WEATHER_NOTIFICATIONS) {
            if (results.length>0 && results[0]==PackageManager.PERMISSION_GRANTED) {
                WeatherJobService.schedule(this);
                WeatherData.Report saved=WeatherData.saved(this);
                if (saved!=null) WeatherData.notifyWeather(this,saved);
            } else WeatherData.setAlerts(this,false);
            if (!editing) renderHome();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        editing = false;
        renderHome();
    }

    @Override
    public void onBackPressed() {
        if (editing) {
            editing = false;
            renderHome();
        } // En el inicio la tecla Atrás no cierra el launcher.
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private TextView text(String value, float size, int color, Typeface face) {
        TextView v = new TextView(this);
        v.setText(value);
        v.setTextSize(size);
        v.setTextColor(color);
        if (face != null) v.setTypeface(face);
        v.setGravity(Gravity.CENTER_VERTICAL);
        v.setIncludeFontPadding(false);
        return v;
    }

    private Typeface light() { return Typeface.create("sans-serif-light", Typeface.NORMAL); }
    private Typeface medium() { return Typeface.create("sans-serif-medium", Typeface.NORMAL); }

    private LinearLayout vertical() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    private LinearLayout horizontal() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL);
        return l;
    }

    private LinearLayout.LayoutParams lp(int w, int h) {
        return new LinearLayout.LayoutParams(w < 0 ? w : dp(w), h < 0 ? h : dp(h));
    }

    private void gap(LinearLayout parent, int height) {
        parent.addView(new View(this), lp(1, height));
    }

    private void divider(LinearLayout parent) {
        View v = new View(this);
        v.setBackgroundColor(BORDER);
        parent.addView(v, lp(-1, 1));
    }

    private GradientDrawable outline(int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(BLACK);
        d.setCornerRadius(dp(radius));
        d.setStroke(dp(1), BORDER);
        return d;
    }

    private void ripple(View v, GradientDrawable background) {
        v.setBackground(new RippleDrawable(ColorStateList.valueOf(0x44FFFFFF), background, null));
    }

    /** Aplica las barras del sistema para Android 15+ con pantalla de borde a borde. */
    private LinearLayout root() {
        LinearLayout root = vertical();
        root.setBackgroundColor(BLACK);
        root.setPadding(dp(25), dp(30), dp(25), dp(20));
        if (Build.VERSION.SDK_INT >= 30) {
            root.setOnApplyWindowInsetsListener((view, insets) -> {
                android.graphics.Insets bars = insets.getInsets(WindowInsets.Type.systemBars());
                view.setPadding(dp(25), bars.top + dp(19), dp(25), bars.bottom + dp(16));
                return insets;
            });
        }
        setContentView(root);
        return root;
    }

    private Set<String> selectedPackages() {
        LinkedHashSet<String> values = new LinkedHashSet<>();
        String json = getSharedPreferences(PREFS, MODE_PRIVATE).getString(KEY_SELECTION, "[]");
        try {
            JSONArray saved = new JSONArray(json);
            for (int i = 0; i < saved.length(); i++) {
                String value = saved.optString(i, "");
                if (!value.isEmpty()) values.add(value);
            }
        } catch (JSONException ignored) {
            // Datos corruptos o versión anterior: lista vacía de forma segura.
        }
        return values;
    }

    private void savePackages(Set<String> values) {
        JSONArray array = new JSONArray();
        for (String pkg : values) array.put(pkg);
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(KEY_SELECTION, array.toString()).apply();
    }

    @SuppressWarnings("deprecation")
    private List<AppEntry> installedApps() {
        PackageManager pm = getPackageManager();
        Intent launcher = new Intent(Intent.ACTION_MAIN);
        launcher.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> candidates;
        if (Build.VERSION.SDK_INT >= 33) {
            candidates = pm.queryIntentActivities(launcher, PackageManager.ResolveInfoFlags.of(0));
        } else {
            candidates = pm.queryIntentActivities(launcher, 0);
        }
        Map<String, AppEntry> unique = new HashMap<>();
        for (ResolveInfo info : candidates) {
            if (info.activityInfo == null) continue;
            String pkg = info.activityInfo.packageName;
            if (pkg.equals(getPackageName())) continue;
            String label = info.loadLabel(pm).toString().trim();
            if (label.isEmpty()) label = pkg;
            unique.putIfAbsent(pkg, new AppEntry(pkg, label,
                new ComponentName(pkg, info.activityInfo.name)));
        }
        ArrayList<AppEntry> apps = new ArrayList<>(unique.values());
        Collections.sort(apps, Comparator.comparing(a -> a.label.toLowerCase(new Locale("es", "CR"))));
        return apps;
    }

    private int batteryPercent() {
        Intent sticky = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (sticky == null) return -1;
        int level = sticky.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = sticky.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        return level >= 0 && scale > 0 ? Math.round(level * 100f / scale) : -1;
    }

    private boolean isDefaultHome() {
        if (Build.VERSION.SDK_INT >= 29) {
            RoleManager manager = (RoleManager) getSystemService(Context.ROLE_SERVICE);
            return manager != null && manager.isRoleAvailable(RoleManager.ROLE_HOME)
                && manager.isRoleHeld(RoleManager.ROLE_HOME);
        }
        return false;
    }

    private void requestDefaultHome() {
        try {
            if (Build.VERSION.SDK_INT >= 29) {
                RoleManager roleManager = (RoleManager) getSystemService(Context.ROLE_SERVICE);
                if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                    startActivityForResult(roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME), 10);
                    return;
                }
            }
            startActivity(new Intent(Settings.ACTION_HOME_SETTINGS));
        } catch (Exception e) {
            Toast.makeText(this, "Elegí Matte Launcher en Ajustes > Aplicación de inicio", Toast.LENGTH_LONG).show();
        }
    }

    private TextView action(String title, Runnable callback) {
        TextView v = text(title, 12, WHITE, medium());
        v.setLetterSpacing(0.10f);
        v.setGravity(Gravity.CENTER);
        ripple(v, outline(12));
        v.setClickable(true);
        v.setFocusable(true);
        v.setContentDescription(title);
        v.setOnClickListener(view -> callback.run());
        return v;
    }

    private void renderHome() {
        editing = false;
        LinearLayout r = root();
        LinearLayout top = horizontal();
        TextView brand = text("M A T T E", 11, WHITE, medium());
        top.addView(brand, new LinearLayout.LayoutParams(0, dp(30), 1));
        int percent = batteryPercent();
        TextView battery = text(percent < 0 ? "" : percent + "%", 13, MUTED, light());
        top.addView(battery, lp(-2, 30));
        r.addView(top, lp(-1, 36));
        gap(r, 16);

        TextClock weekday = new TextClock(this);
        weekday.setFormat12Hour("EEEE");
        weekday.setFormat24Hour("EEEE");
        weekday.setTextColor(WHITE);
        weekday.setTextSize(36);
        weekday.setTypeface(medium());
        weekday.setAllCaps(true);
        weekday.setSingleLine(true);
        weekday.setIncludeFontPadding(false);
        r.addView(weekday, lp(-1, 50));

        TextClock clock = new TextClock(this);
        clock.setFormat12Hour("hh:mm");
        clock.setFormat24Hour("HH:mm");
        clock.setTextColor(WHITE);
        clock.setTextSize(52);
        clock.setTypeface(light());
        clock.setIncludeFontPadding(false);
        clock.setSingleLine(true);
        r.addView(clock, lp(-1, 66));

        TextClock date = new TextClock(this);
        date.setFormat12Hour("d 'de' MMMM");
        date.setFormat24Hour("d 'de' MMMM");
        date.setTextColor(MUTED);
        date.setTextSize(14);
        date.setTypeface(light());
        date.setAllCaps(false);
        r.addView(date, lp(-1, 27));
        gap(r, 12);
        addWeatherPanel(r);
        gap(r, 14);
        divider(r);
        gap(r, 13);

        List<AppEntry> all = installedApps();
        Set<String> saved = selectedPackages();
        List<AppEntry> visible = new ArrayList<>();
        for (AppEntry a : all) if (saved.contains(a.packageName)) visible.add(a);
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setVerticalScrollBarEnabled(false);
        LinearLayout list = vertical();
        TextView noticeTitle = text("VISTAS PREVIAS", 12, WHITE, medium());
        noticeTitle.setLetterSpacing(0.14f);
        list.addView(noticeTitle, lp(-1, 30));
        previewContainer=vertical();
        list.addView(previewContainer, lp(-1,-2));
        refreshPreviews();
        gap(list, 14);
        divider(list);
        gap(list, 10);
        LinearLayout heading = horizontal();
        TextView title = text("MIS APLICACIONES", 12, WHITE, medium());
        title.setLetterSpacing(0.14f);
        heading.addView(title, new LinearLayout.LayoutParams(0, dp(28), 1));
        heading.addView(text(String.valueOf(visible.size()), 13, MUTED, light()), lp(-2, 28));
        list.addView(heading, lp(-1, 30));
        gap(list, 8);
        if (visible.isEmpty()) {
            gap(list, 42);
            TextView none = text("Todavía no elegiste aplicaciones.", 16, MUTED, light());
            list.addView(none, lp(-1, 30));
            gap(list, 10);
            TextView hint = text("Tocá EDITAR APLICACIONES para agregar las que querás ver aquí.", 13, MUTED, light());
            hint.setLineSpacing(dp(4), 1f);
            list.addView(hint, lp(-1, -2));
        } else {
            for (AppEntry entry : visible) {
                LinearLayout row = horizontal();
                row.setContentDescription("Abrir " + entry.label);
                row.setClickable(true);
                row.setFocusable(true);
                row.setOnClickListener(v -> launch(entry));
                ImageView appIcon = appIcon(entry.packageName);
                row.addView(appIcon, lp(36, 36));
                TextView name = text(entry.label, 15, WHITE, light());
                LinearLayout.LayoutParams nameLp = new LinearLayout.LayoutParams(0, dp(48), 1);
                nameLp.leftMargin = dp(15);
                row.addView(name, nameLp);
                row.addView(text("›", 20, MUTED, light()), lp(15, 48));
                list.addView(row, lp(-1, 52));
            }
        }
        scroll.addView(list);
        r.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        gap(r, 15);
        TextView edit = action("＋  EDITAR APLICACIONES", this::showPicker);
        r.addView(edit, lp(-1, 54));
        if (!isDefaultHome()) {
            gap(r, 12);
            TextView setHome = action("PONER COMO INICIO", this::requestDefaultHome);
            r.addView(setHome, lp(-1, 48));
        }
    }


    private ImageView appIcon(String packageName) {
        ImageView icon = new ImageView(this);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        icon.setPadding(dp(4),dp(4),dp(4),dp(4));
        android.graphics.ColorMatrix grayscale = new android.graphics.ColorMatrix();
        grayscale.setSaturation(0f);
        icon.setColorFilter(new android.graphics.ColorMatrixColorFilter(grayscale));
        icon.setBackground(outline(12));
        try { icon.setImageDrawable(getPackageManager().getApplicationIcon(packageName)); }
        catch (PackageManager.NameNotFoundException ignored) {
            icon.setImageResource(android.R.drawable.sym_def_app_icon);
        }
        icon.setContentDescription("Icono de "+packageName);
        return icon;
    }

    private boolean hasWeatherLocationPermission() {
        return checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED;
    }

    private void addWeatherPanel(LinearLayout root) {
        LinearLayout panel=vertical();
        panel.setPadding(dp(13),dp(10),dp(13),dp(10));
        panel.setBackground(outline(12));
        LinearLayout first=horizontal();
        weatherCurrent=text("CLIMA  ·  SIN UBICACIÓN",15,WHITE,medium());
        first.addView(weatherCurrent,new LinearLayout.LayoutParams(0,dp(29),1));
        TextView update=text("↻",23,MUTED,light());
        update.setGravity(Gravity.CENTER);
        update.setContentDescription("Actualizar clima");
        update.setOnClickListener(v -> loadWeather(true));
        first.addView(update,lp(36,32));
        panel.addView(first,lp(-1,32));
        weatherDetails=text("Tocá para activar el clima local",12,MUTED,light());
        panel.addView(weatherDetails,lp(-1,23));
        LinearLayout buttons=horizontal();
        TextView loc=action(hasWeatherLocationPermission()?"ACTUALIZAR":"ACTIVAR CLIMA", ()->loadWeather(true));
        LinearLayout.LayoutParams locLp=new LinearLayout.LayoutParams(0,dp(35),1);
        locLp.rightMargin=dp(7);
        buttons.addView(loc,locLp);
        TextView alerts=action(WeatherData.alerts(this)?"AVISOS ACTIVADOS":"AVISOS DEL CLIMA",this::toggleWeatherAlerts);
        buttons.addView(alerts,new LinearLayout.LayoutParams(0,dp(35),1));
        panel.addView(buttons,lp(-1,35));
        root.addView(panel,lp(-1,-2));
        updateWeatherView();
        if (hasWeatherLocationPermission()) loadWeather(false);
    }

    private void updateWeatherView() {
        if (weatherCurrent==null || weatherDetails==null) return;
        WeatherData.Report data=WeatherData.saved(this);
        if (data!=null) {
            weatherCurrent.setText(String.format(Locale.forLanguageTag("es-CR"),
                "%s  ·  %.0f °C",data.description(),data.temperature));
            String staleness=System.currentTimeMillis()-data.timestamp>2*60*60*1000L?" · sin actualizar":"";
            weatherDetails.setText(String.format(Locale.forLanguageTag("es-CR"),
                "Sensación térmica %.0f °C%s",data.apparent,staleness));
        } else {
            weatherCurrent.setText("CLIMA");
            weatherDetails.setText(hasWeatherLocationPermission()?"Obteniendo clima local…":"Activá la ubicación aproximada");
        }
    }

    private void loadWeather(boolean force) {
        if (weatherLoading) return;
        if (!hasWeatherLocationPermission()) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},REQUEST_LOCATION);
            return;
        }
        android.content.SharedPreferences p=WeatherData.prefs(this);
        long lastLocation=p.getLong("location_checked",0);
        if (!WeatherData.hasCoords(this) || force ||
            System.currentTimeMillis()-lastLocation > 60L*60*1000) {
            LocationManager lm=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
            if (lm!=null) {
                try {
                    Location known=lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (known==null) known=lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                    if (known!=null) {
                        WeatherData.saveCoords(this,known.getLatitude(),known.getLongitude());
                        p.edit().putLong("location_checked",System.currentTimeMillis()).apply();
                    }
                    if (!WeatherData.hasCoords(this)) {
                        if (!lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                            if (force) Toast.makeText(this,"Activá la ubicación del teléfono",Toast.LENGTH_LONG).show();
                            return;
                        }
                        lm.getCurrentLocation(LocationManager.NETWORK_PROVIDER,null,getMainExecutor(),location -> {
                            if (location==null) {
                                if (force) Toast.makeText(this,"Ubicación aún no disponible",Toast.LENGTH_LONG).show();
                                return;
                            }
                            WeatherData.saveCoords(this,location.getLatitude(),location.getLongitude());
                            p.edit().putLong("location_checked",System.currentTimeMillis()).apply();
                            WeatherJobService.schedule(this);
                            fetchWeather(force);
                        });
                        return;
                    }
                } catch (SecurityException ex) { return; }
            }
        }
        fetchWeather(force);
    }

    private void fetchWeather(boolean force) {
        WeatherData.Report existing=WeatherData.saved(this);
        if (!force && existing!=null && System.currentTimeMillis()-existing.timestamp<WeatherData.REFRESH_MS) {
            updateWeatherView(); return;
        }
        if (weatherLoading || !WeatherData.hasCoords(this)) return;
        weatherLoading=true;
        if (weatherDetails!=null) weatherDetails.setText("Actualizando condiciones…");
        new Thread(() -> {
            boolean success=true;
            try { WeatherData.fetch(getApplicationContext(),false); }
            catch (Exception ignored) { success=false; }
            final boolean ok=success;
            runOnUiThread(() -> {
                weatherLoading=false;
                if (!editing) updateWeatherView();
                if (!ok && force) Toast.makeText(this,"Sin conexión con el servicio del clima",Toast.LENGTH_SHORT).show();
            });
        },"matte-weather-ui").start();
    }

    private void toggleWeatherAlerts() {
        if (!hasWeatherLocationPermission()) {
            Toast.makeText(this,"Activá primero el clima local",Toast.LENGTH_SHORT).show();
            loadWeather(true);
            return;
        }
        if (!WeatherData.hasCoords(this)) {
            loadWeather(true);
            Toast.makeText(this,"Esperá la ubicación y volvé a tocar avisos",Toast.LENGTH_LONG).show();
            return;
        }
        boolean desired=!WeatherData.alerts(this);
        WeatherData.setAlerts(this,desired);
        WeatherJobService.schedule(this);
        if (desired && Build.VERSION.SDK_INT>=33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},REQUEST_WEATHER_NOTIFICATIONS);
        } else if (desired) {
            WeatherData.Report report=WeatherData.saved(this);
            if (report!=null) WeatherData.notifyWeather(this,report);
        }
        if (!editing) renderHome();
    }

    void refreshPreviews() {
        if (editing || previewContainer==null) return;
        if (!BuildConfig.MESSAGE_PREVIEWS) {
            previewContainer.removeAllViews();
            previewContainer.addView(text("Vista Lite: sin acceso a tus mensajes.",12,MUTED,light()),lp(-1,34));
            return;
        }
        previewContainer.removeAllViews();
        if (!NotificationFeed.accessGranted(this)) {
            TextView grant=action("ACTIVAR ACCESO A NOTIFICACIONES",()->{
                Intent settings=new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                startActivity(settings);
            });
            previewContainer.addView(grant,lp(-1,49));
            gap(previewContainer,7);
            TextView note=text("Para Teams, WhatsApp y Outlook. Solo se leen avisos activos.",12,MUTED,light());
            previewContainer.addView(note,lp(-1,38));
            return;
        }
        addPreview("teams","Teams","com.microsoft.teams");
        addPreview("whatsapp","WhatsApp","com.whatsapp");
        addPreview("outlook","Outlook","com.microsoft.office.outlook");
    }

    private void addPreview(String group, String label, String packageName) {
        NotificationFeed.Preview notification=NotificationFeed.latest(group);
        LinearLayout row=horizontal();
        row.addView(appIcon(packageName),lp(36,36));
        LinearLayout texts=vertical();
        TextView title=text(label+(notification==null?"":"  ·  "+notification.title),13,WHITE,medium());
        title.setSingleLine(true);
        title.setEllipsize(android.text.TextUtils.TruncateAt.END);
        texts.addView(title,lp(-1,20));
        TextView snippet=text(notification==null?"Sin avisos activos":notification.message,12,MUTED,light());
        snippet.setSingleLine(true);
        snippet.setEllipsize(android.text.TextUtils.TruncateAt.END);
        texts.addView(snippet,lp(-1,21));
        LinearLayout.LayoutParams tLp=new LinearLayout.LayoutParams(0,dp(45),1);
        tLp.leftMargin=dp(12);
        row.addView(texts,tLp);
        row.setContentDescription("Abrir "+label);
        row.setClickable(true);
        row.setOnClickListener(v -> {
            if (notification!=null && notification.action!=null) {
                try { notification.action.send(); return; }
                catch (android.app.PendingIntent.CanceledException ignored) {}
            }
            Intent app=getPackageManager().getLaunchIntentForPackage(packageName);
            if (app==null && group.equals("whatsapp"))
                app=getPackageManager().getLaunchIntentForPackage("com.whatsapp.w4b");
            if (app!=null) startActivity(app);
        });
        previewContainer.addView(row,lp(-1,52));
        divider(previewContainer);
    }

    private String firstLetter(String label) {
        return label.isEmpty() ? "•" : label.substring(0, label.offsetByCodePoints(0, 1)).toUpperCase(Locale.ROOT);
    }

    private void launch(AppEntry entry) {
        try {
            Intent launch = getPackageManager().getLaunchIntentForPackage(entry.packageName);
            if (launch == null) {
                launch = Intent.makeMainActivity(entry.activity);
            }
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            startActivity(launch);
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo abrir " + entry.label, Toast.LENGTH_SHORT).show();
        }
    }

    private void showPicker() {
        editing = true;
        List<AppEntry> installed = installedApps();
        LinkedHashSet<String> draft = new LinkedHashSet<>(selectedPackages());
        LinearLayout r = root();
        TextView header = text("EDITAR APLICACIONES", 19, WHITE, medium());
        r.addView(header, lp(-1, 45));
        TextView sub = text("Seleccioná solo las aplicaciones que querés mostrar.", 13, MUTED, light());
        r.addView(sub, lp(-1, 52));
        gap(r, 12);

        EditText search = new EditText(this);
        search.setSingleLine(true);
        search.setHint("Buscar aplicación");
        search.setTextColor(WHITE);
        search.setHintTextColor(MUTED);
        search.setTextSize(16);
        search.setPadding(dp(16), 0, dp(16), 0);
        search.setBackground(outline(12));
        search.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        r.addView(search, lp(-1, 52));
        gap(r, 17);

        ScrollView scroll = new ScrollView(this);
        scroll.setVerticalScrollBarEnabled(false);
        LinearLayout appRows = vertical();
        scroll.addView(appRows);
        r.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        Runnable refresh = () -> {
            String filter = search.getText().toString().trim().toLowerCase(Locale.ROOT);
            appRows.removeAllViews();
            int found = 0;
            for (AppEntry app : installed) {
                if (!app.label.toLowerCase(Locale.ROOT).contains(filter)
                    && !app.packageName.toLowerCase(Locale.ROOT).contains(filter)) continue;
                found++;
                LinearLayout selectRow = horizontal();
                selectRow.addView(appIcon(app.packageName), lp(38,38));
                CheckBox check = new CheckBox(this);
                check.setText(app.label);
                check.setTextColor(WHITE);
                check.setTextSize(16);
                check.setTypeface(light());
                check.setGravity(Gravity.CENTER_VERTICAL);
                check.setButtonTintList(ColorStateList.valueOf(WHITE));
                check.setChecked(draft.contains(app.packageName));
                check.setOnCheckedChangeListener((button, checked) -> {
                    if (checked) draft.add(app.packageName);
                    else draft.remove(app.packageName);
                });
                LinearLayout.LayoutParams checkWidth = new LinearLayout.LayoutParams(0, dp(57), 1);
                checkWidth.leftMargin=dp(10);
                selectRow.addView(check, checkWidth);
                appRows.addView(selectRow, lp(-1,57));
                divider(appRows);
            }
            if (found == 0) {
                gap(appRows, 24);
                appRows.addView(text("No hay coincidencias.", 15, MUTED, light()), lp(-1, 40));
            }
        };
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { refresh.run(); }
            @Override public void afterTextChanged(Editable e) {}
        });
        refresh.run();
        gap(r, 18);
        LinearLayout actions = horizontal();
        TextView cancel = action("CANCELAR", () -> { editing = false; renderHome(); });
        TextView save = action("GUARDAR", () -> {
            savePackages(draft);
            editing = false;
            renderHome();
        });
        LinearLayout.LayoutParams left = new LinearLayout.LayoutParams(0, dp(54), 1);
        left.rightMargin = dp(9);
        actions.addView(cancel, left);
        LinearLayout.LayoutParams right = new LinearLayout.LayoutParams(0, dp(54), 1);
        right.leftMargin = dp(9);
        actions.addView(save, right);
        r.addView(actions, lp(-1, 54));
        search.clearFocus();
    }
}
