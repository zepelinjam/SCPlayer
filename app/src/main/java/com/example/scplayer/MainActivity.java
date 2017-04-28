package com.example.scplayer;

import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.RequestQueue;
import com.example.scplayer.Adapter.SongAdapter;
import com.example.scplayer.Model.Song;
import com.example.scplayer.Request.SoundcloudApiRequest;
import com.example.scplayer.Utility.Utility;
import org.w3c.dom.Text;
import java.io.IOException;
import java.util.ArrayList;
import android.app.SearchManager;
import android.widget.SearchView.OnQueryTextListener;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "APP";
    private RecyclerView recycler;
    private SongAdapter mAdapter;
    private ArrayList<Song> songList;
    private int currentIndex;
    private TextView tb_title, tb_duration, tv_time;
    private ImageView iv_play, iv_next, iv_previous;
    private ProgressBar pb_loader, pb_main_loader;
    private MediaPlayer mediaPlayer;
    private long currentSongLength;
    private SeekBar seekBar;
    private boolean firstLaunch = true;
    private FloatingActionButton fab_search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // просмотр иницализаций
        initializeViews();
        // запрос списка песен
        getSongList("");

        songList = new ArrayList<>();

        recycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mAdapter = new SongAdapter(getApplicationContext(), songList, new SongAdapter.RecyclerItemClickListener() {
            @Override
            public void onClickListener(Song song, int position) {
                firstLaunch = false;
                changeSelectedSong(position);
                prepareSong(song);
            }
        });
        recycler.setAdapter(mAdapter);

        // инициализация медиа-плеера
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                //Lancer la chanson
                togglePlay(mp);
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if(currentIndex + 1 < songList.size()){
                    Song next = songList.get(currentIndex + 1);
                    changeSelectedSong(currentIndex+1);
                    prepareSong(next);
                }else{
                    Song next = songList.get(0);
                    changeSelectedSong(0);
                    prepareSong(next);
                }
            }
        });

        // seekbar управления
        handleSeekbar();

        // Управление песней
        pushPlay();
        pushPrevious();
        pushNext();

        // Управление по нажатию на кнопку поиска
        fab_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDialog();
            }
        });
    }
    // обработчик меню (поиск)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        if(null!=searchManager ) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        }
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            // обработчик ввода текста
            @Override
            public boolean onQueryTextChange(String newText) {
                getSongList(newText); // передаем обработчику введенный текст поиска
                return false;
            }
        });
        return true;
    }
    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);
        MenuItem item = menu.findItem(R.id.menuSearch);
        SearchView searchView = (SearchView)item.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }*/

    private void handleSeekbar(){
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer.seekTo(progress * 1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void prepareSong(Song song){

        currentSongLength = song.getDuration();
        pb_loader.setVisibility(View.VISIBLE);
        tb_title.setVisibility(View.GONE);
        iv_play.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.selector_play));
        tb_title.setText(song.getTitle());
        tv_time.setText(Utility.convertDuration(song.getDuration()));
        String stream = song.getStreamUrl()+"?client_id="+Config.CLIENT_ID;
        mediaPlayer.reset();

        try {
            mediaPlayer.setDataSource(stream);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void togglePlay(MediaPlayer mp){

        if(mp.isPlaying()){
            mp.stop();
            mp.reset();
        }else{
            pb_loader.setVisibility(View.GONE);
            tb_title.setVisibility(View.VISIBLE);
            mp.start();
            iv_play.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.selector_pause));
            final Handler mHandler = new Handler();
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    seekBar.setMax((int) currentSongLength / 1000);
                    int mCurrentPosition = mediaPlayer.getCurrentPosition() / 1000;
                    seekBar.setProgress(mCurrentPosition);
                    tv_time.setText(Utility.convertDuration((long)mediaPlayer.getCurrentPosition()));
                    mHandler.postDelayed(this, 1000);

                }
            });
        }

    }


    private void initializeViews(){

        tb_title = (TextView) findViewById(R.id.tb_title);
        iv_play = (ImageView) findViewById(R.id.iv_play);
        iv_next = (ImageView) findViewById(R.id.iv_next);
        iv_previous = (ImageView) findViewById(R.id.iv_previous);
        pb_loader = (ProgressBar) findViewById(R.id.pb_loader);
        pb_main_loader = (ProgressBar) findViewById(R.id.pb_main_loader);
        recycler = (RecyclerView) findViewById(R.id.recycler);
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        tv_time = (TextView) findViewById(R.id.tv_time);
        fab_search = (FloatingActionButton) findViewById(R.id.fab_search);

    }

    public void getSongList(String query){
        RequestQueue queue = VolleySingleton.getInstance(this).getRequestQueue();
        SoundcloudApiRequest request = new SoundcloudApiRequest(queue);
        pb_main_loader.setVisibility(View.VISIBLE);
        request.getSongList(query, new SoundcloudApiRequest.SoundcloudInterface() {
            @Override
            public void onSuccess(ArrayList<Song> songs) {
                currentIndex = 0;
                pb_main_loader.setVisibility(View.GONE);
                songList.clear();
                songList.addAll(songs);
                mAdapter.notifyDataSetChanged();
                mAdapter.setSelectedPosition(0);

            }

            @Override
            public void onError(String message) {
                pb_main_loader.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void changeSelectedSong(int index){
        mAdapter.notifyItemChanged(mAdapter.getSelectedPosition());
        currentIndex = index;
        mAdapter.setSelectedPosition(currentIndex);
        mAdapter.notifyItemChanged(currentIndex);
    }

    private void pushPlay(){
        iv_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mediaPlayer.isPlaying() && mediaPlayer != null){
                    iv_play.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.selector_play));
                    mediaPlayer.pause();
                }else{
                    if(firstLaunch){
                        Song song = songList.get(0);
                        changeSelectedSong(0);
                        prepareSong(song);
                    }else{
                        mediaPlayer.start();
                        firstLaunch = false;
                    }
                    iv_play.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.selector_pause));
                }

            }
        });
    }

    private void pushPrevious(){

        iv_previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firstLaunch = false;
                if(mediaPlayer != null){

                    if(currentIndex - 1 >= 0){
                        Song previous = songList.get(currentIndex - 1);
                        changeSelectedSong(currentIndex - 1);
                        prepareSong(previous);
                    }else{
                        changeSelectedSong(songList.size() - 1);
                        prepareSong(songList.get(songList.size() - 1));
                    }

                }
            }
        });

    }

    private void pushNext(){

        iv_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firstLaunch = false;
                if(mediaPlayer != null){

                    if(currentIndex + 1 < songList.size()){
                        Song next = songList.get(currentIndex + 1);
                        changeSelectedSong(currentIndex + 1);
                        prepareSong(next);
                    }else{
                        changeSelectedSong(0);
                        prepareSong(songList.get(0));
                    }

                }
            }
        });

    }

    public void createDialog(){

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        final View view = getLayoutInflater().inflate(R.layout.dialog_search, null);
        builder.setTitle(R.string.search);
        builder.setView(view);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText et_search = (EditText) view.findViewById(R.id.et_search);
                String search = et_search.getText().toString().trim();
                if(search.length() > 0){
                    getSongList(search);
                }else{
                    Toast.makeText(MainActivity.this, "Пожалуйста, заполните поле", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.create().show();


    }

    @Override
    protected void onDestroy() {
        if(mediaPlayer != null){
            mediaPlayer.release();
        }
        super.onDestroy();
    }
}

