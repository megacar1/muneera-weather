
package net.pogacic.balkonwidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews

class BalkonWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {

            val views = RemoteViews(
                context.packageName,
                R.layout.widget_layout
            )

            // TEST VALUES — no network, no JSON
            views.setTextViewText(R.id.temp, "123.4°C")
            views.setTextViewText(R.id.feels, "Feels Like 567.8°C")
            views.setTextViewText(R.id.humidity, "Humidity 75%")
            views.setTextViewText(R.id.abs, "Abs: 22.6 g/m3")

            appWidgetManager.updateAppWidget(
                appWidgetId,
                views
            )
        }
    }
}
