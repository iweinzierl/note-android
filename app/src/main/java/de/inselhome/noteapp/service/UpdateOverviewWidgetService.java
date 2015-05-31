package de.inselhome.noteapp.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import org.slf4j.Logger;

import de.inselhome.android.logging.AndroidLoggerFactory;
import de.inselhome.noteapp.widget.overview.OverviewWidgetProvider;

public class UpdateOverviewWidgetService extends Service {

    private static final Logger LOGGER = AndroidLoggerFactory.getInstance().getLogger("UpdateOverviewWidgetService");
    private static final long UPDATE_INTERVAL = 1000 * 60 * 15;

    private PendingIntent service;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        LOGGER.debug("Create UpdateOverviewWidgetService");

        final Intent updateIntent = new Intent(getApplicationContext(), UpdateNoteService.class);
        service = PendingIntent.getService(getApplicationContext(), 0, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        final AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, 0, UPDATE_INTERVAL, service);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LOGGER.debug("Start UpdateOverviewWidgetService");

        sendBroadcast(new Intent(OverviewWidgetProvider.UPDATE_ACTION));
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        final AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(service);
    }
}
