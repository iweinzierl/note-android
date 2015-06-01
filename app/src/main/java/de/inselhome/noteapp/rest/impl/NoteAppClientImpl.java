package de.inselhome.noteapp.rest.impl;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import de.inselhome.android.logging.AndroidLoggerFactory;
import de.inselhome.noteapp.domain.Note;
import de.inselhome.noteapp.rest.NoteAppClient;
import org.slf4j.Logger;
import org.springframework.http.HttpBasicAuthentication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * @author iweinzierl
 */
public class NoteAppClientImpl implements NoteAppClient {

    private static final Logger LOGGER = AndroidLoggerFactory.getInstance("[NOTEAPP]").getLogger("NoteappClientImpl");

    private final String baseUrl;
    private final RestTemplate restTemplate;
    private final HttpHeaders httpHeaders;

    public NoteAppClientImpl(final String baseUrl) {
        this.baseUrl = baseUrl;
        this.restTemplate = new RestTemplate();
        this.restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        this.httpHeaders = new HttpHeaders();
        this.httpHeaders.setAccept(Lists.newArrayList(MediaType.APPLICATION_JSON));
    }

    public Boolean login(final String username, final String password) {
        this.httpHeaders.setAuthorization(new HttpBasicAuthentication(username, password));

        LOGGER.info("Login to backend: username = {}, password = {}", username, password);

        // TODO implement proper login resource
        final List<Note> notes = list().orNull();
        return notes != null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<List<Note>> list() {
        LOGGER.info("Fetch note list from backend");

        try {
            final URL url = withPath("/note");
            final HttpEntity<String> httpEntity = new HttpEntity<>(httpHeaders);

            final ResponseEntity<Note[]> exchange = restTemplate.exchange(url.toString(), HttpMethod.GET, httpEntity, Note[].class);
            final Note[] notes = exchange.getBody();

            LOGGER.debug("Received {} notes from server", notes.length);
            return Optional.fromNullable(Arrays.asList(notes));
        } catch (Exception e) {
            LOGGER.error("Error while fetching notes from server: {}", e, e.getMessage());
        }

        return Optional.absent();
    }

    @Override
    public Optional<Note> create(final Note note) {
        LOGGER.info("Create note: {}", note);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.setAccept(Lists.newArrayList(MediaType.APPLICATION_JSON));

        try {
            final URL url = withPath("/note");
            final HttpEntity<Note> httpEntity = new HttpEntity<>(note, httpHeaders);

            final ResponseEntity<Note> exchange = restTemplate.exchange(url.toString(), HttpMethod.PUT, httpEntity, Note.class);
            final Note createdNote = exchange.getBody();

            LOGGER.debug("Received note from server: {}", createdNote);
            return Optional.fromNullable(createdNote);
        } catch (Exception e) {
            LOGGER.error("Error while creating note at server: {}", e, note);
        }

        return Optional.absent();
    }

    @Override
    public Optional<Note> update(final Note note) {
        LOGGER.info("Update note '{}' in backend", note);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.setAccept(Lists.newArrayList(MediaType.APPLICATION_JSON));

        try {
            final URL url = withPath("/note");
            final HttpEntity<Note> httpEntity = new HttpEntity<>(note, httpHeaders);

            final ResponseEntity<Note> exchange = restTemplate.exchange(url.toString(), HttpMethod.PUT, httpEntity, Note.class);
            final Note updatedNote = exchange.getBody();

            LOGGER.debug("Successfully updated note '{}'", updatedNote);
            return Optional.fromNullable(updatedNote);
        } catch (Exception e) {
            LOGGER.error("Error while updating note '{}'", e, note);
        }

        return Optional.absent();
    }

    @Override
    public boolean solve(final String noteId) {
        LOGGER.info("Mark note '{}' as solved in backend", noteId);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        try {
            final URL url = withPath("/note/solve/" + noteId);
            final HttpEntity<String> httpEntity = new HttpEntity<>(httpHeaders);

            restTemplate.exchange(url.toString(), HttpMethod.POST, httpEntity, Void.class);

            LOGGER.debug("Successfully marked note '{}' as solved", noteId);
            return true;
        } catch (Exception e) {
            LOGGER.error("Error while marking note '{}' as solved", e, noteId);
        }

        return false;
    }

    @Override
    public boolean open(final String noteId) {
        LOGGER.info("Open solved note '{}' in backend", noteId);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        try {
            final URL url = withPath("/note/open/" + noteId);
            final HttpEntity<String> httpEntity = new HttpEntity<>(httpHeaders);

            restTemplate.exchange(url.toString(), HttpMethod.POST, httpEntity, Void.class);

            LOGGER.debug("Successfully opened solved note '{}'", noteId);
            return true;
        } catch (Exception e) {
            LOGGER.error("Error while opening solved note '{}'", e, noteId);
        }

        return false;
    }

    @Override
    public void delete(final Note note) {
    }

    public URL withPath(final String path) throws MalformedURLException {
        return new URL(baseUrl + path);
    }
}
