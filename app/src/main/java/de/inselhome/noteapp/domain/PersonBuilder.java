package de.inselhome.noteapp.domain;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * @author iweinzierl
 */
public class PersonBuilder {

    private final Person person;

    public PersonBuilder() {
        person = new Person();
    }

    public PersonBuilder withName(final String name) {
        person.setName(name);
        return this;
    }

    public Person build() {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(person.getName()));
        return person;
    }
}

