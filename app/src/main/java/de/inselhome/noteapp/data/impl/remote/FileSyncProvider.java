package de.inselhome.noteapp.data.impl.remote;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.inselhome.noteapp.domain.Note;
import de.inselhome.noteapp.domain.sync.SyncAction;
import de.inselhome.noteapp.domain.sync.SyncTask;
import de.inselhome.noteapp.exception.PersistenceException;
import de.inselhome.noteapp.util.FileUtils;
import de.inselhome.noteapp.util.JsonUtils;

public class FileSyncProvider implements SyncProvider {

    private static final String SYNC_FILENAME = "file.sync.provider.";

    private final File directory;
    private String username;

    public FileSyncProvider(File directory) {
        this.directory = directory;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public void add(final SyncAction action, final Note note) throws PersistenceException {
        try {
            final List<SyncTask> syncTasks = list();

            note.setId(UUID.randomUUID().toString());

            final SyncTask newTask = new SyncTask();
            newTask.setSyncAction(action);
            newTask.setNote(note);

            syncTasks.add(newTask);

            writeSyncTasks(syncTasks);
        } catch (IOException e) {
            throw new PersistenceException("Unable to add note to sync file", e);
        }
    }

    @Override
    public void remove(final SyncTask task) throws PersistenceException {
        try {
            final List<SyncTask> syncTasks = list();

            final List<SyncTask> filteredTasks = Lists.newArrayList(Iterables.filter(syncTasks, new Predicate<SyncTask>() {
                @Override
                public boolean apply(SyncTask input) {
                    return task.getNote().getId().equals(input.getNote().getId());
                }
            }));

            writeSyncTasks(filteredTasks);
        }
        catch (IOException e) {
            throw new PersistenceException("Unable to remove note from sync file", e);
        }
    }

    @Override
    public List<SyncTask> list() throws IOException {
        final File syncFile = getSyncFile();
        if (syncFile.exists()) {
            final String syncJson = FileUtils.fromFile(syncFile);
            final SyncTask[] read = JsonUtils.read(SyncTask[].class, syncJson);

            return Lists.newArrayList(read);
        }
        else {
            return new ArrayList<>();
        }
    }

    private File getSyncFile() {
        return new File(directory, SYNC_FILENAME + username);
    }

    private void writeSyncTasks(final List<SyncTask> syncTasks) throws IOException {
        final String syncJson = JsonUtils.toJson(syncTasks);
        FileUtils.toFile(getSyncFile(), syncJson);
    }
}
