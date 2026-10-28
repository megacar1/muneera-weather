package net.pogacic.balkonwidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import org.json.JSONObject
import java.net.URL
import java.util.Calendar
import java.util.TimeZone
import java.text.SimpleDateFormat
import java.util.Locale
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent

class BalkonWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        val pendingResult = goAsync()
        Thread {
            try {
                
                var jsonText: String? = null
                var lastError: Exception? = null

                // Maximum 5 attempts, waiting 2 seconds before each.
                for (attempt in 0 until 5) {
                    Thread.sleep(2000)

                    var conn: java.net.HttpURLConnection? = null

                    try {
                        conn = URL("https://pogacic.net/json.php")
                            .openConnection() as java.net.HttpURLConnection

                        conn.setRequestProperty("User-Agent", "Mozilla/5.0")
                        conn.connectTimeout = 10000
                        conn.readTimeout = 10000

                        jsonText = conn.inputStream.bufferedReader().use {
                            it.readText()
                        }

                        break
                    } catch (e: Exception) {
                        lastError = e
                    } finally {
                        conn?.disconnect()
                    }
                }

                val json = JSONObject(
                    jsonText ?: throw (
                        lastError ?: Exception("Weather fetch failed after 5 attempts")
                    )
                )
                val temp = json.optDouble("temperature")
                val hum = json.optDouble("humidity")
                val abs = json.optDouble("absolute_humidity")
                val feels = json.optDouble("feels_like")

                val tz = TimeZone.getTimeZone("Asia/Dubai")
                val hour = Calendar.getInstance(tz).get(Calendar.HOUR_OF_DAY)
                val isDay = hour in 6..18
                val updateTime = SimpleDateFormat("HH:mm", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("Asia/Dubai")
                    }.format(Calendar.getInstance().time)
                
                for (id in ids) {
                    val views = RemoteViews(context.packageName, R.layout.widget_layout).apply {
                        setTextViewText(R.id.location, "Al Muneera • Last Update: $updateTime")
                        setTextViewText(R.id.temp, String.format("%.1f°C", temp))
                        setTextViewText(R.id.feels, String.format("Feels Like %.1f°C", feels))
                        setTextViewText(R.id.humidity, "${hum.toInt()}% (${String.format("%.1f", abs)} g/m³)")
                        setImageViewResource(R.id.big_icon, if (isDay) R.drawable.ic_sun else R.drawable.ic_moon)
                        setOnClickPendingIntent(R.id.refresh_button, PendingIntent.getBroadcast(context, 0, Intent(context, BalkonWidget::class.java).setAction("net.pogacic.balkonwidget.REFRESH"), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
                    }
                    manager.updateAppWidget(id, views)
                }

} catch (e: Exception) {
    val errorType = e.javaClass.simpleName
    val errorMessage = e.message ?: "(no message)"

    android.util.Log.e(
        "BalkonWidget",
        "FAIL: $errorType: $errorMessage\n${android.util.Log.getStackTraceString(e)}"
    )

    for (id in ids) {
        val views = RemoteViews(context.packageName, R.layout.widget_layout).apply {
            setTextViewText(R.id.temp, "-")
            setTextViewText(R.id.feels, "-")
            setTextViewText(R.id.humidity, "-")
        }
        manager.updateAppWidget(id, views)
    }
} finally {
    pendingResult.finish()
}
    
        }.start()
    }
 override fun onReceive(context: Context, intent: Intent) {
    if (intent.action == "net.pogacic.balkonwidget.REFRESH") {
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(
            ComponentName(context, BalkonWidget::class.java)
        )
        onUpdate(context, manager, ids)
    } else {
        super.onReceive(context, intent)
    }
}   
}
