package com.example

import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.provider.Settings
import android.view.Display
import android.widget.RemoteViews
import android.widget.RemoteViewsService

class HzWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return HzRemoteViewsFactory(this.applicationContext)
    }
}

class HzRemoteViewsFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {
    private var rates = emptyList<Int>()
    private var currentPeak = 60

    override fun onCreate() {
    }

    override fun onDataSetChanged() {
        val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val display = displayManager.getDisplay(Display.DEFAULT_DISPLAY)
        
        val modeRates = mutableSetOf<Int>()
        display?.supportedModes?.forEach { mode ->
            modeRates.add(mode.refreshRate.toInt())
            if (android.os.Build.VERSION.SDK_INT >= 34) {
                mode.alternativeRefreshRates.forEach { rate ->
                    modeRates.add(rate.toInt())
                }
            }
        }
        rates = modeRates.toSortedSet().toList()

        currentPeak = try {
            Settings.System.getFloat(context.contentResolver, "peak_refresh_rate", 60f).toInt()
        } catch (e: Settings.SettingNotFoundException) {
            60
        }
    }

    override fun onDestroy() {
    }

    override fun getCount(): Int {
        return rates.size
    }

    override fun getViewAt(position: Int): RemoteViews {
        val hz = rates[position]
        val views = RemoteViews(context.packageName, R.layout.hz_widget_item)
        views.setTextViewText(R.id.widget_item_text, "${hz}Hz")

        if (hz == currentPeak) {
            views.setInt(R.id.widget_item_text, "setBackgroundResource", R.drawable.btn_bg_active)
        } else {
            views.setInt(R.id.widget_item_text, "setBackgroundResource", R.drawable.btn_bg_inactive)
        }

        val fillInIntent = Intent().apply {
            putExtra(HzWidgetProvider.EXTRA_HZ, hz)
        }
        views.setOnClickFillInIntent(R.id.widget_item_text, fillInIntent)

        return views
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }
}
