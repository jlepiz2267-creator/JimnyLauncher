package com.jimny.launcher

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.FrameLayout
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    private val background = Color.rgb(18, 13, 10)
    private val panel = Color.rgb(77, 39, 19)
    private val red = Color.rgb(255, 161, 55)
    private val muted = Color.rgb(255, 211, 173)

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
        brand.addView(text("DRIVE  /  LAUNCHER", 11f, red, true))
        header.addView(brand, LinearLayout.LayoutParams(0, -2, 1f))
        header.addView(text("●  SISTEMA LISTO", 12f, muted))
        root.addView(header, LinearLayout.LayoutParams(-1, dp(66)))

        val body = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val navigation = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        navigation.addView(text("HOLA, JUAN", 23f, Color.WHITE, true))
        navigation.addView(text("Tu Suzuki Jimny JB74", 12f, muted))
        navigation.addView(text("NAVEGACIÓN", 12f, red, true))
        navigation.addView(appCard("↗", "Google Maps", "Abrir navegación", "com.google.android.apps.maps", panel),
            LinearLayout.LayoutParams(-1, 0, 1f).apply { topMargin = dp(10) })
        navigation.addView(appCard("➤", "Waze", "Rutas y tráfico", "com.waze", panel),
            LinearLayout.LayoutParams(-1, 0, 1f).apply { topMargin = dp(10) })
        navigation.background = gradient(Color.rgb(189, 91, 23), Color.rgb(80, 37, 17))
        navigation.setPadding(dp(14), dp(12), dp(14), dp(12))
        body.addView(navigation, LinearLayout.LayoutParams(0, -1, 1.12f).apply { rightMargin = dp(12) })

        val music = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(22), dp(16), dp(22), dp(16))
            background = gradient(Color.rgb(215, 104, 26), Color.rgb(91, 35, 15))
            isClickable = true
            setOnClickListener { openApp("YouTube Music", "com.google.android.apps.youtube.music") }
        }
        music.addView(text("♫", 47f, red, true))
        music.addView(text("TU MÚSICA", 11f, muted, true))
        music.addView(text("YouTube Music", 24f, Color.WHITE, true))
        music.addView(text("Tocá para abrir el reproductor  ↗", 12f, muted))
        body.addView(music, LinearLayout.LayoutParams(0, -1, 1.12f).apply { rightMargin = dp(12) })

        val info = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(14), dp(12), dp(14), dp(12))
            background = gradient(Color.rgb(142, 63, 19), Color.rgb(53, 29, 20))
        }
        info.addView(text("HORA LOCAL", 11f, muted, true))
        val clock = text("", 37f, Color.WHITE, true)
        clock.gravity = Gravity.CENTER
        info.addView(clock)
        val date = text("", 15f, muted)
        date.gravity = Gravity.CENTER
        info.addView(date)
        info.addView(text("SUZUKI  •  JB74", 12f, red, true).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(15), 0, 0)
        })
        body.addView(info, LinearLayout.LayoutParams(0, -1, 0.95f))
        root.addView(body, LinearLayout.LayoutParams(-1, 0, 1f))

        val footer = text("JIMNY LAUNCHER  /  V0.3      •      Usar aplicaciones de video solo estacionado", 10f, muted)
        footer.gravity = Gravity.CENTER
        root.addView(footer, LinearLayout.LayoutParams(-1, dp(34)))

        val container = FrameLayout(this)
        container.addView(root, FrameLayout.LayoutParams(-1, -1))
        val welcome = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(this@MainActivity.background)
            addView(text("◆  SUZUKI JIMNY  ◆", 16f, red, true).apply {
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
                clock.postDelayed(this, 30_000)
            }
        }
        update.run()
    }
}
