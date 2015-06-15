package de.inselhome.noteapp.data.impl.remote;

import java.io.IOException;
import java.util.List;

import de.inselhome.noteapp.domain.Note;
import de.inselhome.noteapp.domain.sync.SyncAction;
import de.inselhome.noteapp.domain.sync.SyncTask;
import de.inselhome.noteapp.exception.PersistenceException;

public interface SyncProvider {

    void setUsername(String username);

    void add(SyncAction action, Note note) throws PersistenceException;

    void remove(SyncTask task) throws PersistenceException;

    List<SyncTask> list() throws IOException;
}
