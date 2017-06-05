package com.example.scplayer.Request;

import android.util.Log;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.example.scplayer.Config;
import com.example.scplayer.Model.Playlist;
import com.example.scplayer.Model.Song;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class SoundcloudApiRequest {
    // описываем интерфейс для треков
    public interface SoundcloudInterface{
        void onSuccess(ArrayList<Song> songs); // описываем для треков
        void onError(String message); // описываем для треков
    }
    // описываем интерфейс для плейлистов
    public interface SoundcloudInterface_PL{
        void onSucsess_PL(ArrayList<Playlist> playlists, ArrayList<Song> songs); // описываем для плейлистов
        void onError_PL (String message); // описываем для плейлистов
    }
    // Класс RequestQueue (из библ. Volley) используется для отправки сетевых запросов
    private RequestQueue queue; // создаем объект класса RequestQueue
    //private static final String URL = "http://api.soundcloud.com/tracks?filter=public&limit=100&client_id="+ Config.CLIENT_ID;
    public static String URL = "http://api.soundcloud.com/tracks?filter=public&limit=100&client_id="+ Config.CLIENT_ID;
    private static final String TAG = "APP";
    private static final String URL_PL = "http://api.soundcloud.com/playlists?filter=public&limit=100&streamable=true&client_id="+ Config.CLIENT_ID;
    
    public SoundcloudApiRequest(RequestQueue queue) {
        this.queue = queue;
    }

    public void getPlaylist(String query, final SoundcloudInterface_PL callback){

        String url = URL_PL; // переменная для ссылки на Soundcloud

        if(query.length() > 0){ // если запрос не пустой, то
            try {
                query = URLEncoder.encode(query, "UTF-8"); // запрос кодируется в кодировку "UTF-8"
                url = URL + "&q=" + query; // добавляем к ссылке закодированный запрос
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        Log.d(TAG, "запрос getPlaylist: " + url); // отмечаем в логе
        // формируем GET запрос (JSONArrayRequest - запрос массива)
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) { // обработка ответа с сервера
                Log.d(TAG, "onResponse: " + response); // отмечаем в логе

                ArrayList<Playlist> playlists = new ArrayList<>(); // создам массив <Playlist>
                ArrayList<Song> songs = new ArrayList<>();

                if(response.length() > 0){ // если ответ с сервера не пустой, то
                    for (int i = 0; i < response.length() ; i++) { // для каждого элемента в полученном массиве ...
                        try {
                            JSONObject playlistObject = response.getJSONObject(i); // извлекаем JSON-объект из полученного массива
                            long id = playlistObject.getLong("id"); // id альбома
                            String title = playlistObject.getString("title"); // название альбома
                            String artworkUrl_PL = playlistObject.getString("artwork_url"); // ссылка на обложку
                            JSONObject user = playlistObject.getJSONObject("user"); // исполнитель (ползователь в SC)
                            String artist = user.getString("username");
                            String genre = playlistObject.getString("genre"); // жанр

                            Playlist playlist = new Playlist(id, title, artist, artworkUrl_PL, genre);
                            playlists.add(playlist);

                            JSONObject songObject = response.getJSONObject(i);
                            long track_id = songObject.getLong("id");
                            String track_title = songObject.getString("title");
                            String track_artworkUrl = songObject.getString("artwork_url");
                            String track_streamUrl = songObject.getString("stream_url");
                            long track_duration = songObject.getLong("duration");
                            int track_playbackCount = songObject.has("playback_count") ? songObject.getInt("playback_count") : 0;
                            JSONObject track_user = songObject.getJSONObject("user");
                            String track_artist = user.getString("username");

                            Song song = new Song(track_id, track_title, track_artist, track_artworkUrl, track_duration, track_streamUrl, track_playbackCount);
                            songs.add(song);

                        } catch (JSONException e) {
                            Log.d(TAG, "onResponse: " + e.getMessage());
                            callback.onError_PL("Произошла ошибка");
                            e.printStackTrace();
                        }
                    }

                    callback.onSucsess_PL(playlists, songs);

                }else{
                    callback.onError_PL("Не найдено плейлистов");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onResponse: " + error.getMessage());
                callback.onError_PL("Ошибка ответа с сервера");
            }
        });

        queue.add(request);

    }

    public void getSongList(String query, final SoundcloudInterface callback){

        String url = URL;
        if(query.length() > 0){
            try {
                query = URLEncoder.encode(query, "UTF-8");
                url = URL + "&q=" + query;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        Log.d(TAG, "getSongList: " + url);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Log.d(TAG, "onResponse: " + response);

                ArrayList<Song> songs = new ArrayList<>();
                if(response.length() > 0){
                    for (int i = 0; i < response.length() ; i++) {
                        try {
                            JSONObject songObject = response.getJSONObject(i);
                            long id = songObject.getLong("id");
                            String title = songObject.getString("title");
                            String artworkUrl = songObject.getString("artwork_url");
                            String streamUrl = songObject.getString("stream_url");
                            long duration = songObject.getLong("duration");
                            int playbackCount = songObject.has("playback_count") ? songObject.getInt("playback_count") : 0;
                            JSONObject user = songObject.getJSONObject("user");
                            String artist = user.getString("username");

                            Song song = new Song(id, title, artist, artworkUrl, duration, streamUrl, playbackCount);
                            songs.add(song);

                        } catch (JSONException e) {
                            Log.d(TAG, "onResponse: " + e.getMessage());
                            callback.onError("Произошла ошибка");
                            e.printStackTrace();
                        }
                    }

                    callback.onSuccess(songs);

                }else{
                    callback.onError("Не найдено ни одной композиции");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onResponse: " + error.getMessage());
                callback.onError("Произошла ошибка");
            }
        });

        queue.add(request);

    }
}
