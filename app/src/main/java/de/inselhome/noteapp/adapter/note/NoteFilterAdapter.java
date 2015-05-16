package de.inselhome.noteapp.adapter.note;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import de.inselhome.android.utils.UiUtils;
import de.inselhome.noteapp.R;
import de.inselhome.noteapp.domain.Note;
import de.inselhome.noteapp.domain.Person;
import de.inselhome.noteapp.domain.Tag;
import de.inselhome.noteapp.util.NoteFilter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author iweinzierl
 */
public class NoteFilterAdapter extends BaseAdapter {

    private static class PeopleFilter implements NoteFilter {
        private final Person person;

        private PeopleFilter(final Person person) {
            this.person = person;
        }

        @Override
        public String getTextValue() {
            return person.getName();
        }

        @Override
        public boolean applies(Note note) {
            return note.getPeople().contains(person);
        }

        @Override
        public int hashCode() {
            return getTextValue().hashCode();
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof PeopleFilter && getTextValue().equals(((PeopleFilter) o).getTextValue());
        }
    }

    private static class TagsFilter implements NoteFilter {
        private final Tag tag;

        private TagsFilter(final Tag tag) {
            this.tag = tag;
        }

        @Override
        public String getTextValue() {
            return tag.getName();
        }

        @Override
        public boolean applies(Note note) {
            return note.getTags().contains(tag);
        }

        @Override
        public int hashCode() {
            return getTextValue().hashCode();
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof TagsFilter && getTextValue().equals(((TagsFilter) o).getTextValue());
        }
    }

    private static class ViewHolder {
        public TextView value;
    }

    private final Context context;
    private final List<NoteFilter> filters;

    public NoteFilterAdapter(final Context context) {
        this.context = context;
        this.filters = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return filters.size();
    }

    @Override
    public Object getItem(int position) {
        return filters.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        if (row == null) {
            row = setupRow(parent);
        }

        final NoteFilter filter = (NoteFilter) getItem(position);
        final ViewHolder viewHolder = (ViewHolder) row.getTag();

        UiUtils.setSafeText(viewHolder.value, R.id.value, filter.getTextValue());

        return row;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    public Set<NoteFilter> getItems(long[] itemIds) {
        final Set<NoteFilter> items = new HashSet<>();

        for (final long itemId : itemIds) {
            items.add((NoteFilter) getItem((int) itemId));
        }

        return items;
    }

    public void addFilters(final List<NoteFilter> filters) {
        for (final NoteFilter filter : filters) {
            if (!this.filters.contains(filter)) {
                this.filters.add(filter);
            }
        }

        notifyDataSetChanged();
    }

    private View setupRow(final ViewGroup parent) {
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);

        final View row = inflater.inflate(R.layout.list_item_filter, parent, false);

        final ViewHolder viewHolder = new ViewHolder();
        viewHolder.value = UiUtils.getGeneric(TextView.class, row, R.id.value);

        row.setTag(viewHolder);

        return row;
    }

    public static List<NoteFilter> makePeopleFilters(final Note note) {
        final List<NoteFilter> filters = new ArrayList<>();
        for (final Person person : note.getPeople()) {
            filters.add(new PeopleFilter(person));
        }

        return filters;
    }

    public static List<NoteFilter> makeTagsFilters(final Note note) {
        final List<NoteFilter> filters = new ArrayList<>();
        for (final Tag tag : note.getTags()) {
            filters.add(new TagsFilter(tag));
        }

        return filters;
    }
}
