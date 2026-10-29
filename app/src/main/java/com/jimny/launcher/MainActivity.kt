package com.jimny.launcher

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.BLACK
        window.navigationBarColor = Color.BLACK
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(24, 16, 24, 16)
            setBackgroundColor(Color.rgb(15, 17, 20))
        }
        fun label(value: String, size: Float): TextView = TextView(this).apply {
            text = value
            textSize = size
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        root.addView(label("JIMNY  •  LAUNCHER", 28f))
        val clock = label("", 24f)
        root.addView(clock)
        val formatter = SimpleDateFormat("EEE d MMM  •  HH:mm", Locale("es", "CR"))
        val ticker = object : Runnable {
            override fun run() {
                clock.text = formatter.format(Date())
                clock.postDelayed(this, 30_000)
            }
        }
        ticker.run()
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
        listOf(
            "MAPS" to "com.google.android.apps.maps",
            "WAZE" to "com.waze",
            "YOUTUBE MUSIC" to "com.google.android.apps.youtube.music"
        ).forEach { (name, pkg) ->
            val button = Button(this).apply {
                text = name
                setTextColor(Color.WHITE)
                setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(150, 30, 35)))
                setOnClickListener {
                    val intent: Intent? = packageManager.getLaunchIntentForPackage(pkg)
                    if (intent != null) startActivity(intent)
                    else Toast.makeText(this@MainActivity, "$name no está instalado", Toast.LENGTH_SHORT).show()
                }
            }
            row.addView(button, LinearLayout.LayoutParams(0, 88, 1f).apply { setMargins(8, 12, 8, 0) })
        }
        root.addView(row, LinearLayout.LayoutParams(-1, -2))
        setContentView(root)
    }
}
