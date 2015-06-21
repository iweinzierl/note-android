package de.inselhome.noteapp.data.impl;

import android.content.Context;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import de.inselhome.noteapp.data.NotePersistenceProvider;
import de.inselhome.noteapp.domain.Note;
import de.inselhome.noteapp.exception.PersistenceException;
import de.inselhome.noteapp.util.FileUtils;
import de.inselhome.noteapp.util.JsonUtils;

public class FilePersistenceProvider implements NotePersistenceProvider {

    private final Context context;
    private final String user;

    public FilePersistenceProvider(final Context context, final String user) {
        this.context = context;
        this.user = user;
    }

    @Override
    public Note save(final Note note) throws PersistenceException {
        prepareNote(note);

        List<Note> persistedNotes = list();
        if (!Strings.isNullOrEmpty(note.getId())) {
            persistedNotes = Lists.newArrayList(Collections2.filter(persistedNotes, new Predicate<Note>() {
                @Override
                public boolean apply(Note input) {
                    return !note.getId().equals(input.getId());
                }
            }));
        }
        persistedNotes.add(note);

        if (!FileUtils.toFile(getNoteFile(), JsonUtils.toJson(persistedNotes))) {
            throw new PersistenceException("Unable to save new note");
        }

        return note;
    }

    @SuppressWarnings(value = "unchecked")
    @Override
    public List<Note> list() throws PersistenceException {
        try {
            if (getNoteFile().exists()) {
                final String jsonContent = FileUtils.fromFile(getNoteFile());
                final Note[] notes = JsonUtils.read(Note[].class, jsonContent);
                return Lists.newArrayList(notes);
            } else {
                return new ArrayList<>();
            }
        } catch (IOException e) {
            throw new PersistenceException("Unable to read json file", e);
        }
    }

    public void replace(final List<Note> notes) throws PersistenceException {
        if (!FileUtils.toFile(getNoteFile(), JsonUtils.toJson(notes))) {
            throw new PersistenceException("Unable to replace notes");
        }
    }

    private File getNoteFile() {
        return new File(context.getFilesDir(), user);
    }

    private Note prepareNote(final Note note) {
        if (Strings.isNullOrEmpty(note.getId())) {
            note.setId(UUID.randomUUID().toString());
        }

        if (note.getCreation() == null) {
            note.setCreation(new Date());
        }

        return note;
    }
}
