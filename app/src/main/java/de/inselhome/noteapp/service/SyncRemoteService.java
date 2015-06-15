package de.inselhome.noteapp.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import org.slf4j.Logger;

import java.io.IOException;

import de.inselhome.android.logging.AndroidLoggerFactory;
import de.inselhome.noteapp.NoteApp;
import de.inselhome.noteapp.data.impl.remote.RemoteNoteAppClient;

public class SyncRemoteService extends Service {

    private static final Logger LOGGER = AndroidLoggerFactory.getInstance().getLogger("SyncRemoteService");
    private static final long UPDATE_INTERVAL = 1 * 60 * 1000;

    private static PendingIntent service;

    public static void start(final Context context) {
        final Intent updateIntent = new Intent(context, SyncRemoteService.class);
        service = PendingIntent.getService(context, 0, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, 0, UPDATE_INTERVAL, service);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final RemoteNoteAppClient client = ((NoteApp) getApplication()).getRemoteNoteAppClient();
        try {
            client.sync();
        }
        catch (IOException e) {
            LOGGER.error("Sync failed!");
        }

        return super.onStartCommand(intent, flags, startId);
    }
}
