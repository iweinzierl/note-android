package de.inselhome.noteapp.task;

import android.os.AsyncTask;

import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import de.inselhome.android.logging.AndroidLoggerFactory;
import de.inselhome.noteapp.domain.Note;
import de.inselhome.noteapp.data.NoteAppClient;

/**
 * @author iweinzierl
 */
public class OpenNoteTask extends AsyncTask<Note, Void, OpenNoteTask.Result> {

    public interface Handler {
        void onFinish(List<Note> successful, List<Note> failed);
    }

    public static class Result {
        public List<Note> successful;
        public List<Note> failed;

        public Result(List<Note> successful, List<Note> failed) {
            this.successful = successful;
            this.failed = failed;
        }
    }

    private static final Logger LOGGER = AndroidLoggerFactory.getInstance("[NOTEAPP]").getLogger("OpenNoteTask");

    private final NoteAppClient noteAppClient;
    private final Handler handler;

    public OpenNoteTask(final NoteAppClient noteAppClient, final Handler handler) {
        this.noteAppClient = noteAppClient;
        this.handler = handler;
    }

    @Override
    protected Result doInBackground(final Note... params) {
        List<Note> successful = new ArrayList<>();
        List<Note> failed = new ArrayList<>();


        for (final Note note : params) {
            try {
                if (noteAppClient.open(note.getId())) {
                    successful.add(note);
                }
            } catch (Exception e) {
                LOGGER.error("Error while opening solved note '{}'", e, note.getId());
                failed.add(note);
            }
        }

        return new Result(successful, failed);
    }

    @Override
    protected void onPostExecute(final Result result) {
        super.onPostExecute(result);
        handler.onFinish(result.successful, result.failed);
    }
}
