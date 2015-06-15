package de.inselhome.noteapp.domain.sync;

import java.util.Date;
import java.util.Set;

import de.inselhome.noteapp.domain.Note;
import de.inselhome.noteapp.domain.Person;
import de.inselhome.noteapp.domain.Tag;

public class UnsyncedNote extends Note {

    private final Note note;

    public UnsyncedNote(final Note note) {
        this.note = note;
    }

    @Override
    public String getId() {
        return note.getId();
    }

    @Override
    public void setId(String id) {
        note.setId(id);
    }

    @Override
    public String getOwner() {
        return note.getOwner();
    }

    @Override
    public void setOwner(String owner) {
        note.setOwner(owner);
    }

    @Override
    public Date getCreation() {
        return note.getCreation();
    }

    @Override
    public void setCreation(Date creation) {
        note.setCreation(creation);
    }

    @Override
    public Date getSolvedAt() {
        return note.getSolvedAt();
    }

    @Override
    public void setSolvedAt(Date solvedAt) {
        note.setSolvedAt(solvedAt);
    }

    @Override
    public String getDescription() {
        return note.getDescription();
    }

    @Override
    public void setDescription(String description) {
        note.setDescription(description);
    }

    @Override
    public Set<Person> getPeople() {
        return note.getPeople();
    }

    @Override
    public void setPeople(Set<Person> people) {
        note.setPeople(people);
    }

    @Override
    public Set<Tag> getTags() {
        return note.getTags();
    }

    @Override
    public void setTags(Set<Tag> tags) {
        note.setTags(tags);
    }
}
