package de.inselhome.noteapp.task;

import android.os.AsyncTask;
import com.google.common.base.Optional;
import de.inselhome.android.logging.AndroidLoggerFactory;
import de.inselhome.noteapp.domain.Note;
import de.inselhome.noteapp.rest.NoteAppClient;
import org.slf4j.Logger;

import java.util.List;

/**
 * @author iweinzierl
 */
public class LoadNotesTask extends AsyncTask<NoteAppClient, Void, Optional<List<Note>>> {

    public interface Handler {
        void onFinish(Optional<List<Note>> result);
    }

    private static final Logger LOGGER = AndroidLoggerFactory.getInstance("[NOTEAPP]").getLogger("LoadNotesTask");

    private final Handler handler;

    public LoadNotesTask(final Handler handler) {
        this.handler = handler;
    }

    @Override
    protected Optional<List<Note>> doInBackground(final NoteAppClient... params) {
        try {
            return params[0].list();
        } catch (Exception e) {
            LOGGER.error("Error while loading notes from backend", e);
        }

        return Optional.absent();
    }

    @Override
    protected void onPostExecute(final Optional<List<Note>> result) {
        super.onPostExecute(result);
        handler.onFinish(result);
    }
}
