package de.inselhome.noteapp.widget.overview;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.widget.RemoteViews;

import java.util.Date;

import de.inselhome.noteapp.R;

public class OverviewWidgetUpdateService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_overview);
        views.setTextViewText(R.id.clock, new Date().toString());

        ComponentName widget = new ComponentName(this, OverviewWidgetProvider.class);
        AppWidgetManager.getInstance(this).updateAppWidget(widget, views);

        return super.onStartCommand(intent, flags, startId);
    }
}
