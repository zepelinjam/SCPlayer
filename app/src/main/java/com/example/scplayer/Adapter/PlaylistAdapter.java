package com.example.scplayer.Adapter;


import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.scplayer.Model.Playlist;
import com.example.scplayer.R;
import com.example.scplayer.Utility.Utility;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {

    private Context context;
    private ArrayList<Playlist> mPlaylistArrayList;
    private RecyclerItemClickListener_PL mListenerPl;
    private int selectedPosition;

    public PlaylistAdapter(Context context, ArrayList<Playlist> playlists,  RecyclerItemClickListener_PL listener){

        this.context = context;
        this.mPlaylistArrayList = playlists;
        this.mListenerPl = listener;

    }

    @Override
    public PlaylistViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_card, parent, false);

        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PlaylistViewHolder playlistViewHolder, int position) {

        Playlist playlist = mPlaylistArrayList.get(position);
        if(playlist != null){ // если плейлист не пуст, ...
            /*
            if(selectedPosition == position){
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
                holder.iv_play_active.setVisibility(View.VISIBLE);
            }else{
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                holder.iv_play_active.setVisibility(View.INVISIBLE);
            }*/

            playlistViewHolder.title.setText(playlist.getPlaylistTitle()); // устанавливаем название плейлиста
            playlistViewHolder.artist.setText(playlist.getPlaylistArtist()); // устанавливаем название исполнителя
            Picasso.with(context).load(playlist.getPlaylistArtworkUrl()).placeholder(R.drawable.music_placeholder).into(playlistViewHolder.artwork);
            playlistViewHolder.genre.setText(playlist.getPlaylistGenre());

            playlistViewHolder.bind(playlist, mListenerPl);

        }

    }

    @Override
    public int getItemCount() {
        return mPlaylistArrayList.size();
    }

    public static class PlaylistViewHolder extends RecyclerView.ViewHolder{

        private TextView title, artist, genre;
        private ImageView artwork;

        public PlaylistViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.playlist_title);
            artist = (TextView) itemView.findViewById(R.id.artist_title);
            artwork = (ImageView) itemView.findViewById(R.id.thumbnail);

            genre = (TextView) itemView.findViewById(R.id.genre);
        }

        public void bind(final Playlist playlist, final RecyclerItemClickListener_PL listener){
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClickListener(playlist, getLayoutPosition());
                }
            });
        }

    }

    public interface RecyclerItemClickListener_PL{
        void onClickListener(Playlist playlist, int position);
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }
}
