package net.pogacic.balkonwidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import org.json.JSONObject
import java.net.URL
import java.util.Calendar
import java.util.TimeZone

class BalkonWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        val pendingResult = goAsync()
        Thread {
            try {
                val conn = URL("https://pogacic.net/json.php").openConnection() as java.net.HttpURLConnection
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
                
                for (id in ids) {
                    val views = RemoteViews(context.packageName, R.layout.widget_layout).apply {
                        setTextViewText(R.id.location, "Al Muneera • Now")
                        setTextViewText(R.id.temp, String.format("%.1f°C", temp))
                        setTextViewText(R.id.feels, String.format("Feels Like %.1f°C", feels))
                        setTextViewText(R.id.humidity, "Humidity ${hum.toInt()}%")
                        setTextViewText(R.id.abs, "Abs: %.1f g/m3".format(abs))
                        setImageViewResource(R.id.big_icon, if (isDay) R.drawable.ic_sun else R.drawable.ic_moon)
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
            setTextViewText(R.id.temp, "ERR: $errorType")
            setTextViewText(R.id.feels, errorMessage.take(100))
        }
        manager.updateAppWidget(id, views)
    }
} finally {
    pendingResult.finish()
}
        }.start()
    }
}
