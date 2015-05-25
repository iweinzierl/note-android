package de.inselhome.noteapp.rest;

import java.util.List;

import com.google.common.base.Optional;
import de.inselhome.noteapp.domain.Note;

/**
 * @author  iweinzierl
 */
public interface NoteAppClient {

    Boolean login(final String username, final String password);

    Optional<List<Note>> list();

    Optional<Note> create(Note note);

    Optional<Note> update(Note note);

    boolean solve(String noteId);

    boolean open(String noteId);

    void delete(Note note);
}
