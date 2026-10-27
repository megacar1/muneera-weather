override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
    val pendingResult = goAsync()
    Thread {
        try {
            android.util.Log.d("BalkonWidget", "TEST UPDATE START")
            for (id in ids) {
                val views = RemoteViews(context.packageName, R.layout.widget_layout).apply {
                    setTextViewText(R.id.location, "Al Muneera • TEST")
                    setTextViewText(R.id.temp, "99.9°C")
                    setTextViewText(R.id.feels, "Feels Like 100°C TEST")
                    setTextViewText(R.id.humidity, "Humidity 99%")
                    setTextViewText(R.id.abs, "Abs: 99.9 g/m3")
                }
                manager.updateAppWidget(id, views)
            }
            android.util.Log.d("BalkonWidget", "TEST UPDATE DONE")
        } finally {
            pendingResult.finish()
        }
    }.start()
}
