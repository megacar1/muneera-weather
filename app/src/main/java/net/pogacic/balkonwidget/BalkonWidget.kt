
package net.pogacic.balkonwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.widget.RemoteViews

class BalkonWidget : AppWidgetProvider() {

    companion object {
        const val ACTION_REFRESH =
            "net.pogacic.balkonwidget.ACTION_REFRESH"

        const val EXTRA_MANUAL =
            "net.pogacic.balkonwidget.EXTRA_MANUAL"
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.e(
            "BalkonWidget",
            "onUpdate called: ${appWidgetIds.joinToString()}"
        )

        for (id in appWidgetIds) {
            updateWidget(
                context,
                appWidgetManager,
                id,
                false
            )
        }
    }

    private fun updateWidget(
        context: Context,
        manager: AppWidgetManager,
        id: Int,
        manual: Boolean
    ) {
        val views = RemoteViews(
            context.packageName,
            R.layout.widget_layout
        )

        // Obvious test appearance
        views.setInt(
            R.id.root,
            "setBackgroundColor",
            Color.GREEN
        )

        views.setTextViewText(
            R.id.location,
            if (manual) "BUTTON CLICK WORKED" else "KOTLIN UPDATE WORKED"
        )
        views.setTextViewText(R.id.temp, "123")
        views.setTextViewText(R.id.feels, "FEELS 456")
        views.setTextViewText(R.id.humidity, "HUMIDITY 789%")
        views.setTextViewText(R.id.abs, "ABS 321")

        val intent = Intent(
            context,
            BalkonWidget::class.java
        ).apply {
            action = ACTION_REFRESH
            putExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                id
            )
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                PendingIntent.FLAG_IMMUTABLE
        )

        views.setOnClickPendingIntent(
            R.id.refresh_button,
            pendingIntent
        )

        manager.updateAppWidget(id, views)
    }

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        if (intent.action == ACTION_REFRESH) {
            val id = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )

            Log.e(
                "BalkonWidget",
                "Refresh button received; widget ID=$id"
            )

            if (id != AppWidgetManager.INVALID_APPWIDGET_ID) {
                updateWidget(
                    context,
                    AppWidgetManager.getInstance(context),
                    id,
                    true
                )
            }
            return
        }

        super.onReceive(context, intent)
    }
}
