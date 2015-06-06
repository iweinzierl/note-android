package de.inselhome.noteapp.task;

import android.os.AsyncTask;
import com.google.common.base.Optional;
import de.inselhome.android.logging.AndroidLoggerFactory;
import de.inselhome.noteapp.domain.Note;
import de.inselhome.noteapp.data.NoteAppClient;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * @author iweinzierl
 */
public class CreateNotesTask extends AsyncTask<Note, Void, List<Note>> {

    public interface Handler {
        void onFinish(List<Note> result);
    }

    private static final Logger LOGGER = AndroidLoggerFactory.getInstance("[NOTEAPP]").getLogger("LoadNotesTask");

    private final NoteAppClient noteAppClient;
    private final Handler handler;

    public CreateNotesTask(final NoteAppClient noteAppClient, final Handler handler) {
        this.noteAppClient = noteAppClient;
        this.handler = handler;
    }

    @Override
    protected List<Note> doInBackground(final Note... params) {
        final List<Note> createdNotes = new ArrayList<>();

        for (final Note note : params) {
            try {
                final Optional<Note> createdNote = noteAppClient.create(note);
                if (createdNote.isPresent()) {
                    final Note created = createdNote.get();

                    LOGGER.info("Created new note: {}", created.getId());
                    createdNotes.add(created);
                }
            } catch (Exception e) {
                LOGGER.error("Error while loading notes from backend", e);
            }
        }

        return createdNotes;
    }

    @Override
    protected void onPostExecute(final List<Note> result) {
        super.onPostExecute(result);
        handler.onFinish(result);
    }
}
