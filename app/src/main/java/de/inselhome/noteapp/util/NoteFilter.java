package de.inselhome.noteapp.util;

import de.inselhome.noteapp.domain.Note;

/**
 * @author iweinzierl
 */
public interface NoteFilter {

    String getTextValue();

    boolean applies(Note note);
}
