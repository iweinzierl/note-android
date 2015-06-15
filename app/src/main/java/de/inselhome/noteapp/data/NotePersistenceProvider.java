package de.inselhome.noteapp.data;

import java.util.List;

import de.inselhome.noteapp.domain.Note;
import de.inselhome.noteapp.exception.PersistenceException;

public interface NotePersistenceProvider {

    Note save(Note note) throws PersistenceException;

    List<Note> list() throws PersistenceException;

    void replace(List<Note> notes) throws PersistenceException;
}
