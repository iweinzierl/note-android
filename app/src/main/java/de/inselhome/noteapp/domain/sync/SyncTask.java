package de.inselhome.noteapp.domain.sync;

import de.inselhome.noteapp.domain.Note;

public class SyncTask {

    private SyncAction syncAction;
    private Note note;

    public SyncAction getSyncAction() {
        return syncAction;
    }

    public void setSyncAction(SyncAction syncAction) {
        this.syncAction = syncAction;
    }

    public Note getNote() {
        return note;
    }

    public void setNote(Note note) {
        this.note = note;
    }
}
