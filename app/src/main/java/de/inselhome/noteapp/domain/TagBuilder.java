package de.inselhome.noteapp.domain;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * @author iweinzierl
 */
public class TagBuilder {

    private final Tag tag;

    public TagBuilder() {
        tag = new Tag();
    }

    public TagBuilder withName(final String name) {
        tag.setName(name);
        return this;
    }

    public Tag build() {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(tag.getName()));
        return tag;
    }
}

