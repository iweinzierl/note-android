package de.inselhome.noteapp.data;

import java.util.List;

import com.google.common.base.Optional;

import de.inselhome.noteapp.domain.Note;
import de.inselhome.noteapp.exception.PersistenceException;

/**
 * @author iweinzierl
 */
public interface NoteAppClient {

    void setUsername(String username);

    void setPassword(String password);

    Optional<List<Note>> list() throws PersistenceException;

    Optional<Note> create(Note note) throws PersistenceException;

    Optional<Note> update(Note note) throws PersistenceException;

    boolean solve(String noteId);

    boolean open(String noteId);

    void delete(Note note);
}
