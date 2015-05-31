package de.inselhome.noteapp.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.google.common.base.Optional;

import org.slf4j.Logger;

import java.util.List;

import de.inselhome.android.logging.AndroidLoggerFactory;
import de.inselhome.noteapp.NoteApp;
import de.inselhome.noteapp.domain.Note;
import de.inselhome.noteapp.task.LoadNotesTask;
import de.inselhome.noteapp.widget.overview.OverviewWidgetProvider;

public class UpdateNoteService extends Service {

    private static final Logger LOGGER = AndroidLoggerFactory.getInstance().getLogger("UpdateNoteService");
    private static final long UPDATE_INTERVAL = 30 * 60 * 1000;

    private static PendingIntent service;

    public static void start(final Context context) {
        final Intent updateIntent = new Intent(context, UpdateNoteService.class);
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
        LOGGER.info("Update notes");

        final NoteApp app = (NoteApp) getApplication();
        new LoadNotesTask(new LoadNotesTask.Handler() {
            @Override
            public void onFinish(Optional<List<Note>> result) {
                if (result.isPresent()) {
                    LOGGER.info("Updated cache with {} notes", result.get().size());
                    sendBroadcast(new Intent(OverviewWidgetProvider.UPDATE_ACTION));
                }
                else {
                    LOGGER.warn("Updating note cache failed");
                }
            }
        }).execute(app.getNoteAppClient());

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        service.cancel();
        super.onDestroy();
    }
}
