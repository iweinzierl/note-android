package de.inselhome.noteapp.widget.overview;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import org.slf4j.Logger;

import de.inselhome.android.logging.AndroidLoggerFactory;
import de.inselhome.noteapp.R;
import de.inselhome.noteapp.activity.CreateNoteActivity;
import de.inselhome.noteapp.activity.NoteOverview;

public class OverviewWidgetProvider extends AppWidgetProvider {

    public static final String UPDATE_ACTION = "de.inselhome.noteapp.OVERVIEWWIDGET_UPDATE";

    private static final Logger LOGGER = AndroidLoggerFactory.getInstance().getLogger("OverviewWidgetProvider");

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        LOGGER.info("Update OverviewWidget");

        for (int appWidgetId : appWidgetIds) {
            final Intent remoteServiceIntent = new Intent(context, OverviewWidgetRemoteService.class);
            remoteServiceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            remoteServiceIntent.setData(Uri.parse(remoteServiceIntent.toUri(Intent.URI_INTENT_SCHEME)));

            final RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_overview);
            rv.setRemoteAdapter(appWidgetId, R.id.list, remoteServiceIntent);
            rv.setEmptyView(R.id.list, R.id.empty_view);

            final Intent intent = new Intent(context, CreateNoteActivity.class);
            final PendingIntent pendingIntent = PendingIntent.getActivities(context, 0, new Intent[]{intent}, 0);
            rv.setOnClickPendingIntent(R.id.add, pendingIntent);

            final Intent overviewIntent = new Intent(context, NoteOverview.class);
            final PendingIntent overviewPendingIntent = PendingIntent.getActivities(context, 0, new Intent[]{overviewIntent}, 0);
            rv.setOnClickPendingIntent(R.id.control_header, overviewPendingIntent);

            final Intent modifyIntent = new Intent(context, CreateNoteActivity.class);
            final PendingIntent pendingModifyIntent = PendingIntent.getActivities(context, 0, new Intent[] { modifyIntent}, 0);
            rv.setPendingIntentTemplate(R.id.list, pendingModifyIntent);

            appWidgetManager.updateAppWidget(appWidgetId, rv);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        LOGGER.debug("Received broadcast event: {}", intent.getAction());

        if (intent.getAction().equals(UPDATE_ACTION)) {
            final AppWidgetManager manager = AppWidgetManager.getInstance(context);
            final ComponentName component = new ComponentName(context, OverviewWidgetProvider.class);

            manager.notifyAppWidgetViewDataChanged(manager.getAppWidgetIds(component), R.id.list);
        }
        super.onReceive(context, intent);
    }
}
