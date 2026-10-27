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
    val pendingResult = goAsync() // <-- THIS IS THE FIX
    
    Thread {
        try {
            // Add User-Agent - some hosts block Java requests
            val conn = URL("https://pogacic.net/weather.php?json=1").openConnection() as java.net.HttpURLConnection
            conn.setRequestProperty("User-Agent", "Mozilla/5.0")
            conn.connectTimeout = 15000
            conn.readTimeout = 15000
            
            val jsonText = conn.inputStream.bufferedReader().readText()
            val json = JSONObject(jsonText)
            
            val temp = json.optDouble("temperature")
            val hum = json.optDouble("humidity")
            val abs = json.optDouble("absolute_humidity")
            val feels = json.optDouble("feels_like")

            val tz = TimeZone.getTimeZone("Asia/Dubai")
            val hour = Calendar.getInstance(tz).get(Calendar.HOUR_OF_DAY)
            val isDay = hour in 6..18
            // Use your drawables, fallback to system if missing
            val iconRes = try {
                if (isDay) R.drawable.ic_sun else R.drawable.ic_night_cloud
            } catch (e: Exception) {
                if (isDay) android.R.drawable.ic_menu_day else android.R.drawable.ic_menu_night
            }

            for (id in ids) {
                val views = RemoteViews(context.packageName, R.layout.widget_layout).apply {
                    setTextViewText(R.id.location, "Al Muneera • Now")
                    setTextViewText(R.id.temp, String.format("%.1f°C", temp))
                    setTextViewText(R.id.feels, String.format("Feels Like %.1f°C", feels))
                    setTextViewText(R.id.humidity, "Humidity ${hum.toInt()}%")
                    setTextViewText(R.id.abs, "Abs: %.1f g/m3".format(abs))
                    setImageViewResource(R.id.big_icon, iconRes)
                }
                manager.updateAppWidget(id, views)
            }
        } catch (e: Exception) {
            android.util.Log.e("BalkonWidget", "FAIL", e)
        } finally {
            pendingResult.finish() // <-- Tell Android we're done
        }
    }.start()
}
