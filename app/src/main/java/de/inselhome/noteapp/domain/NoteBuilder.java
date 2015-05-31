package de.inselhome.noteapp.domain;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import java.util.Date;

/**
 * @author iweinzierl
 */
public class NoteBuilder {

    private final Note note;

    public NoteBuilder() {
        this.note = new Note();
    }

    public NoteBuilder withDescription(final String description) {
        note.setDescription(description);
        return this;
    }

    public NoteBuilder withCreation(final Date creation) {
        note.setCreation(creation);
        return this;
    }

    public Note build() {
        // TODO parse people and tags
        Preconditions.checkArgument(!Strings.isNullOrEmpty(note.getDescription()));
        return note;
    }
}
