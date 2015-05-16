package de.inselhome.noteapp.domain;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.Set;

/**
 * @author iweinzierl
 */
public class NoteDescriptionParser {

    private final String description;

    public NoteDescriptionParser(final String description) {
        Preconditions.checkNotNull(description, "Description must not be null");
        this.description = description;
    }

    @SuppressWarnings("unchecked")
    public Set<Person> getPeople() {
        if (!description.contains("@")) {
            return Collections.EMPTY_SET;
        }

        return extractTokens(new Function<String, Person>() {
            @Override
            public Person apply(String input) {
                return new PersonBuilder().withName(input.replace("@", "")).build();
            }
        }, "@");
    }

    @SuppressWarnings("unchecked")
    public Set<Tag> getTags() {
        if (!description.contains("#")) {
            return Collections.EMPTY_SET;
        }

        return extractTokens(new Function<String, Tag>() {
            @Override
            public Tag apply(String input) {
                return new TagBuilder().withName(input.replace("#", "")).build();
            }
        }, "#");
    }

    private <T> Set<T> extractTokens(Function<String, T> transform, final String splitChar) {
        final Iterable<String> tokens = Splitter.on(" ").split(description);
        return Sets.newHashSet(Iterables.transform(Iterables.filter(tokens, new Predicate<String>() {
            @Override
            public boolean apply(String input) {
                return input.startsWith(splitChar);
            }
        }), transform));
    }
}
