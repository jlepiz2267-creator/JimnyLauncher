package com.jimny.launcher

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.view.Gravity
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    private val orange = Color.rgb(255, 159, 54)
    private val dark = Color.rgb(20, 16, 17)
    private val handler = Handler(Looper.getMainLooper())
    private var title: TextView? = null
    private var artist: TextView? = null
    private var status: TextView? = null
    private var clock: TextView? = null
    private var controller: MediaController? = null
    private var map: WebView? = null
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun label(s: String, size: Float = 16f, color: Int = Color.WHITE): TextView =
        TextView(this).apply { text=s; textSize=size; setTextColor(color); gravity=Gravity.CENTER_VERTICAL; setPadding(dp(8),dp(3),dp(8),dp(3)) }
    private fun panel(): LinearLayout = LinearLayout(this).apply {
        orientation=LinearLayout.VERTICAL; setPadding(dp(10),dp(10),dp(10),dp(10))
        background=GradientDrawable().apply { setColor(Color.rgb(49,32,27)); cornerRadius=dp(18).toFloat() }
    }
    private fun button(s: String, action: () -> Unit): Button = Button(this).apply {
        text=s; setAllCaps(false); setTextColor(Color.WHITE); setBackgroundColor(Color.rgb(119,62,30)); setOnClickListener { action() }
    }
    private fun open(pkg: String) {
        packageManager.getLaunchIntentForPackage(pkg)?.let { startActivity(it) }
    }
    private fun current(): MediaController? = try {
        val manager=getSystemService(MEDIA_SESSION_SERVICE) as MediaSessionManager
        val sessions=manager.getActiveSessions(ComponentName(this,MusicListener::class.java))
        sessions.firstOrNull { it.packageName=="com.google.android.apps.youtube.music" }
            ?: sessions.firstOrNull()
    } catch (_: SecurityException) { null }
    private fun refreshMusic() {
        controller=current()
        val c=controller
        val m=c?.metadata
        title?.text=m?.getString("android.media.metadata.TITLE") ?: "Sin reproducción activa"
        artist?.text=m?.getString("android.media.metadata.ARTIST") ?: "Abrí YouTube Music para empezar"
        status?.text=if(c==null) "Activá el acceso a notificaciones para controlar la música" else "Controles integrados · ${c.packageName}"
    }
    private val tick=object: Runnable {
        override fun run() {
            clock?.text=SimpleDateFormat("HH:mm · EEE d MMM",Locale("es","CR")).format(Date())
            refreshMusic()
            handler.postDelayed(this,3000)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor=dark; window.navigationBarColor=dark
        val root=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(dp(12),dp(8),dp(12),dp(8)); setBackgroundColor(dark) }
        val head=LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL }
        head.addView(label("◆ JIMNY / DRIVE  v0.6",22f,orange),LinearLayout.LayoutParams(0,dp(48),1f))
        clock=label("",14f)
        head.addView(clock)
        root.addView(head)
        val body=LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL }
        val left=panel()
        left.addView(label("⌂  CASA  /  TRABAJO",17f,orange))
        left.addView(label("Mapa integrado y controles de música",12f))
        val music=panel()
        music.addView(label("♫  REPRODUCTOR",19f,orange))
        title=label("Sin reproducción activa",18f)
        artist=label("",13f)
        status=label("",11f,orange)
        music.addView(title);music.addView(artist);music.addView(status)
        val controls=LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL }
        controls.addView(button("⏮"){ controller?.transportControls?.skipToPrevious() },LinearLayout.LayoutParams(0,dp(50),1f))
        controls.addView(button("⏯"){ val c=controller; if(c?.playbackState?.state==3) c.transportControls.pause() else c?.transportControls?.play() },LinearLayout.LayoutParams(0,dp(50),1f))
        controls.addView(button("⏭"){ controller?.transportControls?.skipToNext() },LinearLayout.LayoutParams(0,dp(50),1f))
        music.addView(controls)
        music.addView(button("Permitir controles de música"){ startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) })
        music.addView(button("Abrir YouTube Music para elegir canciones"){ open("com.google.android.apps.youtube.music") })
        left.addView(music,LinearLayout.LayoutParams(-1,0,1f).apply { topMargin=dp(12) })
        body.addView(left,LinearLayout.LayoutParams(0,-1,0.95f).apply { rightMargin=dp(12) })
        val right=panel()
        right.addView(label("⌖ MAPA INTERACTIVO · OpenStreetMap",17f,orange))
        map=WebView(this).apply {
            settings.javaScriptEnabled=true
            settings.domStorageEnabled=true
            webViewClient=WebViewClient()
            loadUrl("file:///android_asset/map.html")
        }
        right.addView(map,LinearLayout.LayoutParams(-1,0,1f))
        val nav=LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL }
        nav.addView(button("Google Maps ↗"){open("com.google.android.apps.maps")},LinearLayout.LayoutParams(0,dp(46),1f))
        nav.addView(button("Waze ↗"){open("com.waze")},LinearLayout.LayoutParams(0,dp(46),1f))
        right.addView(nav)
        body.addView(right,LinearLayout.LayoutParams(0,-1,1.6f))
        root.addView(body,LinearLayout.LayoutParams(-1,0,1f))
        setContentView(root)
    }
    override fun onResume(){ super.onResume();handler.removeCallbacks(tick);tick.run() }
    override fun onPause(){handler.removeCallbacks(tick);super.onPause()}
    override fun onDestroy(){map?.destroy();super.onDestroy()}
}
