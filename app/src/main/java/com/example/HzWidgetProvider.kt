package com.example

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.provider.Settings
import android.util.Log
import android.view.Display
import android.view.View
import android.widget.RemoteViews
import android.widget.Toast

class HzWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_SET_HZ) {
            val hz = intent.getIntExtra(EXTRA_HZ, 60)
            setRefreshRate(context, hz)
            
            // Trigger update of all widgets to reflect active state
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, HzWidgetProvider::class.java))
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }

    private fun setRefreshRate(context: Context, hz: Int) {
        try {
            val contentResolver = context.contentResolver
            // For older / standard Android 11+
            Settings.System.putFloat(contentResolver, "min_refresh_rate", hz.toFloat())
            Settings.System.putFloat(contentResolver, "peak_refresh_rate", hz.toFloat())
            // Some specific OEMs
            Settings.System.putInt(contentResolver, "user_refresh_rate", hz)
            Toast.makeText(context, "Set to ${hz}Hz", Toast.LENGTH_SHORT).show()
        } catch (e: SecurityException) {
            Log.e("HzWidget", "Permission denied", e)
            Toast.makeText(context, "Permission Denied. Open App to grant.", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e("HzWidget", "Error setting refresh rate", e)
        }
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val display = displayManager.getDisplay(Display.DEFAULT_DISPLAY)
        val modes = display.supportedModes
        val rates = modes.map { it.refreshRate.toInt() }.toSortedSet().toList()

        val views = RemoteViews(context.packageName, R.layout.hz_widget)
        
        val buttonIds = listOf(R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4, R.id.btn_5)
        
        // Hide all buttons initially
        buttonIds.forEach { id ->
            views.setViewVisibility(id, View.GONE)
        }

        // Current active hz (approximation)
        val currentPeak = try {
            Settings.System.getFloat(context.contentResolver, "peak_refresh_rate", 60f).toInt()
        } catch (e: Settings.SettingNotFoundException) {
            60
        }

        // Show buttons for available rates
        for (i in 0 until minOf(rates.size, buttonIds.size)) {
            val hz = rates[i]
            val btnId = buttonIds[i]
            
            views.setViewVisibility(btnId, View.VISIBLE)
            views.setTextViewText(btnId, "${hz}Hz")
            
            if (hz == currentPeak) {
                views.setInt(btnId, "setBackgroundResource", R.drawable.btn_bg_active)
            } else {
                views.setInt(btnId, "setBackgroundResource", R.drawable.btn_bg_inactive)
            }

            val intent = Intent(context, HzWidgetProvider::class.java).apply {
                action = ACTION_SET_HZ
                putExtra(EXTRA_HZ, hz)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                hz,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(btnId, pendingIntent)
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    companion object {
        const val ACTION_SET_HZ = "com.aistudio.hzwidget.SET_HZ"
        const val EXTRA_HZ = "EXTRA_HZ"
    }
}
