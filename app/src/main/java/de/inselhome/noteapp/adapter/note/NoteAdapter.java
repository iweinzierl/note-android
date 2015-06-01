package de.inselhome.noteapp.adapter.note;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import de.inselhome.android.logging.AndroidLoggerFactory;
import de.inselhome.android.utils.UiUtils;
import de.inselhome.noteapp.R;
import de.inselhome.noteapp.domain.Note;
import de.inselhome.noteapp.util.ColorProvider;
import de.inselhome.noteapp.util.NoteFilter;

/**
 * @author iweinzierl
 */
public class NoteAdapter extends BaseAdapter {

    private static final Logger LOGGER = AndroidLoggerFactory.getInstance("[NOTEAPP]").getLogger("NoteAdapter");

    private final Context context;
    private final List<Note> notes;

    private List<Note> visibleNotes;
    private Set<NoteFilter> filters;

    private static class ViewHolder {
        public TextView description;
        public TextView creation;
    }

    public NoteAdapter(final Context context, final List<Note> notes) throws NullPointerException {
        Preconditions.checkNotNull(context);
        Preconditions.checkNotNull(notes);
        this.context = context;
        this.notes = new ArrayList<>(notes);
        this.visibleNotes = newSortedList(notes);
    }

    @Override
    public int getCount() {
        return visibleNotes.size();
    }

    public void addItem(final Note note) {
        notes.add(note);
        visibleNotes.add(note);
        Collections.sort(visibleNotes, new Comparator<Note>() {
            @Override
            public int compare(Note note, Note t1) {
                return note.getCreation().compareTo(t1.getCreation());
            }
        });
        notifyDataSetChanged();
    }

    public void addItems(final List<Note> notes) {
        this.notes.addAll(notes);
        visibleNotes.addAll(notes);
        Collections.sort(visibleNotes, new Comparator<Note>() {
            @Override
            public int compare(Note note, Note t1) {
                return note.getCreation().compareTo(t1.getCreation());
            }
        });
        notifyDataSetChanged();
    }

    @Override
    public Object getItem(final int position) {
        return visibleNotes.get(position);
    }

    public List<Note> getItems(final int[] positions) {
        final List<Note> copy = new ArrayList<>(positions.length);
        for (int pos : positions) {
            copy.add((Note) getItem(pos));
        }

        return copy;
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        View row = convertView;

        if (row == null) {
            row = setupRow(parent);
        }

        final Note note = (Note) getItem(position);
        final ViewHolder viewHolder = (ViewHolder) row.getTag();

        UiUtils.setSafeHtmlText(viewHolder.description, R.id.description, ColorProvider.colorText(note.getDescription()));
        UiUtils.setSafeText(viewHolder.creation, R.id.creation, note.getCreation().toString());

        return row;
    }

    private List<Note> newSortedList(final List<Note> notes) {
        final ArrayList<Note> notesClone = new ArrayList<>(notes);
        Collections.sort(notesClone, new Comparator<Note>() {
            @Override
            public int compare(Note a, Note b) {
                return a.getCreation().compareTo(b.getCreation());
            }
        });

        return notesClone;
    }

    private View setupRow(final ViewGroup parent) {
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);

        final View row = inflater.inflate(R.layout.list_item_note, parent, false);

        final ViewHolder viewHolder = new ViewHolder();
        viewHolder.description = UiUtils.getGeneric(TextView.class, row, R.id.description);
        viewHolder.creation = UiUtils.getGeneric(TextView.class, row, R.id.creation);

        row.setTag(viewHolder);

        return row;
    }

    public Note remove(final int pos) {
        visibleNotes.remove(pos);
        Note note = notes.remove(pos);

        notifyDataSetChanged();
        return note;
    }

    public void setFilter(final Set<NoteFilter> filter) {
        this.filters = Sets.newHashSet(filter);
        filter();
    }

    private void filter() {
        final List<Note> newVisible = new ArrayList<>();

        for (final Note note : notes) {
            if (appliesFilter(note)) {
                newVisible.add(note);
            }
        }

        visibleNotes = newVisible;
        notifyDataSetChanged();
    }

    private boolean appliesFilter(final Note note) {
        if (filters == null || filters.isEmpty()) {
            LOGGER.debug("no filters set - applying every note");
            return true;
        }

        for (final NoteFilter filter : filters) {
            if (!filter.applies(note)) {
                return false;
            }
        }

        return true;
    }
}
