package de.inselhome.noteapp.adapter.note;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import de.inselhome.android.logging.AndroidLoggerFactory;
import de.inselhome.android.utils.UiUtils;
import de.inselhome.noteapp.R;
import de.inselhome.noteapp.domain.Note;
import de.inselhome.noteapp.util.ColorProvider;
import de.inselhome.noteapp.util.NoteFilter;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
        this.visibleNotes = new ArrayList<>(notes);
    }

    @Override
    public int getCount() {
        return visibleNotes.size();
    }

    public void addItem(final Note note) {
        notes.add(note);
        visibleNotes.add(note);
    }

    public void addItems(final List<Note> notes) {
        this.notes.addAll(notes);
        visibleNotes.addAll(notes);
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

        UiUtils.setSafeHtmlText(viewHolder.description, R.id.description, colorText(note.getDescription()));
        UiUtils.setSafeText(viewHolder.creation, R.id.creation, note.getCreation().toString());

        return row;
    }

    private Spannable colorText(final String text) {
        final SpannableStringBuilder builder = new SpannableStringBuilder(text);
        colorText(builder, text, "#");
        colorText(builder, text, "@");
        return builder;
    }

    private Spannable colorText(final SpannableStringBuilder builder, final String text, final String keyChar) {
        int index = -1;

        do {
            index = colorText(builder, text, index + 1, keyChar);
        }
        while (index >= 0);

        return builder;
    }

    private int colorText(final SpannableStringBuilder builder, final String text, final int start, final String keyChar) {
        final int index = text.indexOf(keyChar, start);

        if (index >= 0) {
            final int space = text.indexOf(" ", index);

            if (space > index) {
                final String substring = text.substring(index, space);
                builder.setSpan(new ForegroundColorSpan(ColorProvider.fromString(substring)), index, space, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            } else {
                final String substring = text.substring(index, text.length());
                builder.setSpan(new ForegroundColorSpan(ColorProvider.fromString(substring)), index, text.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            }
        }

        return index;
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
        return notes.remove(pos);
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
