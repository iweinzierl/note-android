package de.inselhome.noteapp.widget.overview;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.google.gson.Gson;

import org.slf4j.Logger;

import java.io.File;

import de.inselhome.android.logging.AndroidLoggerFactory;
import de.inselhome.noteapp.NoteApp;
import de.inselhome.noteapp.R;
import de.inselhome.noteapp.activity.CreateNoteActivity;
import de.inselhome.noteapp.activity.NoteOverview;
import de.inselhome.noteapp.domain.Note;
import de.inselhome.noteapp.security.Credentials;
import de.inselhome.noteapp.util.ColorProvider;
import de.inselhome.noteapp.util.FileUtils;

public class OverviewWidgetRemoteService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new OverviewRemoteViewsFactory(getApplicationContext(), intent);
    }

    public class OverviewRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

        private final Logger LOGGER = AndroidLoggerFactory.getInstance().getLogger("OverviewRemoteViewsFactory");
        private final Context applicationContext;

        private Note[] notes;

        public OverviewRemoteViewsFactory(Context applicationContext, Intent intent) {
            this.applicationContext = applicationContext;
            LOGGER.info("Setup instance of OverviewRemoteViewsFactory");
        }

        private void updatesNotesFromCache() {
            LOGGER.debug("Read notes from cache for widget usage");

            final NoteApp noteApp = (NoteApp) getApplication();
            final Credentials credentials = noteApp.loadCredentials();

            final String json = FileUtils.fromFile(new File(applicationContext.getCacheDir(), "note_cache_" + credentials.getUsername() + ".json"));
            this.notes = new Gson().fromJson(json, Note[].class);

            LOGGER.info("Read {} notes from cache for widget usage", this.notes.length);
        }

        @Override
        public void onCreate() {
            updatesNotesFromCache();
        }

        @Override
        public void onDataSetChanged() {
            updatesNotesFromCache();
        }

        @Override
        public void onDestroy() {
        }

        @Override
        public int getCount() {
            return notes == null ? 0 : notes.length;
        }

        @Override
        public RemoteViews getViewAt(int i) {
            final Note note = notes[i];

            final RemoteViews rv = new RemoteViews(getPackageName(), R.layout.list_item_widget_overview);
            rv.setTextViewText(R.id.description, ColorProvider.colorText(note.getDescription()));
            rv.setTextViewText(R.id.creation, note.getCreation().toString());

            Intent fillInIntent = new Intent();
            fillInIntent.putExtra(CreateNoteActivity.EXTRA_NOTE_ID, note.getId());
            rv.setOnClickFillInIntent(R.id.item, fillInIntent);

            return rv;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
