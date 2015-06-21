package de.inselhome.noteapp.data.impl.remote;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.springframework.http.HttpBasicAuthentication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import de.inselhome.android.logging.AndroidLoggerFactory;
import de.inselhome.noteapp.data.NoteAppClient;
import de.inselhome.noteapp.domain.Note;
import de.inselhome.noteapp.domain.sync.SyncAction;
import de.inselhome.noteapp.domain.sync.SyncTask;
import de.inselhome.noteapp.domain.sync.UnsyncedNote;
import de.inselhome.noteapp.exception.NoNetworkException;
import de.inselhome.noteapp.exception.PersistenceException;
import de.inselhome.noteapp.service.SyncRemoteService;

/**
 * @author iweinzierl
 */
public class RemoteNoteAppClient implements NoteAppClient {

    private static final Logger LOGGER = AndroidLoggerFactory.getInstance("[NOTEAPP]").getLogger("NoteappClientImpl");

    private final Context context;
    private final String baseUrl;
    private final RestTemplate restTemplate;
    private final SyncProvider syncProvider;

    private String username;
    private String password;

    public RemoteNoteAppClient(final Context context, final String baseUrl) {
        this.context = context;
        this.baseUrl = baseUrl;
        this.restTemplate = new RestTemplate();
        this.restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        this.syncProvider = new FileSyncProvider(context.getFilesDir());

        SyncRemoteService.start(context);
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
        syncProvider.setUsername(username);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<List<Note>> list() throws PersistenceException {
        LOGGER.info("Fetch note list from backend");

        checkInternetConnection();

        try {
            final URL url = withPath("/note");
            final HttpEntity<String> httpEntity = new HttpEntity<>(createBasicHttpHeaders());

            final ResponseEntity<Note[]> exchange = restTemplate.exchange(url.toString(), HttpMethod.GET, httpEntity, Note[].class);
            final Note[] notes = exchange.getBody();

            LOGGER.debug("Received {} notes from server", notes.length);
            return Optional.fromNullable(mergeWithSync(notes));
        } catch (Exception e) {
            LOGGER.error("Error while fetching notes from server: {}", e, e.getMessage());
            throw new PersistenceException("Unable to fetch notes from remote service", e);
        }
    }

    private List<Note> mergeWithSync(Note[] notes) {
        final ArrayList<Note> notesList = Lists.newArrayList(notes);

        try {
            notesList.addAll(Lists.transform(syncProvider.list(), new Function<SyncTask, Note>() {
                @Override
                public Note apply(SyncTask input) {
                    return new UnsyncedNote(input.getNote());
                }
            }));
        } catch (IOException e) {
            LOGGER.warn("Unable to add unsynced notes to list");
        }

        return notesList;
    }

    @Override
    public Optional<Note> create(final Note note) throws PersistenceException {
        LOGGER.info("Create note: {}", note);

        try {
            checkInternetConnection();

            final HttpHeaders httpHeaders = createBasicHttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            httpHeaders.setAccept(Lists.newArrayList(MediaType.APPLICATION_JSON));

            final URL url = withPath("/note");
            final HttpEntity<Note> httpEntity = new HttpEntity<>(note, httpHeaders);

            final ResponseEntity<Note> exchange = restTemplate.exchange(url.toString(), HttpMethod.PUT, httpEntity, Note.class);
            final Note createdNote = exchange.getBody();

            LOGGER.debug("Received note from server: {}", createdNote);
            return Optional.fromNullable(createdNote);
        } catch (NoNetworkException e) {
            syncProvider.add(SyncAction.CREATE, note);
            return Optional.of(note);
        } catch (Exception e) {
            LOGGER.error("Error while creating note at server: {}", e, note);
        }

        return Optional.absent();
    }

    @Override
    public Optional<Note> update(final Note note) throws PersistenceException {
        LOGGER.info("Update note '{}' in backend", note);

        try {
            checkInternetConnection();

            final HttpHeaders httpHeaders = createBasicHttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            httpHeaders.setAccept(Lists.newArrayList(MediaType.APPLICATION_JSON));

            final URL url = withPath("/note");
            final HttpEntity<Note> httpEntity = new HttpEntity<>(note, httpHeaders);

            final ResponseEntity<Note> exchange = restTemplate.exchange(url.toString(), HttpMethod.PUT, httpEntity, Note.class);
            final Note updatedNote = exchange.getBody();

            LOGGER.debug("Successfully updated note '{}'", updatedNote);
            return Optional.fromNullable(updatedNote);
        } catch (NoNetworkException e) {
            syncProvider.add(SyncAction.UPDATE, note);
        } catch (Exception e) {
            LOGGER.error("Error while updating note '{}'", e, note);
        }

        return Optional.absent();
    }

    @Override
    public boolean solve(final String noteId) throws PersistenceException {
        LOGGER.info("Mark note '{}' as solved in backend", noteId);

        try {
            checkInternetConnection();

            final HttpHeaders httpHeaders = createBasicHttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);

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
    public boolean open(final String noteId) throws PersistenceException {
        LOGGER.info("Open solved note '{}' in backend", noteId);

        try {
            checkInternetConnection();

            final HttpHeaders httpHeaders = createBasicHttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);

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
    public boolean delete(final Note note) {
        // TODO
        return false;
    }

    public void sync() throws IOException {
        final List<SyncTask> tasks = syncProvider.list();

        LOGGER.info("Starting sync of {} notes with remote service", tasks.size());

        new AsyncTask<SyncTask, Void, Void>() {
            @Override
            protected Void doInBackground(SyncTask... tasks) {
                int syncedTasks = 0;

                for (SyncTask task : tasks) {
                    switch (task.getSyncAction()) {
                        // TODO implement all sync actions
                        case CREATE:
                            try {
                                RemoteNoteAppClient.this.create(task.getNote());
                                syncProvider.remove(task);

                                syncedTasks++;
                            } catch (PersistenceException e) {
                                LOGGER.warn("Failed to sync note: {} -> {}", task.getSyncAction(), task.getNote());
                            }
                    }
                }

                LOGGER.info("Synced {} notes", syncedTasks);

                return null;
            }
        }.execute(tasks.toArray(new SyncTask[tasks.size()]));

    }

    private HttpHeaders createBasicHttpHeaders() {
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Lists.newArrayList(MediaType.APPLICATION_JSON));
        httpHeaders.setAuthorization(new HttpBasicAuthentication(username, password));

        LOGGER.info("Set Http headers with username = {}, password = {}", username, password);

        return httpHeaders;
    }


    public URL withPath(final String path) throws MalformedURLException {
        return new URL(baseUrl + path);
    }

    private void checkInternetConnection() {
        final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();

        if (activeNetworkInfo == null || !activeNetworkInfo.isAvailable()) {
            throw new NoNetworkException("No network connection to connect to remote service");
        }
    }
}
