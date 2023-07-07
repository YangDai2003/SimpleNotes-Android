package com.example.notesapp;

import static com.example.notesapp.FileUtils.deleteSingleFile;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.notesapp.Adapters.NotesListAdapter;
import com.example.notesapp.DataBase.RoomDB;
import com.example.notesapp.Models.Notes;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.elevation.SurfaceColors;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 30415
 */
public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    ActivityResultLauncher<Intent> intentActivityResultLauncher1, intentActivityResultLauncher2,
    intentActivityResultLauncher;
    RecyclerView recyclerView;
    NotesListAdapter notesListAdapter;
    SwipeRefreshLayout swipeRefreshLayout;
    private List<Notes> notes = new ArrayList<>();
    RoomDB database;
    ExtendedFloatingActionButton extendedFloatingActionButton;
    SearchView searchView;
    private Notes selectedNotes;
    private int mPosition;
    private boolean filter = false;
    SharedPreferences sharedPreferences;
    StaggeredGridLayoutManager layoutManager;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.info, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.song_detail_toolbar_menu_info) {
            searchView.clearFocus();
            Intent intent = new Intent(this, SettingsActivity.class);
            intentActivityResultLauncher.launch(intent);
            overridePendingTransition(R.anim.in, R.anim.stay);
        } else if (item.getItemId() == R.id.filter_menu) {
            if (!filter) {
                filterPin();
                filter = true;
                item.setIcon(R.drawable.baseline_filter_list_off_24);
            } else {
                notesListAdapter.filteredList(notes);
                filter = false;
                item.setIcon(R.drawable.baseline_filter_list_24);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DynamicColors.applyToActivityIfAvailable(this);
        getWindow().setStatusBarColor(SurfaceColors.SURFACE_2.getColor(this));
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);

        swipeRefreshLayout = findViewById(R.id.refresh);
        recyclerView = findViewById(R.id.recycler_view);
        extendedFloatingActionButton = findViewById(R.id.fab_add_btn);
        searchView = findViewById(R.id.search_view);

        swipeRefreshLayout.setOnRefreshListener(this::onRefresh);
        swipeRefreshLayout.setDistanceToTriggerSync((int) (getResources().getDisplayMetrics().density * 200));
        swipeRefreshLayout.setProgressViewEndTarget(true, 400);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        database = RoomDB.getInstance(this);
        notes = database.dao().getAll();
        updateRecycler(notes);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                notesListAdapter.setScrollUp(dy > 10);
                if (dy != 0) {
                    searchView.clearFocus();
                }
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (layoutManager != null) {
                    int[] arr = layoutManager.findFirstVisibleItemPositions(null);
                    List<Integer> list = Arrays.stream(arr).boxed().collect(Collectors.toList());
                    if (list.contains(0) || notes.isEmpty()) {
                        extendedFloatingActionButton.extend();
                    } else {
                        extendedFloatingActionButton.shrink();
                    }
                }
            }
        });

        extendedFloatingActionButton.setOnClickListener(view -> {
            searchView.clearFocus();
            Intent intent = new Intent(this, NotesActivity.class);
            intentActivityResultLauncher1.launch(intent);
            overridePendingTransition(R.anim.in, R.anim.stay);
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterStr(newText);
                return false;
            }
        });
        intentActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), res -> {
            if (res.getResultCode() == Activity.RESULT_OK) {
                notes = database.dao().getAll();
                updateRecycler(notes);
            }
        });
        //2点击note  1点击button
        intentActivityResultLauncher1 = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), res -> {
            if (res.getResultCode() == Activity.RESULT_OK) {
                Notes newNotes = null;
                if (res.getData() != null) {
                    newNotes = (Notes) res.getData().getSerializableExtra("note");
                }
                long id = database.dao().insert(newNotes);
                if (newNotes != null) {
                    newNotes.setID(id);
                }
                notes.add(0, newNotes);
                notesListAdapter.notifyItemInserted(0);
            }
        });
        intentActivityResultLauncher2 = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), res -> {
            if (res.getResultCode() == Activity.RESULT_OK) {
                Notes newNotes = null;
                if (res.getData() != null) {
                    newNotes = (Notes) res.getData().getSerializableExtra("note");
                }
                if (newNotes != null) {
                    database.dao().update(newNotes);
                }
                notes.clear();
                notes.addAll(database.dao().getAll());
                notesListAdapter.notifyItemChanged(mPosition);
            }
        });
    }

    protected void onRefresh() {//刷新
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            notes = database.dao().getAll();
            updateRecycler(notes);
            swipeRefreshLayout.setRefreshing(false);//刷新旋转动画停止
        }, 1000);
    }

    ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            int position = viewHolder.getAdapterPosition();
            if (notes.get(position).isPinned()) {
                return 0;
            }
            int swiped = ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT;
            //第一个参数拖动，第二个删除侧滑
            return makeMovementFlags(0, swiped);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            new MaterialAlertDialogBuilder(MainActivity.this)
                    .setTitle(getString(R.string.delete))//标题
                    .setMessage(getString(R.string.sure))//内容
                    .setIcon(R.mipmap.ic_launcher)//图标
                    .setCancelable(false)
                    .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> notesListAdapter.notifyItemChanged(position))
                    .setPositiveButton(getString(R.string.delete), (dialog, which) -> {
                        database.dao().delete(notes.get(position));
                        List<String> paths = Arrays.asList(notes.get(position).getImage().trim().split(" "));
                        for (int i = 0; i < paths.size(); ++i) {
                            deleteSingleFile(paths.get(i));
                        }
                        notes.remove(position);
                        notesListAdapter.notifyItemRemoved(position);
                        Toast.makeText(MainActivity.this, getString(R.string.deleted), Toast.LENGTH_SHORT).show();
                    })
                    .show();
        }
    });


    private void filterStr(String newText) {
        List<Notes> filterList = new ArrayList<>();
        for (Notes singleNote : notes) {
            if (singleNote.getTitle().toLowerCase().contains(newText.toLowerCase())
                    || singleNote.getNotes().toLowerCase().contains(newText.toLowerCase())) {
                filterList.add(singleNote);
            }
        }
        notesListAdapter.filteredList(filterList);
    }

    private void filterPin() {
        List<Notes> filterList = new ArrayList<>();
        for (Notes singleNote : notes) {
            if (singleNote.isPinned()) {
                filterList.add(singleNote);
            }
        }
        notesListAdapter.filteredList(filterList);
    }

    private void updateRecycler(List<Notes> notes) {
        recyclerView.setHasFixedSize(true);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        } else {
            layoutManager = new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL);
        }
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        recyclerView.setLayoutManager(layoutManager);
        notesListAdapter = new NotesListAdapter(notes, notesClick);
        recyclerView.setAdapter(notesListAdapter);
    }

    private final NotesClick notesClick = new NotesClick() {
        @Override
        public void onClick(Notes notes, int position) {
            searchView.clearFocus();
            mPosition = position;
            Intent intent = new Intent(MainActivity.this, NotesActivity.class);
            intent.putExtra("old_note", notes);
            intentActivityResultLauncher2.launch(intent);
            overridePendingTransition(R.anim.in, R.anim.stay);
        }

        @Override
        public void onLongClick(Notes notes, CardView cardView, int position) {
            searchView.clearFocus();
            selectedNotes = notes;
            mPosition = position;
            showPopup(cardView);
        }
    };

    private void showPopup(CardView cardView) {
        PopupMenu popupMenu = new PopupMenu(this, cardView);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.pop_up);
        popupMenu.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.pin) {
            database.dao().pin(selectedNotes.getID(), !selectedNotes.isPinned());
            int idx = notes.indexOf(selectedNotes);
            notes.remove(selectedNotes);
            selectedNotes.setPinned(!selectedNotes.isPinned());
            notes.add(idx, selectedNotes);
            notesListAdapter.notifyItemChanged(mPosition);
            return true;
        } else if (item.getItemId() == R.id.del) {
            if (selectedNotes.isPinned()) {
                Toast.makeText(MainActivity.this, getString(R.string.toast), Toast.LENGTH_SHORT).show();
            } else {
                new MaterialAlertDialogBuilder(this)
                        .setTitle(getString(R.string.delete))//标题
                        .setMessage(getString(R.string.sure))//内容
                        .setIcon(R.mipmap.ic_launcher)//图标
                        .setCancelable(false)
                        .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {

                        })
                        .setPositiveButton(getString(R.string.delete), (dialog, which) -> {
                            database.dao().delete(selectedNotes);
                            List<String> paths = Arrays.asList(selectedNotes.getImage().trim().split(" "));
                            for (int i = 0; i < paths.size(); ++i) {
                                deleteSingleFile(paths.get(i));
                            }
                            notes.remove(selectedNotes);
                            notesListAdapter.notifyItemRemoved(mPosition);
                            Toast.makeText(this, getString(R.string.deleted), Toast.LENGTH_SHORT).show();
                        })
                        .show();
            }
            return true;
        }
        return false;
    }
}