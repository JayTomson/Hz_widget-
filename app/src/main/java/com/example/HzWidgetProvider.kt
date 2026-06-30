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
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_grid)
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
        val views = RemoteViews(context.packageName, R.layout.hz_widget)
        
        val intent = Intent(context, HzWidgetService::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            data = android.net.Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
        }
        
        views.setRemoteAdapter(R.id.widget_grid, intent)
        views.setEmptyView(R.id.widget_grid, android.R.id.empty)
        
        val clickIntentTemplate = Intent(context, HzWidgetProvider::class.java).apply {
            action = ACTION_SET_HZ
        }
        val clickPendingIntentTemplate = PendingIntent.getBroadcast(
            context,
            0,
            clickIntentTemplate,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        views.setPendingIntentTemplate(R.id.widget_grid, clickPendingIntentTemplate)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    companion object {
        const val ACTION_SET_HZ = "com.aistudio.hzwidget.SET_HZ"
        const val EXTRA_HZ = "EXTRA_HZ"
    }
}
