override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
  // Run on background thread
  Thread {
    try {
      val text = URL("https://pogacic.net/weather.php").readText() 
      // text = "Temperature: 30.6 C Humidity: 71.0 % Absolute Humidity: 22.3 g/m3 Feels Like: 36.8 C"
      
      val temp = Regex("Temperature:\\s*([0-9.]+)").find(text)?.groupValues?.get(1) ?: "--"
      val hum = Regex("Humidity:\\s*([0-9.]+)").find(text)?.groupValues?.get(1) ?: "--"
      val abs = Regex("Absolute Humidity:\\s*([0-9.]+)").find(text)?.groupValues?.get(1) ?: "--"
      val feels = Regex("Feels Like:\\s*([0-9.]+)").find(text)?.groupValues?.get(1) ?: "--"

      val tz = TimeZone.getTimeZone("Asia/Dubai")
      val hour = Calendar.getInstance(tz).get(Calendar.HOUR_OF_DAY)
      val isDay = hour in 6..18
      val iconRes = if (isDay) R.drawable.ic_sun else R.drawable.ic_night_cloud

      for (id in ids) {
        val views = RemoteViews(context.packageName, R.layout.widget_layout).apply {
          setTextViewText(R.id.location, "Al Muneera • Now")
          setTextViewText(R.id.temp, "${temp}°C")
          setTextViewText(R.id.feels, "Feels Like ${feels}°C")
          setTextViewText(R.id.humidity, "Humidity ${hum}%")
          setTextViewText(R.id.abs, "Abs: ${abs} g/m3")
          setImageViewResource(R.id.big_icon, iconRes)
          
          // Tap opens pogacic.net
          val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://pogacic.net/weather.php"))
          val pending = PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
          setOnClickPendingIntent(R.id.root, pending)
        }
        manager.updateAppWidget(id, views)
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }.start()
}
