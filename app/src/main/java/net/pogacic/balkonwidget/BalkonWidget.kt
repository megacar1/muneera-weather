
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
    android.util.Log.e(
        "BalkonWidget",
        "GREEN TEST: onUpdate called, IDs=${appWidgetIds.joinToString()}"
    )

    for (id in appWidgetIds) {
        val views = RemoteViews(
            context.packageName,
            R.layout.widget_layout
        )

        // Make the entire widget unmistakable
        views.setInt(
            R.id.root,
            "setBackgroundColor",
            android.graphics.Color.GREEN
        )

        views.setTextViewText(R.id.location, "GREEN TEST - UPDATED")
        views.setTextViewText(R.id.temp, "123")
        views.setTextViewText(R.id.feels, "FEELS 456")
        views.setTextViewText(R.id.humidity, "HUMIDITY 789%")
        views.setTextViewText(R.id.abs, "ABS 321")

        appWidgetManager.updateAppWidget(id, views)
    }
}
