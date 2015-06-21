package de.inselhome.noteapp.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.common.base.Optional;

import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.inselhome.android.logging.AndroidLoggerFactory;
import de.inselhome.android.utils.UiUtils;
import de.inselhome.android.utils.list.SwipeDismissListViewTouchListener;
import de.inselhome.noteapp.NoteApp;
import de.inselhome.noteapp.R;
import de.inselhome.noteapp.adapter.note.NoteAdapter;
import de.inselhome.noteapp.adapter.note.NoteFilterAdapter;
import de.inselhome.noteapp.domain.Note;
import de.inselhome.noteapp.intent.CreateNoteIntent;
import de.inselhome.noteapp.data.NoteAppClient;
import de.inselhome.noteapp.task.LoadNotesTask;
import de.inselhome.noteapp.task.OpenNoteTask;
import de.inselhome.noteapp.task.SolveNoteTask;
import de.inselhome.noteapp.util.LogoutHandler;
import de.inselhome.noteapp.util.NoteFilter;
import de.inselhome.noteapp.widget.overview.OverviewWidgetProvider;

/**
 * @author iweinzierl
 */
public class NoteOverview extends Activity {

    private class NoteFilterManager {
        private final ListView tagList;
        private final ListView peopleList;
        private final NoteFilterAdapter tagAdapter;
        private final NoteFilterAdapter peopleAdapter;

        private NoteFilterManager(ListView tagList, ListView peopleList, NoteFilterAdapter tagAdapter, NoteFilterAdapter peopleAdapter) {
            this.tagList = tagList;
            this.peopleList = peopleList;
            this.tagAdapter = tagAdapter;
            this.peopleAdapter = peopleAdapter;
        }

        public Set<NoteFilter> getActiveFilters() {
            final long[] tagItemIds = tagList.getCheckedItemIds();
            final long[] peopleItemIds = peopleList.getCheckedItemIds();

            final Set<NoteFilter> tagFilters = tagAdapter.getItems(tagItemIds);
            final Set<NoteFilter> peopleFilters = peopleAdapter.getItems(peopleItemIds);

            final Set<NoteFilter> totalFilters = new HashSet<>();
            totalFilters.addAll(tagFilters);
            totalFilters.addAll(peopleFilters);

            return totalFilters;
        }
    }

    private class FilterItemSelectionListener implements AdapterView.OnItemClickListener {
        private final NoteFilterManager noteFilterManager;

        private FilterItemSelectionListener(final NoteFilterManager noteFilterManager) {
            this.noteFilterManager = noteFilterManager;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            filterNotes(noteFilterManager.getActiveFilters());
        }
    }

    private static final Logger LOGGER = AndroidLoggerFactory.getInstance("[NOTEAPP]").getLogger("NoteOverview");

    private static final int REQUEST_NEW_NOTE = 100;
    private static final int REQUEST_MODIFY_NOTE = 200;

