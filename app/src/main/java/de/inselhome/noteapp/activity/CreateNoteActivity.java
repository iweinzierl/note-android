package de.inselhome.noteapp.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import com.google.common.base.Optional;

import de.inselhome.android.logging.AndroidLoggerFactory;
import de.inselhome.noteapp.NoteApp;
import de.inselhome.noteapp.R;
import de.inselhome.noteapp.adapter.note.AutoCompleteAdapter;
import de.inselhome.noteapp.adapter.note.NoteFilterAdapter;
import de.inselhome.noteapp.domain.Note;
import de.inselhome.noteapp.domain.NoteBuilder;
import de.inselhome.noteapp.domain.NoteDescriptionParser;
import de.inselhome.noteapp.intent.CreateNoteIntent;
import de.inselhome.noteapp.rest.NoteAppClient;
import de.inselhome.noteapp.rest.impl.NoteAppClientImpl;
import de.inselhome.noteapp.task.CreateNotesTask;
import de.inselhome.noteapp.task.LoadNotesTask;

import org.slf4j.Logger;

import java.util.Collections;
import java.util.List;

/**
 * @author iweinzierl
 */
public class CreateNoteActivity extends Activity {

    private static final Logger LOGGER = AndroidLoggerFactory.getInstance("[NOTEAPP]").getLogger("CreateNoteActivity");

    private AutoCompleteTextView description;
    private AutoCompleteAdapter autoCompleteAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createnote);

        autoCompleteAdapter = new AutoCompleteAdapter(this);

        description = (AutoCompleteTextView) findViewById(R.id.description);
        description.setAdapter(autoCompleteAdapter);

        Button save = (Button) findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LOGGER.debug("Clicked to 'save' note");
                saveNote();
            }
        });

        Button cancel = (Button) findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LOGGER.debug("Clicked to 'cancel' note");
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        final NoteAppClient noteAppClient = NoteApp.get(this).getNoteAppClient();
        new LoadNotesTask(new LoadNotesTask.Handler() {
            @Override
            @SuppressWarnings("unchecked")
            public void onFinish(Optional<List<Note>> result) {
                final List<Note> notes = result.or(Collections.EMPTY_LIST);
                autoCompleteAdapter.setData(notes);
            }
        }).execute(noteAppClient);
    }

    private void saveNote() {
        final String noteDescription = description.getText().toString();
        LOGGER.debug("Going to save note: {}", noteDescription);

        try {
            final Note newNote = new NoteBuilder().withDescription(noteDescription).build();
            final NoteDescriptionParser parser = new NoteDescriptionParser(newNote.getDescription());
            newNote.setPeople(parser.getPeople());
            newNote.setTags(parser.getTags());

            final NoteAppClient noteAppClient = NoteApp.get(this).getNoteAppClient();

            // TODO Display progress
            new CreateNotesTask(noteAppClient, new CreateNotesTask.Handler() {
                @Override
                public void onFinish(List<Note> result) {
                    if (!result.isEmpty()) {
                        setResult(RESULT_OK, new CreateNoteIntent(result.get(0)));
                        finish();
                    }
                }
            }).execute(newNote);

        } catch (IllegalArgumentException e) {
            LOGGER.error("Error during note creation", e);
        }
    }
}
