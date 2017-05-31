package com.example.scplayer;


import android.app.SearchManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
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

public class PlaylistMain extends AppCompatActivity{

    private static final String TAG = "APP";
    private RecyclerView recycler;
    private PlaylistAdapter mPlaylistAdapter;
    private ArrayList<Playlist> mPlaylistArrayList;
    private int currentIndex;
    private boolean firstLaunch = true;
    private ProgressBar pb_main_loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);
        pb_main_loader = (ProgressBar) findViewById(R.id.pl_main_loader);
        recycler = (RecyclerView) findViewById(R.id.recycler_view);
        // запрос списка песен
        getPlayList("");

        mPlaylistArrayList = new ArrayList<>();

        recycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mPlaylistAdapter = new PlaylistAdapter(getApplicationContext(), mPlaylistArrayList, new PlaylistAdapter.RecyclerItemClickListener_PL() {
            @Override
            public void onClickListener(Playlist playlist, int position) {
                //firstLaunch = false;
                //changeSelectedSong(position);
                //prepareSong(song);
                Toast.makeText(PlaylistMain.this, "Нажатие на альбоме", Toast.LENGTH_SHORT).show();
            }
        });

        recycler.addItemDecoration(new GridSpacingItemDecoration_PL(2, dpToPx(10), true));
        recycler.setItemAnimator(new DefaultItemAnimator());

        recycler.setAdapter(mPlaylistAdapter);
    }

    public void getPlayList(String query){
        RequestQueue queue = VolleySingleton.getInstance(this).getRequestQueue();
        SoundcloudApiRequest request = new SoundcloudApiRequest(queue);
        pb_main_loader.setVisibility(View.VISIBLE);
        request.getPlaylist(query, new SoundcloudApiRequest.SoundcloudInterface_PL() {
            @Override
            public void onSucsess_PL(ArrayList<Playlist> playlists) {
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
}
