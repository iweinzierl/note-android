package de.inselhome.noteapp.adapter.note;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import de.inselhome.android.utils.UiUtils;
import de.inselhome.noteapp.R;
import de.inselhome.noteapp.domain.HasName;
import de.inselhome.noteapp.domain.Note;
import de.inselhome.noteapp.domain.Person;
import de.inselhome.noteapp.domain.Tag;

public class AutoCompleteAdapter implements ListAdapter, Filterable {

    private interface FilterCallback {
        void onFilterResult(Collection<HasName> filterResults);
    }

    private class AutoCompleteFilter extends Filter {
        private final List<HasName> hasNames;
        private final FilterCallback callback;

        public AutoCompleteFilter(List<HasName> hasNames, FilterCallback callback) {
            this.hasNames = hasNames;
            this.callback = callback;
        }

        @Override
        protected FilterResults performFiltering(final CharSequence charSequence) {
            Collection<HasName> filtered = Collections2.filter(hasNames, new Predicate<HasName>() {
                @Override
                public boolean apply(HasName input) {
                    return input.getName().toLowerCase().contains(charSequence.toString().toLowerCase());
                }
            });

            FilterResults results = new FilterResults();
            results.count = filtered.size();
            results.values = filtered;

            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
           callback.onFilterResult((Collection<HasName>) filterResults.values);
        }
    }

    private class ViewHolder {
        public TextView text;
    }

    private Context context;
    private List<HasName> total;
    private List<HasName> data;
    private String text;

    public AutoCompleteAdapter(final Context context) {
        this.context = context;
        this.total = new ArrayList<>();
        this.data = new ArrayList<>();
    }

    @Override
    public android.widget.Filter getFilter() {
        return new AutoCompleteFilter(this.total, new FilterCallback() {
            @Override
            public void onFilterResult(Collection<HasName> filterResults) {
                data.clear();

                if (filterResults != null && filterResults.size() > 0) {
                    data.addAll(filterResults);
                }
            }
        });
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int i) {
        return true;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver dataSetObserver) {
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
    }

    @Override
    public int getCount() {
        return data == null ? 0 : total.size();
    }

    @Override
    public Object getItem(int i) {
        return data == null ? null : total.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        View row = view;

        if (row == null) {
            row = setupRow(viewGroup);
        }

        final HasName hasName = (HasName) getItem(position);
        final ViewHolder viewHolder = (ViewHolder) row.getTag();

        UiUtils.setSafeText(viewHolder.text, R.id.value, hasName.getName());

        return row;
    }

    @Override
    public int getItemViewType(int i) {
        return 1;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return data == null;
    }

    public void setData(final List<Note> notes) {
        this.total.clear();
        this.total.addAll(createData(notes));
    }

    private List<HasName> createData(final List<Note> notes) {
        final List<HasName> hasNames = new ArrayList<>();

        for (Note note : notes) {
            hasNames.addAll(createPeopleFilter(note.getPeople()));
            hasNames.addAll(createTagFilter(note.getTags()));
        }

        return hasNames;
    }

    private List<HasName> createPeopleFilter(Set<Person> people) {
        final List<HasName> hasNames = new ArrayList<>();

        for (Person person : people) {
            hasNames.add(person);
        }

        return hasNames;
    }

    private List<HasName> createTagFilter(Set<Tag> tags) {
        final List<HasName> hasNames = new ArrayList<>();

        for (Tag tag : tags) {
            hasNames.add(tag);
        }

        return hasNames;
    }

    private View setupRow(final ViewGroup parent) {
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);

        final View row = inflater.inflate(R.layout.list_item_suggestion, parent, false);

        final ViewHolder viewHolder = new ViewHolder();
        viewHolder.text = UiUtils.getGeneric(TextView.class, row, R.id.value);

        row.setTag(viewHolder);

        return row;
    }
}
