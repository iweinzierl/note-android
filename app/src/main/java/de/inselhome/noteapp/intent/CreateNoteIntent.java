package de.inselhome.noteapp.intent;

import android.content.Context;
import android.content.Intent;
import de.inselhome.noteapp.activity.CreateNoteActivity;
import de.inselhome.noteapp.domain.Note;

/**
 * @author iweinzierl
 */
public class CreateNoteIntent extends Intent {

    public static final String EXTRA_NOTE = "createnote.extra.note";

    public CreateNoteIntent(final Context packageContext) {
        super(packageContext, CreateNoteActivity.class);
    }

    public CreateNoteIntent(final Intent o) {
        super(o);
    }

    public CreateNoteIntent(final Note note) {
        //putExtra(EXTRA_NOTE, note);
        //putExtra(EXTRA_NOTE, new ObjectMapper().writeValueAsString(note));
    }

    public Note getNote() {
        return (Note) getSerializableExtra(EXTRA_NOTE);
    }
}
