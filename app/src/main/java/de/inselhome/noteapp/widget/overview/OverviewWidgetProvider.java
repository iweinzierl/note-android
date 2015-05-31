package de.inselhome.noteapp.widget.overview;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import de.inselhome.noteapp.R;
import de.inselhome.noteapp.activity.CreateNoteActivity;

public class OverviewWidgetProvider extends AppWidgetProvider {

    private PendingIntent service;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            final Intent remoteServiceIntent = new Intent(context, OverviewWidgetRemoteService.class);
            remoteServiceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            remoteServiceIntent.setData(Uri.parse(remoteServiceIntent.toUri(Intent.URI_INTENT_SCHEME)));

            final RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_overview);
            rv.setRemoteAdapter(appWidgetId, R.id.list, remoteServiceIntent);
            rv.setEmptyView(R.id.list, R.id.empty_view);

            final Intent intent = new Intent(context, CreateNoteActivity.class);
            final PendingIntent pendingIntent = PendingIntent.getActivities(context, 0, new Intent[] { intent }, 0);
            rv.setOnClickPendingIntent(R.id.add, pendingIntent);

            /*
            final Intent intent = new Intent(context, OverviewWidgetUpdateService.class);
            service = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

            final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.setRepeating(AlarmManager.RTC, 0, 600000, service);
            */

            appWidgetManager.updateAppWidget(appWidgetId, rv);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);

        final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(service);
    }
}
