package com.example.balkonwidget // <-- keep your package name

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import org.json.JSONObject
import java.net.URL
import java.util.Calendar
import java.util.TimeZone

class BalkonWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        for (id in ids) {
            // Show loading immediately so user doesn't see --C
            val loadingViews = RemoteViews(context.packageName, R.layout.widget_layout).apply {
                setTextViewText(R.id.location, "📍 Al Muneera • Now")
                setTextViewText(R.id.temp, "--°C")
                setTextViewText(R.id.feels, "Feels Like --°C")
                setTextViewText(R.id.humidity, "Humidity --%")
                setTextViewText(R.id.abs, "Abs: -- g/m3")
            }
            manager.updateAppWidget(id, loadingViews)

            Thread {
                try {
                    // 10 sec timeout so widget doesn't hang
                    val conn = URL("https://pogacic.net/weather.php?json=1").openConnection()
                    conn.connectTimeout = 10000
                    conn.readTimeout = 10000
                    val jsonText = conn.getInputStream().bufferedReader().readText()
                    android.util.Log.d("BalkonWidget", "JSON: $jsonText")

                    val json = JSONObject(jsonText)
                    val temp = json.optDouble("temperature", 0.0)
                    val hum = json.optDouble("humidity", 0.0)
                    val abs = json.optDouble("absolute_humidity", 0.0)
                    val feels = json.optDouble("feels_like", 0.0)

                    val tz = TimeZone.getTimeZone("Asia/Dubai")
                    val hour = Calendar.getInstance(tz).get(Calendar.HOUR_OF_DAY)
                    val isDay = hour in 6..18
                    val iconRes = if (isDay) android.R.drawable.ic_menu_day else android.R.drawable.ic_menu_night

                    val views = RemoteViews(context.packageName, R.layout.widget_layout).apply {
                        setTextViewText(R.id.location, "Al Muneera • Now")
                        setTextViewText(R.id.temp, String.format("%.1f°C", temp))
                        setTextViewText(R.id.feels, String.format("Feels Like %.1f°C", feels))
                        setTextViewText(R.id.humidity, String.format("Humidity %.0f%%", hum))
                        setTextViewText(R.id.abs, String.format("Abs: %.1f g/m3", abs))
                        setImageViewResource(R.id.big_icon, iconRes)

                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://pogacic.net/"))
                        val pending = PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                        setOnClickPendingIntent(R.id.root, pending)
                    }
                    manager.updateAppWidget(id, views)

                } catch (e: Exception) {
                    android.util.Log.e("BalkonWidget", "Fetch failed", e)
                }
            }.start()
        }
    }
}
