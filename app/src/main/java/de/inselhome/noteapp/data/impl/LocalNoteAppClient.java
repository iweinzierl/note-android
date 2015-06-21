package de.inselhome.noteapp.data.impl;

import android.content.Context;
import android.content.Intent;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Date;
import java.util.List;

import de.inselhome.noteapp.data.NoteAppClient;
import de.inselhome.noteapp.data.NotePersistenceProvider;
import de.inselhome.noteapp.domain.Note;
import de.inselhome.noteapp.exception.PersistenceException;
import de.inselhome.noteapp.widget.overview.OverviewWidgetProvider;

public class LocalNoteAppClient implements NoteAppClient {

    private final Context context;

    private NotePersistenceProvider persistenceProvider;
    private List<Note> cache;

    public LocalNoteAppClient(final Context context) {
        this.context = context;
    }

    @Override
    public void setUsername(String username) {
        this.persistenceProvider = new FilePersistenceProvider(context, username);
    }

    @Override
    public void setPassword(String password) {
        // not used
    }

    @Override
    public Optional<List<Note>> list() throws PersistenceException {
        if (cache == null || cache.isEmpty()) {
            cache = persistenceProvider.list();
        }

        return cache == null || cache.isEmpty() ? Optional.<List<Note>>absent() : Optional.of(openList());
    }

    @Override
    public Optional<Note> create(Note note) throws PersistenceException {
        final Note created = persistenceProvider.save(note);

        if (created == null) {
            return Optional.absent();
        } else {
            cache.add(created);
            return Optional.of(created);
        }
    }

    @Override
    public Optional<Note> update(final Note note) throws PersistenceException {
        if (Strings.isNullOrEmpty(note.getId())) {
            return Optional.absent();
        } else {
            final Note updated = persistenceProvider.save(note);

            cache = Lists.newArrayList(Collections2.filter(cache, new Predicate<Note>() {
                @Override
                public boolean apply(Note input) {
                    return !note.getId().equals(input.getId());
                }
            }));

            cache.add(updated);

            return Optional.of(updated);
        }
    }

    @Override
    public boolean solve(final String noteId) throws PersistenceException {
        if (Strings.isNullOrEmpty(noteId)) {
            return false;
        } else {
            final Note note = Iterables.find(cache, new Predicate<Note>() {
                @Override
                public boolean apply(Note input) {
                    return noteId.equals(input.getId());
                }
            });
            note.setSolvedAt(new Date());
            persistenceProvider.save(note);
            return true;
        }
    }

    @Override
    public boolean open(final String noteId) throws PersistenceException {
        if (Strings.isNullOrEmpty(noteId)) {
            return false;
        } else {
            final Note note = Iterables.find(cache, new Predicate<Note>() {
                @Override
                public boolean apply(Note input) {
                    return noteId.equals(input.getId());
                }
            });
            note.setSolvedAt(null);
            persistenceProvider.save(note);
            return true;
        }
    }

    @Override
    public boolean delete(Note note) {
        // TODO
        return false;
    }

    public void replace(final List<Note> notes) throws PersistenceException {
        persistenceProvider.replace(notes);
        context.sendBroadcast(new Intent(OverviewWidgetProvider.UPDATE_ACTION));
    }

    private List<Note> openList() {
        return Lists.newArrayList(Collections2.filter(cache, new Predicate<Note>() {
            @Override
            public boolean apply(Note input) {
                return input.getSolvedAt() == null;
            }
        }));
    }
}
