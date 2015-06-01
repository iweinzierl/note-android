package de.inselhome.noteapp.task;

import android.os.AsyncTask;

import com.google.common.base.Optional;

import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import de.inselhome.android.logging.AndroidLoggerFactory;
import de.inselhome.noteapp.domain.Note;
import de.inselhome.noteapp.rest.NoteAppClient;

/**
 * @author iweinzierl
 */
public class UpdateNoteTask extends AsyncTask<Note, Void, List<Note>> {

    public interface Handler {
        void onFinish(List<Note> result);
    }

    private static final Logger LOGGER = AndroidLoggerFactory.getInstance("[NOTEAPP]").getLogger("UpdateNoteTask");

    private final NoteAppClient noteAppClient;
    private final Handler handler;

    public UpdateNoteTask(final NoteAppClient noteAppClient, final Handler handler) {
        this.noteAppClient = noteAppClient;
        this.handler = handler;
    }

    @Override
    protected List<Note> doInBackground(final Note... params) {
        final List<Note> createdNotes = new ArrayList<>();

        for (final Note note : params) {
            try {
                final Optional<Note> updatedNote = noteAppClient.update(note);
                if (updatedNote.isPresent()) {
                    final Note updated = updatedNote.get();

                    LOGGER.info("Updated note: {}", updated.getId());
                    createdNotes.add(updated);
                }
            } catch (Exception e) {
                LOGGER.error("Error while updating note from backend", e);
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
