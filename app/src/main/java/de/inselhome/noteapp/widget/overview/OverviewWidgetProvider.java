package de.inselhome.noteapp.widget.overview;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

public class OverviewWidgetProvider extends AppWidgetProvider {

    private PendingIntent service;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onEnabled(context);

        final Intent intent = new Intent(context, OverviewWidgetUpdateService.class);
        service = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC, 0, 600000, service);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);

        final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(service);
    }
}
