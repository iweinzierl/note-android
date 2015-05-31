package de.inselhome.noteapp.rest.impl;

import android.content.Context;
import android.content.Intent;

import com.google.common.base.Optional;
import com.google.gson.Gson;

import org.slf4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.inselhome.android.logging.AndroidLoggerFactory;
import de.inselhome.noteapp.domain.Note;
import de.inselhome.noteapp.rest.NoteAppClient;
import de.inselhome.noteapp.util.FileUtils;
import de.inselhome.noteapp.widget.overview.OverviewWidgetProvider;

public class CacheableNoteAppClient implements NoteAppClient {

    private static final Logger LOGGER = AndroidLoggerFactory.getInstance().getLogger("CacheableNoteAppClient");

    private final NoteAppClient delegate;
    private final File cacheDir;

    private List<Note> notes;

    private String username;
    private Context context;

    public CacheableNoteAppClient(final Context context, final NoteAppClient delegate, final File cacheDir) {
        this.context = context;
        this.notes = new ArrayList<>();
        this.delegate = delegate;
        this.cacheDir = cacheDir;
    }

    @Override
    public Boolean login(String username, String password) {
        this.username = username;
        return delegate.login(username, password);
    }

    @Override
    public Optional<List<Note>> list() {
        final Optional<List<Note>> notes = delegate.list();

        if (notes.isPresent()) {
            writeCachedNotes(notes);
            this.notes.clear();
            this.notes.addAll(notes.get());
        } else {
            final List<Note> cachedNotes = readCachedNotes();
            this.notes.clear();
            this.notes.addAll(cachedNotes);
        }

        if (this.notes.isEmpty()) {
            return Optional.absent();
        } else {
            return Optional.of(this.notes);
        }
    }

    private List<Note> readCachedNotes() {
        final String jsonNotes = FileUtils.fromFile(getCacheFile());
        final Note[] notes = new Gson().fromJson(jsonNotes, Note[].class);

        return Arrays.asList(notes);
    }

    private void writeCachedNotes(Optional<List<Note>> notes) {
        final String jsonNotes = new Gson().toJson(notes.get());
        if (FileUtils.toFile(getCacheFile(), jsonNotes)) {
            LOGGER.info("successfully cached {} notes", notes.get().size());
            context.sendBroadcast(new Intent(OverviewWidgetProvider.UPDATE_ACTION));
        } else {
            LOGGER.warn("caching of notes failed");
        }
    }

    private File getCacheFile() {
        return new File(cacheDir, "note_cache_" + username + ".json");
    }

    @Override
    public Optional<Note> create(Note note) {
        final Optional<Note> newNote = delegate.create(note);
        if (newNote.isPresent()) {
            this.notes.add(newNote.get());
            writeCachedNotes(Optional.of(this.notes));
        }
        return newNote;
    }

    @Override
    public Optional<Note> update(Note note) {
        // TODO update cached note
        return delegate.update(note);
    }

    @Override
    public boolean solve(String noteId) {
        if (delegate.solve(noteId)) {
            removeNoteFromMemoryCache(noteId);
            writeCachedNotes(Optional.of(this.notes));
            return true;
        }

        return false;
    }

    @Override
    public boolean open(String noteId) {
        return delegate.open(noteId);
    }

    @Override
    public void delete(Note note) {
        delegate.delete(note);
        removeNoteFromMemoryCache(note.getId());
    }

    private void removeNoteFromMemoryCache(final String noteId) {
        for (int i = notes.size() - 1; i >= 0; i--) {
            final Note n = notes.get(i);

            if (noteId.equals(n.getId())) {
                notes.remove(n);
            }
        }
    }
}
