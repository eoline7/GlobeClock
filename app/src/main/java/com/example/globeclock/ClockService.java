package com.example.globeclock;

import android.app.Activity;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.TypedValue;
import android.widget.RemoteViews;
import androidx.annotation.Nullable;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class ClockService extends Service {

    private Timer mTimer;

    @Override
    public void onCreate() {
        super.onCreate();
        mTimer = new Timer();
        mTimer.schedule(new MyTimerTask(), 0, 1000);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private final class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            SharedPreferences cfg = getSharedPreferences("MainActivity", Activity.MODE_PRIVATE);
            String title = cfg.getString(getString(R.string.cfg_title), getString(R.string.init_title));
            String zone = cfg.getString(getString(R.string.cfg_zone), getString(R.string.init_zone));
            boolean boolsec = cfg.getBoolean(getString(R.string.cfg_second), false);
            int fontsize = cfg.getInt(getString(R.string.cfg_fontsize), 48);

            Calendar ca = Calendar.getInstance();
            TimeZone cur_zone = TimeZone.getTimeZone(zone);
            ca.setTimeZone(cur_zone);
            int hh = ca.get(Calendar.HOUR_OF_DAY);
            int mm = ca.get(Calendar.MINUTE);
            int ss = ca.get(Calendar.SECOND);
            String timeText;
            if(boolsec) {
                timeText = String.format("%1$02d:%2$02d:%3$02d", hh, mm, ss);
            }
            else {
                timeText = String.format("%1$02d:%2$02d", hh, mm);
            }
            int dw = ca.get(Calendar.DAY_OF_WEEK);
            String weeks = getString(R.string.weeks);
            String titleText = String.format("%1$s / 周%2$s", title, weeks.substring(dw-1,dw));

            RemoteViews views = new RemoteViews(getPackageName(), R.layout.clock_widget);
            views.setTextViewText(R.id.aTime, timeText);
            views.setTextViewText(R.id.aTitle, titleText);
            views.setTextViewTextSize(R.id.aTime, TypedValue.COMPLEX_UNIT_SP, fontsize);

            AppWidgetManager widgetManager = AppWidgetManager.getInstance(getApplicationContext());
            ComponentName componentName = new ComponentName(getApplicationContext(), ClockWidget.class);
            widgetManager.updateAppWidget(componentName, views);
        }
    }
}

