package com.jimny.launcher

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.FrameLayout
import android.content.Intent
import android.net.Uri
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.ImageView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    private val background = Color.rgb(18, 13, 10)
    private val orange = Color.rgb(255, 161, 55)
    private val muted = Color.rgb(255, 211, 173)

    private val timeHandler = Handler(Looper.getMainLooper())
    private var timeUpdater: Runnable? = null
    private var mapView: WebView? = null
    private var mapPage: LinearLayout? = null
    private var dashboard: LinearLayout? = null
    private var carImage: ImageView? = null
    private val carImagePrefs by lazy { getSharedPreferences("jimny_display", MODE_PRIVATE) }
    private val photoRequestCode = 451

    private fun chooseOriginalCarPhoto() {
        val picker = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        try {
            startActivityForResult(picker, photoRequestCode)
        } catch (e: Exception) {
            Toast.makeText(this, "No fue posible abrir la galería", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayOriginalCarPhoto(uri: Uri): Boolean {
        return try {
            val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
            if (opts.outWidth <= 0 || opts.outHeight <= 0) return false
            var sampleSize = 1
            while (opts.outWidth / sampleSize > 1600 || opts.outHeight / sampleSize > 1200) {
                sampleSize *= 2
            }
            val loadOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            val bitmap = contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, loadOpts)
            } ?: return false
            carImage?.setImageBitmap(bitmap)
            carImage?.contentDescription = "Fotografía original del Suzuki Jimny elegida por el usuario"
            true
        } catch (_: Exception) {
            false
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != photoRequestCode || resultCode != RESULT_OK) return
        val uri = data?.data ?: return
        if (!displayOriginalCarPhoto(uri)) {
            Toast.makeText(this, "No se pudo leer la foto. Probá con JPG o PNG.", Toast.LENGTH_LONG).show()
            return
        }
        try {
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (_: SecurityException) {
            Toast.makeText(this, "La foto podría requerir seleccionarse otra vez", Toast.LENGTH_SHORT).show()
        }
        carImagePrefs.edit().putString("chosen_original_car_uri", uri.toString()).apply()
    }


    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (mapPage?.visibility == View.VISIBLE) {
            showDashboard()
        } else {
            super.onBackPressed()
        }
    }

    private fun showDashboard() {
        mapPage?.visibility = View.GONE
        dashboard?.visibility = View.VISIBLE
    }
    private fun showMap() {
        dashboard?.visibility = View.GONE
        mapPage?.visibility = View.VISIBLE
        val web = mapView ?: return
        if (web.url == null) web.loadUrl("file:///android_asset/map.html")
    }

    override fun onDestroy() {
        timeUpdater?.let { timeHandler.removeCallbacks(it) }
        mapView?.destroy()
        mapView = null
        super.onDestroy()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun rounded(color: Int, radius: Int = 20): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
        }

    private fun gradient(top: Int, bottom: Int): GradientDrawable =
        GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(top, bottom)).apply {
            cornerRadius = dp(22).toFloat()
            setStroke(dp(1), Color.rgb(160, 85, 35))
        }

    private fun text(value: String, size: Float, color: Int = Color.WHITE, bold: Boolean = false): TextView =
        TextView(this).apply {
            this.text = value
            textSize = size
            setTextColor(color)
            gravity = Gravity.CENTER_VERTICAL
            if (bold) typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
        }

    private fun openApp(name: String, pkg: String) {
        val intent: Intent? = packageManager.getLaunchIntentForPackage(pkg)
        if (intent != null) {
            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "No se pudo abrir $name", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "$name no está instalado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun appCard(symbol: String, title: String, subtitle: String, pkg: String, color: Int): LinearLayout =
        LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(20), dp(12), dp(14), dp(12))
            background = rounded(color, 18)
            addView(text(symbol, 22f))
            addView(text(title, 19f, Color.WHITE, true))
            addView(text(subtitle, 11f, Color.rgb(224, 227, 232)))
            isClickable = true
            setOnClickListener { openApp(title, pkg) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = background
        window.navigationBarColor = background

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(12), dp(22), dp(14))
            background = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(Color.rgb(52, 26, 13), this@MainActivity.background))
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val brand = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        brand.addView(text("JIMNY  /  DRIVE", 27f, Color.WHITE, true))
        brand.addView(text("DRIVE  /  LAUNCHER", 11f, orange, true))
        header.addView(brand, LinearLayout.LayoutParams(0, -2, 1f))
        header.addView(text("●  SISTEMA LISTO", 12f, muted))
        root.addView(header, LinearLayout.LayoutParams(-1, dp(66)))

        val body = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        fun card(top: Int, bottom: Int) = gradient(top, bottom)
        fun column(): LinearLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(14), dp(12), dp(14), dp(12))
        }

        val left = column().apply {
            background = card(Color.rgb(232, 115, 21), Color.rgb(104, 43, 12))
            addView(text("Hola, Juan", 28f, Color.WHITE, true))
            addView(text("Bienvenido a tu Jimny", 13f, Color.WHITE))
            addView(text("SUZUKI JIMNY JB74", 12f, muted, true))
            val photoView = ImageView(this@MainActivity).apply {
                setImageResource(R.drawable.jimny_white)
                scaleType = ImageView.ScaleType.FIT_CENTER
                adjustViewBounds = true
                contentDescription = "Suzuki Jimny blanco; podés elegir la foto original"
            }
            carImage = photoView
            addView(photoView, LinearLayout.LayoutParams(-1, 0, 1f))
            val saved = carImagePrefs.getString("chosen_original_car_uri", null)
            if (saved != null && !displayOriginalCarPhoto(Uri.parse(saved))) {
                carImagePrefs.edit().remove("chosen_original_car_uri").apply()
            }
            addView(text("TU JIMNY ORIGINAL", 13f, Color.WHITE, true))
            addView(text("▣  ELEGIR FOTO DE MI JIMNY", 12f, Color.WHITE, true).apply {
                setPadding(dp(8), dp(10), dp(8), dp(10))
                background = rounded(Color.rgb(100, 46, 19), 10)
                isClickable = true
                contentDescription = "Elegir fotografía original del Jimny desde archivos"
                setOnClickListener { chooseOriginalCarPhoto() }
            })
            addView(text("Foto privada en tu teléfono · sin acceso general a galería", 10f, muted))
        }
        body.addView(left, LinearLayout.LayoutParams(0, -1, 1.2f).apply { rightMargin = dp(10) })

        val center = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val clockPanel = column().apply {
            background = card(Color.rgb(93, 43, 19), Color.rgb(43, 24, 17))
        }
        val clock = text("", 36f, Color.WHITE, true).apply { gravity = Gravity.CENTER }
        val date = text("", 12f, muted).apply { gravity = Gravity.CENTER }
        clockPanel.addView(clock)
        clockPanel.addView(date)
        center.addView(clockPanel, LinearLayout.LayoutParams(-1, 0, 0.9f))
        center.addView(appCard("↗", "Google Maps", "Abrir navegación", "com.google.android.apps.maps", Color.rgb(141, 65, 22)),
            LinearLayout.LayoutParams(-1, 0, 1f).apply { topMargin = dp(8) })
        center.addView(appCard("➤", "Waze", "Rutas y tráfico", "com.waze", Color.rgb(116, 49, 19)),
            LinearLayout.LayoutParams(-1, 0, 1f).apply { topMargin = dp(8) })
        body.addView(center, LinearLayout.LayoutParams(0, -1, 0.95f).apply { rightMargin = dp(10) })

        val music = column().apply {
            background = card(Color.rgb(197, 87, 18), Color.rgb(59, 28, 16))
            isClickable = true
            setOnClickListener { openApp("YouTube Music", "com.google.android.apps.youtube.music") }
        }
        music.addView(text("♫  YouTube Music", 21f, Color.WHITE, true))
        music.addView(text("♪", 74f, orange, true).apply { gravity = Gravity.CENTER },
            LinearLayout.LayoutParams(-1, 0, 1f))
        music.addView(text("TU MÚSICA", 16f, Color.WHITE, true))
        music.addView(text("Abrir YouTube Music  ↗", 13f, muted))
        music.addView(text("Abrir en el teléfono · sin permisos sensibles", 10f, muted))
        body.addView(music, LinearLayout.LayoutParams(0, -1, 1.1f))
        root.addView(body, LinearLayout.LayoutParams(-1, 0, 1f))
        dashboard = body

        // El mapa se muestra solo cuando se solicita: el panel naranja
        // original es siempre la pantalla de inicio.
        val mapLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
            background = rounded(Color.rgb(29, 22, 18), 18)
            setPadding(dp(12), dp(8), dp(12), dp(12))
        }
        val mapHeader = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val returnButton = text("‹  VOLVER AL PANEL", 14f, orange, true).apply {
            setPadding(dp(10), dp(10), dp(18), dp(10))
            isClickable = true
            setOnClickListener { showDashboard() }
        }
        mapHeader.addView(returnButton)
        mapHeader.addView(text("MAPA · OpenStreetMap", 14f, Color.WHITE, true))
        mapLayout.addView(mapHeader, LinearLayout.LayoutParams(-1, dp(48)))
        val embeddedMap = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webViewClient = WebViewClient()
            setBackgroundColor(Color.rgb(30, 25, 21))
            contentDescription = "Mapa interactivo en el panel Jimny"
        }
        mapView = embeddedMap
        mapLayout.addView(embeddedMap, LinearLayout.LayoutParams(-1, 0, 1f))
        val mapLinks = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        mapLinks.addView(appCard("↗", "Google Maps", "Navegación", "com.google.android.apps.maps", Color.rgb(141, 65, 22)),
            LinearLayout.LayoutParams(0, dp(58), 1f).apply { rightMargin = dp(6) })
        mapLinks.addView(appCard("➤", "Waze", "Tráfico", "com.waze", Color.rgb(116, 49, 19)),
            LinearLayout.LayoutParams(0, dp(58), 1f).apply { leftMargin = dp(6) })
        mapLayout.addView(mapLinks, LinearLayout.LayoutParams(-1, dp(58)).apply { topMargin = dp(8) })
        root.addView(mapLayout, LinearLayout.LayoutParams(-1, 0, 1f))
        mapPage = mapLayout

        val footer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        footer.addView(text("⌖  VER MAPA", 12f, orange, true).apply {
            setPadding(dp(12), dp(5), dp(12), dp(5))
            isClickable = true
            contentDescription = "Mostrar mapa integrado"
            setOnClickListener { showMap() }
        })
        footer.addView(text("      •      ", 12f, orange))
        footer.addView(text("♫  MÚSICA", 12f, muted))
        footer.addView(text("      •      ", 12f, orange))
        footer.addView(text("JIMNY LAUNCHER V0.8.1", 12f, muted))
        root.addView(footer, LinearLayout.LayoutParams(-1, dp(36)))

        val container = FrameLayout(this)
        container.addView(root, FrameLayout.LayoutParams(-1, -1))
        val welcome = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(this@MainActivity.background)
            addView(text("◆  SUZUKI JIMNY  ◆", 16f, orange, true).apply {
                gravity = Gravity.CENTER
            })
            addView(text("BIENVENIDO, JUAN", 34f, Color.WHITE, true).apply {
                gravity = Gravity.CENTER
                setPadding(dp(12), dp(20), dp(12), dp(12))
            })
            addView(text("JIMNY LAUNCHER", 15f, muted, true).apply {
                gravity = Gravity.CENTER
            })
        }
        container.addView(welcome, FrameLayout.LayoutParams(-1, -1))
        setContentView(container)
        Handler(Looper.getMainLooper()).postDelayed({
            if (!isFinishing && !isDestroyed) {
                welcome.animate().alpha(0f).setDuration(450).withEndAction {
                    container.removeView(welcome)
                }.start()
            }
        }, 2600)

        val timeFormat = SimpleDateFormat("HH:mm", Locale("es", "CR"))
        val dateFormat = SimpleDateFormat("EEE d MMM yyyy", Locale("es", "CR"))
        val update = object : Runnable {
            override fun run() {
                clock.text = timeFormat.format(Date())
                date.text = dateFormat.format(Date())
                timeHandler.postDelayed(this, 30_000)
            }
        }
        timeUpdater = update
        update.run()
    }
}
