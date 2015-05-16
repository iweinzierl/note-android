package de.inselhome.noteapp.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author iweinzierl
 */
public class Note implements Serializable {

    private String id;

    private String owner;

    private Date creation;
    private Date solvedAt;

    private String description;

    private Set<Person> people;
    private Set<Tag> tags;

    public Note() {
        people = new HashSet<>();
        tags = new HashSet<>();
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(final String owner) {
        this.owner = owner;
    }

    public Date getCreation() {
        return creation;
    }

    public void setCreation(final Date creation) {
        this.creation = creation;
    }

    public Date getSolvedAt() {
        return solvedAt;
    }

    public void setSolvedAt(Date solvedAt) {
        this.solvedAt = solvedAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public Set<Person> getPeople() {
        return people;
    }

    public void setPeople(final Set<Person> people) {
        this.people = people;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(final Set<Tag> tags) {
        this.tags = tags;
    }
}
