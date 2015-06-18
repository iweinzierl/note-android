package de.inselhome.noteapp.data.impl.remote;

import android.content.Context;

import com.google.common.base.Optional;

import org.slf4j.Logger;

import java.util.List;

import de.inselhome.android.logging.AndroidLoggerFactory;
import de.inselhome.noteapp.data.impl.LocalNoteAppClient;
import de.inselhome.noteapp.domain.Note;
import de.inselhome.noteapp.exception.PersistenceException;

public class BufferedRemoteClient extends RemoteNoteAppClient {

    private final static Logger LOGGER = AndroidLoggerFactory.getInstance().getLogger("BufferedRemoteClient");

    private final LocalNoteAppClient localClient;

    public BufferedRemoteClient(Context context, String baseUrl, LocalNoteAppClient localClient) {
        super(context, baseUrl);
        this.localClient = localClient;
    }

    @Override
    public void setUsername(String username) {
        super.setUsername(username);
        localClient.setUsername(username);
    }

    @Override
    public void setPassword(String password) {
        super.setPassword(password);
        localClient.setPassword(password);
    }

    @Override
    public Optional<List<Note>> list() throws PersistenceException {
        try {
            final Optional<List<Note>> noteList = super.list();

            if (noteList.isPresent()) {
                localClient.replace(noteList.get());
            }

            return noteList;
        } catch (PersistenceException e) {
            LOGGER.warn("Did not receive notes from remote service", e);
            return localClient.list();
        }
    }

    @Override
    public Optional<Note> create(Note note) throws PersistenceException {
        final Optional<Note> persistedNote = super.create(note);

        if (persistedNote.isPresent()) {
            localClient.create(persistedNote.get());
        }

        return persistedNote;
    }

    @Override
    public Optional<Note> update(Note note) throws PersistenceException {
        final Optional<Note> updatedNote = super.update(note);

        if (updatedNote.isPresent()) {
            localClient.update(updatedNote.get());
        }

        return updatedNote;
    }

    @Override
    public boolean solve(String noteId) {
        final boolean solved = super.solve(noteId);

        if (solved) {
            localClient.solve(noteId);
        }

        return solved;
    }

    @Override
    public boolean open(String noteId) {
        final boolean opened = super.open(noteId);

        if (opened) {
            localClient.open(noteId);
        }

        return opened;
    }

    @Override
    public boolean delete(Note note) {
        final boolean deleted = super.delete(note);

        if (deleted) {
            localClient.delete(note);
        }

        return deleted;
    }
}
