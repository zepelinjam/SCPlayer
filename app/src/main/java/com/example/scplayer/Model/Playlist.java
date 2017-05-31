package com.example.scplayer.Model;

public class Playlist {

    private long id;
    private String title; // название альбома
    private String artist; // исполнитель (на SC - username)
    private String pl_artworkUrl; // ссылка на обложку
    private String genre; // жанр альбома

    public Playlist (long id, String title, String artist, String artworkUrl, String genre) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.pl_artworkUrl = artworkUrl;
        this.genre = genre;
    }

    public long getPlaylistId() {
        return id;
    }

    public String getPlaylistTitle() {
        return title;
    }

    public String getPlaylistArtist() {
        return artist;
    }

    public String getPlaylistArtworkUrl() {
        return pl_artworkUrl;
    }

    public String getPlaylistGenre() {
        return genre;
    }

}