package com.example.scplayer;


import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.RequestQueue;
import com.example.scplayer.Adapter.PlaylistAdapter;
import com.example.scplayer.Model.Playlist;
import com.example.scplayer.Model.Song;
import com.example.scplayer.Request.SoundcloudApiRequest;
import com.example.scplayer.Utility.Utility;
import com.mikepenz.iconics.typeface.FontAwesome;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import java.io.IOException;
import java.util.ArrayList;

public class PlaylistMain extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "APP";
    private RecyclerView recycler;
    private PlaylistAdapter mPlaylistAdapter;
    private ArrayList<Playlist> mPlaylistArrayList;
    private int currentIndex;
    private ProgressBar pb_main_loader;
    private Toolbar toolbar;
    private Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        initializeViews();
        // запрос списка песен
        getPlayList("");

        mPlaylistArrayList = new ArrayList<>();

        // Настраиваем адаптер для спиннера
        ArrayAdapter<?> adapter = ArrayAdapter.createFromResource(this, R.array.genres, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Вызываем адаптер
        spinner.setSelection(0); // жанр по умолчанию "All"
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = spinner.getSelectedItem().toString();
                getPlayList(selected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        recycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mPlaylistAdapter = new PlaylistAdapter(getApplicationContext(), mPlaylistArrayList, new PlaylistAdapter.RecyclerItemClickListener_PL() {
            @Override
            public void onClickListener(Playlist playlist, int position) {
                //firstLaunch = false;
                //changeSelectedSong(position);
                //prepareSong(song);
                Toast.makeText(PlaylistMain.this, "Нажатие на альбоме", Toast.LENGTH_SHORT).show();

                // место для кода перехода к другому активити
            }
        });

        recycler.addItemDecoration(new GridSpacingItemDecoration_PL(2, dpToPx(10), true));
        recycler.setItemAnimator(new DefaultItemAnimator());

        recycler.setAdapter(mPlaylistAdapter);

        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    // иницализация вьюшек
    private void initializeViews(){
        pb_main_loader = (ProgressBar) findViewById(R.id.pl_main_loader);
        recycler = (RecyclerView) findViewById(R.id.recycler_view); // RecyclerView
        toolbar = (Toolbar) findViewById(R.id.toolbarPL); // Тулбар
        spinner = (Spinner) findViewById(R.id.spinner); // Spinner на Тулбаре
    }

    public void getPlayList(String query){
        RequestQueue queue = VolleySingleton.getInstance(this).getRequestQueue();
        SoundcloudApiRequest request = new SoundcloudApiRequest(queue);
        pb_main_loader.setVisibility(View.VISIBLE);
        request.getPlaylist(query, new SoundcloudApiRequest.SoundcloudInterface_PL() {
            @Override
            public void onSucsess_PL(ArrayList<Playlist> playlists, ArrayList<Song> songs) {
                currentIndex = 0;
                pb_main_loader.setVisibility(View.GONE);
                mPlaylistArrayList.clear();
                mPlaylistArrayList.addAll(playlists);
                mPlaylistAdapter.notifyDataSetChanged();
                mPlaylistAdapter.setSelectedPosition(0);
            }

            @Override
            public void onError_PL(String message) {
                pb_main_loader.setVisibility(View.GONE);
                Toast.makeText(PlaylistMain.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * RecyclerView item decoration - give equal margin around grid item
     */
    public class GridSpacingItemDecoration_PL extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration_PL (int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    private void changeSelectedSong(int index){
        mPlaylistAdapter.notifyItemChanged(mPlaylistAdapter.getSelectedPosition());
        currentIndex = index;
        mPlaylistAdapter.setSelectedPosition(currentIndex);
        mPlaylistAdapter.notifyItemChanged(currentIndex);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