    private View footer;
    private ListView tagFilterList;
    private ListView peopleFilterList;
    private ListView noteList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noteoverview);

        tagFilterList = (ListView) findViewById(R.id.tagFilterList);
        peopleFilterList = (ListView) findViewById(R.id.peopleFilterList);
        noteList = (ListView) findViewById(R.id.noteList);
        footer = findViewById(R.id.footer);

        final SwipeDismissListViewTouchListener dismissListener = setupSwipeDismissListener();
        noteList.setOnTouchListener(dismissListener);
        noteList.setOnScrollListener(dismissListener.makeScrollListener());
        noteList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final Note note = (Note) adapterView.getItemAtPosition(i);
                startModifyNote(note);
            }
        });

        setupFloatingAddButton();

        final NoteFilterAdapter tagFilterAdapter = new NoteFilterAdapter(this);
        final NoteFilterAdapter peopleFilterAdapter = new NoteFilterAdapter(this);
        final NoteFilterManager noteFilterManager = new NoteFilterManager(tagFilterList, peopleFilterList, tagFilterAdapter, peopleFilterAdapter);

        hideFooter(false);

        tagFilterList.setAdapter(tagFilterAdapter);
        tagFilterList.setOnItemClickListener(new FilterItemSelectionListener(noteFilterManager));

        peopleFilterList.setAdapter(peopleFilterAdapter);
        peopleFilterList.setOnItemClickListener(new FilterItemSelectionListener(noteFilterManager));
    }

    private SwipeDismissListViewTouchListener setupSwipeDismissListener() {
        return new SwipeDismissListViewTouchListener(noteList, new SwipeDismissListViewTouchListener.DismissCallbacks() {
            @Override
            public boolean canDismiss(int position) {
                return true;
            }

            @Override
            public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                removeNotes(reverseSortedPositions);
            }
        });
    }

    private void setupFloatingAddButton() {
        final View addNoteButton = findViewById(R.id.add);
        if (addNoteButton != null) {
            addNoteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new CreateNoteIntent(NoteOverview.this));
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        final NoteAppClient noteAppClient = NoteApp.get(this).getNoteAppClient();
        new LoadNotesTask(new LoadNotesTask.Handler() {
            @Override
            @SuppressWarnings("unchecked")
            public void onFinish(Optional<List<Note>> result) {
                final List<Note> notes = result.or(Collections.EMPTY_LIST);
                setNotes(notes);
                initFilters(notes);
            }
        }).execute(noteAppClient);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.noteoverview, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_new:
                startCreateNewNote();
                return true;

            case R.id.action_logout:
                new LogoutHandler(this).logout();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_NEW_NOTE:
                switch (resultCode) {
                    case RESULT_OK:
                        final Note note = new CreateNoteIntent(data).getNote();
                        if (note != null) {
                            ((NoteAdapter) noteList.getAdapter()).addItem(note);
                        }
                }
            case REQUEST_MODIFY_NOTE:
                switch (resultCode) {
                    case RESULT_OK:
                        final Note note = new CreateNoteIntent(data).getNote();
                        if (note != null) {
                            ((NoteAdapter) noteList.getAdapter()).replace(note);
                        }
                }
        }
    }

    private void setNotes(List<Note> notes) {
        noteList.setAdapter(new NoteAdapter(this, notes));
    }

    private void initFilters(final List<Note> notes) {
        final NoteFilterAdapter tagFilterAdapter = (NoteFilterAdapter) tagFilterList.getAdapter();
        final NoteFilterAdapter peopleFilterAdapter = (NoteFilterAdapter) peopleFilterList.getAdapter();

        final List<NoteFilter> tagsFilters = new ArrayList<>();
        final List<NoteFilter> peopleFilters = new ArrayList<>();

        for (final Note note : notes) {
            tagsFilters.addAll(NoteFilterAdapter.makeTagsFilters(note));
            peopleFilters.addAll(NoteFilterAdapter.makePeopleFilters(note));
        }

        tagFilterAdapter.addFilters(tagsFilters);
        tagFilterList.clearChoices();

        peopleFilterAdapter.addFilters(peopleFilters);
        peopleFilterList.clearChoices();

        LOGGER.info("Created {} filters in total", tagFilterAdapter.getCount() + peopleFilterAdapter.getCount());
    }

    private void startCreateNewNote() {
        LOGGER.debug("Start creating new note");
        startActivityForResult(new CreateNoteIntent(this), REQUEST_NEW_NOTE);
    }

    private void startModifyNote(final Note note) {
        final CreateNoteIntent intent = new CreateNoteIntent(this);
        intent.putExtra(CreateNoteActivity.EXTRA_NOTE_ID, note.getId());

        startActivityForResult(intent, REQUEST_MODIFY_NOTE);
    }

    private void removeNotes(final int[] positions) {

        final NoteAppClient noteAppClient = NoteApp.get(this).getNoteAppClient();
        final NoteAdapter noteAdapter = (NoteAdapter) noteList.getAdapter();

        final List<Note> toSolve = noteAdapter.getItems(positions);

        removeNotesFromList(positions);

        new SolveNoteTask(noteAppClient, new SolveNoteTask.Handler() {
            @Override
            public void onFinish(List<Note> successful, List<Note> failed) {
                LOGGER.info("Successfully marked {} notes as solved", successful.size());
                sendBroadcast(new Intent(OverviewWidgetProvider.UPDATE_ACTION));
                // TODO display failure
            }
        }).execute(toSolve.toArray(new Note[toSolve.size()]));

        showFooter(true);
        final CountDownTimer timer = new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long l) {
            }

            @Override
            public void onFinish() {
                hideFooter(true);
            }
        }.start();

        UiUtils.getGeneric(View.class, footer, R.id.undo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new OpenNoteTask(noteAppClient, new OpenNoteTask.Handler() {
                    @Override
                    public void onFinish(List<Note> successful, List<Note> failed) {
                        LOGGER.info("Successfully opened {} solved notes", successful.size());
                        // TODO display failure
                        noteAdapter.addItems(successful);
                        noteAdapter.notifyDataSetChanged();
                    }
                }).execute(toSolve.toArray(new Note[toSolve.size()]));

                hideFooter(true);
                timer.cancel();
            }
        });
    }

    private void showFooter(boolean animate) {
        if (animate) {
            ViewPropertyAnimator animator = footer.animate();
            animator.alpha(100);
            animator.setDuration(3000);
            animator.start();
        } else {
            footer.setAlpha(100);
        }
    }

    private void hideFooter(boolean animate) {
        if (animate) {
            ViewPropertyAnimator animator = footer.animate();
            animator.alpha(0);
            animator.setDuration(3000);
            animator.start();
        } else {
            footer.setAlpha(0);
        }
    }

    private void removeNotesFromList(final int[] positions) {
        final NoteAdapter noteAdapter = (NoteAdapter) noteList.getAdapter();

        for (final int pos : positions) {
            noteAdapter.remove(pos);
        }
    }

    private void filterNotes(final Set<NoteFilter> filter) {
        final NoteAdapter noteAdapter = (NoteAdapter) noteList.getAdapter();
        noteAdapter.setFilter(filter);
    }
}
