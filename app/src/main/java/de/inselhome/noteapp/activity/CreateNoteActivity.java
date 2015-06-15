package de.inselhome.noteapp.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.google.common.base.Optional;
import com.google.common.base.Strings;

import de.inselhome.android.logging.AndroidLoggerFactory;
import de.inselhome.noteapp.NoteApp;
import de.inselhome.noteapp.R;
import de.inselhome.noteapp.adapter.note.AutoCompleteAdapter;
import de.inselhome.noteapp.domain.Note;
import de.inselhome.noteapp.domain.NoteBuilder;
import de.inselhome.noteapp.domain.NoteDescriptionParser;
import de.inselhome.noteapp.intent.CreateNoteIntent;
import de.inselhome.noteapp.data.NoteAppClient;
import de.inselhome.noteapp.task.CreateNotesTask;
import de.inselhome.noteapp.task.LoadNotesTask;
import de.inselhome.noteapp.task.UpdateNoteTask;

import org.slf4j.Logger;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author iweinzierl
 */
public class CreateNoteActivity extends Activity {

    public static final String EXTRA_NOTE_ID = "de.inselhome.noteapp.OVERVIEWWIDGET_EXTRA_NOTE_ID";

    private static final Logger LOGGER = AndroidLoggerFactory.getInstance("[NOTEAPP]").getLogger("CreateNoteActivity");

    private Note editNote;

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

        final Intent intent = getIntent();
        final String noteId = intent == null ? null : intent.getStringExtra(EXTRA_NOTE_ID);

        final NoteAppClient noteAppClient = NoteApp.get(this).getNoteAppClient();
        new LoadNotesTask(new LoadNotesTask.Handler() {
            @Override
            @SuppressWarnings("unchecked")
            public void onFinish(Optional<List<Note>> result) {
                final List<Note> notes = result.or(Collections.EMPTY_LIST);
                autoCompleteAdapter.setData(notes);

                if (!Strings.isNullOrEmpty(noteId) && result.isPresent()) {
                    fillWithNote(result.get(), noteId);
                }
            }
        }).execute(noteAppClient);
    }

    private void fillWithNote(List<Note> notes, String noteId) {
        for (Note note: notes) {
            // TODO finding the note specified by noteId should be improved!
            if (note.getId().equals(noteId)) {
                this.editNote = note;
                description.setText(note.getDescription());
                return;
            }
        }
    }

    private void saveNote() {
        final String noteDescription = description.getText().toString();
        LOGGER.debug("Going to save note: {}", noteDescription);

        try {
            final Note newNote = new NoteBuilder().withDescription(noteDescription).withCreation(new Date()).build();
            final NoteDescriptionParser parser = new NoteDescriptionParser(newNote.getDescription());
            newNote.setPeople(parser.getPeople());
            newNote.setTags(parser.getTags());

            final NoteAppClient noteAppClient = NoteApp.get(this).getNoteAppClient();

            if (editNote != null) {
                newNote.setId(editNote.getId());

                new UpdateNoteTask(noteAppClient, new UpdateNoteTask.Handler() {
                    @Override
                    public void onFinish(List<Note> result) {
                        if (!result.isEmpty()) {
                            setResult(RESULT_OK, new CreateNoteIntent(result.get(0)));
                            finish();
                        }
                    }
                }).execute(newNote);
            }
            else {

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
            }
        } catch (IllegalArgumentException e) {
            LOGGER.error("Error during note creation", e);
        }
    }
}
