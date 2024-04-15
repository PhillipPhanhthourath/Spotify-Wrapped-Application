package com.example.spotifywrappedapplication;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

class PlaylistSongs {
    private static PlaylistSongs topItems;
    private List<Song> songs;

    public PlaylistSongs(JSONObject tracks) {
        // Initialize the songs list
        this.songs = new ArrayList<>();

        try {
            // Parse the JSON object to extract the array of items
            JSONArray itemsArray = tracks.getJSONArray("items");

            // Iterate through each item in the array
            for (int i = 0; i < itemsArray.length(); i++) {
                JSONObject item = itemsArray.getJSONObject(i);
                JSONObject track = item.getJSONObject("track");
                // Create a new Song object with the track JSON object
                Song song = this.createSong(track);
                System.out.println(song);
                this.songs.add(song);
            }
        } catch (JSONException e) {
            // Handle possible JSON exceptions
            System.err.println("Failed to parse JSON: " + e.getMessage());
        }
    }

    public static PlaylistSongs getTopItems() {
        return topItems;
    }

    public static void setTopItems(JSONObject topItemsInp) {
        // Initialize the songs list
        PlaylistSongs.topItems = new PlaylistSongs(topItemsInp);

    }

    public List<Song> getSongs() {
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }

    public List<Song> top5SongsFreq() {
        // Map to count frequency of each song
        Map<Song, Integer> songFrequency = new HashMap<>();
        for (Song song : songs) {
            songFrequency.put(song, songFrequency.getOrDefault(song, 0) + 1);
        }

        // Stream and sort songs by frequency in descending order and limit to 5
        return songs.stream()
                .distinct() // Ensure we're operating on unique songs
                .sorted((song1, song2) -> Integer.compare(songFrequency.get(song2), songFrequency.get(song1)))
                .limit(5)
                .collect(Collectors.toList());
    }
    public List<PlaylistSongs.Artist> top5Artists() {
        return songs.stream()
                .map(Song::getArtist)
                .distinct()
                .sorted((a1, a2) -> Integer.compare(a2.getSongCount(), a1.getSongCount()))
                .limit(5)
                .collect(Collectors.toList());
    }
    public Song createSong (JSONObject track) {
        try {
            String name = track.getString("name");
            int popularity = track.getInt("popularity");
            String urlToImage = track.getJSONObject("album").getJSONArray("images").getJSONObject(0).getString("url");

            // Assuming only one artist per song for simplicity
            JSONObject artistJson = track.getJSONArray("artists").getJSONObject(0);
            Artist artist = this.createArtist(artistJson);
            return new Song(name, artist, popularity, urlToImage);
        } catch (JSONException e) {
            System.err.println("Failed to parse song JSON: " + e.getMessage());
        }
        System.out.println("FAILED BIG");
        return null;
    }

    class Song {
        private String name;
        private Artist artist;
        private int popularity;
        private String dateAdded;
        private String urlToImage;

        @Override
        public String toString() {
            return "Song{" +
                    "name='" + name + '\'' +
                    ", artist=" + artist +
                    ", popularity=" + popularity +
                    ", dateAdded='" + dateAdded + '\'' +
                    ", urlToImage='" + urlToImage + '\'' +
                    '}';
        }

        private Song (String name, Artist artist, int popularity, String urlToImage) {
            this.name=name;
            this.artist=artist;
            this.popularity=popularity;
            this.urlToImage=urlToImage;
        }

        // Getters and Setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Artist getArtist() {
            return artist;
        }

        public void setArtist(Artist artist) {
            this.artist = artist;
        }

        public int getPopularity() {
            return popularity;
        }

        public void setPopularity(int popularity) {
            this.popularity = popularity;
        }

        public String getDateAdded() {
            return dateAdded;
        }

        public void setDateAdded(String dateAdded) {
            this.dateAdded = dateAdded;
        }

        public String getUrlToImage() {
            return urlToImage;
        }

        public void setUrlToImage(String urlToImage) {
            this.urlToImage = urlToImage;
        }
    }
    // Factory method to manage artist instances
    private Map<String, Artist> artistRegistry = new HashMap<>();
    public Artist createArtist(JSONObject artistJson) throws JSONException {
        String id = artistJson.getString("id");
        String name = artistJson.getString("name");
        if (artistRegistry.containsKey(id)) {
            Artist existingArtist = artistRegistry.get(id);
            existingArtist.incrementSongCount();
            return existingArtist;
        } else {
            Artist newArtist = new PlaylistSongs.Artist(name, id);
            artistRegistry.put(id, newArtist);
            return newArtist;
        }
    }
    class Artist {
        private String name;
        private String id;
        private int songCount; // Instance variable for song count

        // Private constructor to prevent direct instantiation
        private Artist(String name, String id) {
            this.name = name;
            this.id = id;
            this.songCount = 1; // Initialize with 1 song
        }

        private void incrementSongCount() {
            this.songCount++;
        }

        public int getSongCount() {
            return songCount;
        }

        public void setSongCount(int songCount) {
            this.songCount = songCount;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return "Artist{" +
                    "name='" + name + '\'' +
                    ", id='" + id + '\'' +
                    ", songCount=" + songCount +
                    '}';
        }
    }
}
class JsonNameFinder {

    public static void printFormattedJson(String json) {
        try {
            if (json.trim().startsWith("{")) {
                JSONObject jsonObject = new JSONObject(json);
                String formatted = jsonObject.toString(4); // Indentation of 4 spaces
                Log.d("Formatted JSON", formatted);
            } else if (json.trim().startsWith("[")) {
                JSONArray jsonArray = new JSONArray(json);
                String formatted = jsonArray.toString(4); // Indentation of 4 spaces
                Log.d("Formatted JSON", formatted);
            }
        } catch (JSONException e) {
            Log.e("JSON Formatter", "Invalid JSON format", e);
        }
    }

    public static Map<String, List<Map<String, String>>> parsePlaylists(String jsonString) {
        Map<String, List<Map<String, String>>> playlistsMap = new HashMap<>();

        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray playlists = jsonObject.getJSONArray("playlists");

            for (int i = 0; i < playlists.length(); i++) {
                JSONObject playlist = playlists.getJSONObject(i);
                String playlistName = playlist.getString("name");
                JSONArray tracks = playlist.getJSONArray("tracks");

                List<Map<String, String>> tracksList = new ArrayList<>();

                for (int j = 0; j < tracks.length(); j++) {
                    JSONObject track = tracks.getJSONObject(j);
                    Map<String, String> artistSongPair = new HashMap<>();
                    artistSongPair.put(track.getString("artist"), track.getString("song"));
                    tracksList.add(artistSongPair);
                }

                playlistsMap.put(playlistName, tracksList);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return playlistsMap;
    }
    /*
    Useful for finding names of songs / artists in spotify wrapped json text
     */
    public List<String> findNames(String jsonText) {
        List<String> names = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(jsonText); // Starting with an object
            findNamesRecursively(jsonObject, names);
        } catch (Exception e) {
            // Attempt to parse as an array if it's not an object
            try {
                JSONArray jsonArray = new JSONArray(jsonText);
                findNamesRecursively(jsonArray, names);
            } catch (Exception ex) {
                System.out.println("Error parsing JSON: " + ex.getMessage());
            }
        }
        return names;
    }

    private void findNamesRecursively(Object json, List<String> names) throws JSONException {
        if (json instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) json;
            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                if ("name".equals(key)) {
                    names.add(jsonObject.getString(key));
                } else {
                    Object child = jsonObject.get(key);
                    findNamesRecursively(child, names);
                }
            }
        } else if (json instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) json;
            for (int i = 0; i < jsonArray.length(); i++) {
                Object item = jsonArray.get(i);
                findNamesRecursively(item, names);
            }
        }
    }

    public static void main(String[] args) {
        JsonNameFinder finder = new JsonNameFinder();
        String jsonText = "{\"name\":\"John\", \"age\":30, \"children\":[{\"name\":\"Jane\"}]}";
        List<String> names = finder.findNames(jsonText);
        System.out.println("Found names: " + names);
    }
}

public class SpotifyApiHelper {

    private final OkHttpClient client = new OkHttpClient();
    private final String accessToken;

    public SpotifyApiHelper(String accessToken) {
        this.accessToken = accessToken;
    }

    public void getUserProfile(Callback callback) {
        String url = "https://api.spotify.com/v1/me";
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + accessToken)
                .build();

        client.newCall(request).enqueue(callback);
    }

    public void getUserPlaylists(Callback callback) {
        String url = "https://api.spotify.com/v1/me/playlists";
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + accessToken)
                .build();

        client.newCall(request).enqueue(callback);
    }
    public interface StringFunction {
        void execute(String data) throws JSONException;
    }

    public void getUserTopTracks(StringFunction responseExe, String timeRange) {
        Callback responseCallback = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Handle the failure case
                System.out.println("getUserTopTracks failed");
                e.printStackTrace();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                } else {
                    // Here you can handle the JSON response
                    String responseData = response.body().string();
                    try {
                        responseExe.execute(responseData);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    // Further processing of the data, e.g., parsing JSON and extracting data
                }
            }
        };
        // Construct the URL with a time range query parameter
        String url = "https://api.spotify.com/v1/me/top/tracks?time_range=" + timeRange;
        // Build the request with the necessary authorization header and the URL
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + accessToken)
                .build();
        
        
        // Enqueue the request with the callback to handle the response
        client.newCall(request).enqueue(responseCallback);
    }

    public void getArtistFromID(StringFunction responseExe, String artistId) {
        // Construct the URL using the artist ID
        String url = "https://api.spotify.com/v1/artists/" + artistId;

        // Build the request with the necessary authorization header and the URL
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + accessToken)
                .build();

        // Enqueue the request with the callback to handle the response
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Handle the failure case
                System.out.println("getArtist failed");
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                } else {
                    // Here you can handle the JSON response
                    String responseData = response.body().string();
                    try {
                        responseExe.execute(responseData);
                    } catch (JSONException e) {
                        throw new RuntimeException("Failed to parse JSON", e);
                    }
                    // Further processing of the data, e.g., parsing JSON and extracting data
                }
            }
        });
    }

    // All the tracks from all the users playlists
    // Returns a list of json files, each jsonfile is a playlist
    public void getTracksFromAllPlaylists(StringFunction runExe) {
        Callback finalCallback=new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("Spotify API", "Failed to fetch playlists", e);
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonResponse = response.body().string();
                    try {
                        runExe.execute(jsonResponse);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                } else {
                    Log.e("Spotify API", "Response not successful or body is null");
                }
            }
        };
        getUserPlaylists(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                finalCallback.onFailure(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    finalCallback.onFailure(call, new IOException("Failed to get playlists"));
                    return;
                }

                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    JSONArray playlists = jsonObject.getJSONArray("items");
                    int playlistCount = playlists.length();
                    CountDownLatch countDownLatch = new CountDownLatch(playlistCount);
                    final List<String> allTracks = new ArrayList<>(Collections.nCopies(playlists.length(), null));

                    for (int i = 0; i < playlists.length(); i++) {
                        final int index = i;
                        JSONObject playlist = playlists.getJSONObject(i);
                        String playlistId = playlist.getString("id");

                        String tracksUrl = "https://api.spotify.com/v1/playlists/" + playlistId + "/tracks";
                        Request tracksRequest = new Request.Builder()
                                .url(tracksUrl)
                                .header("Authorization", "Bearer " + accessToken)
                                .build();

                        client.newCall(tracksRequest).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                // Decrement the count but handle failure as needed
                                countDownLatch.countDown();
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                try {
                                    if (response.isSuccessful()) {
                                        String trackResponseBody = response.body().string();
                                        // Here you could parse and add to allTracks list
                                        // For demonstration, just adding raw JSON string
                                        allTracks.set(index,trackResponseBody);
                                    }
                                } finally {
                                    countDownLatch.countDown();
                                }
                            }
                        });
                    }

                    // Wait for all requests to finish
                    new Thread(() -> {
                        try {
                            countDownLatch.await();
                            // Here concatenate all tracks and use finalCallback to return them
                            System.out.println("middle");
                            JsonNameFinder jnf = new JsonNameFinder();

                            List<String> names = jnf.findNames(allTracks.get(0)); // This gets the names
                            Set<String> uniqueNames = new HashSet<>(names); // Remove duplicates
                            String result = String.join(", ", uniqueNames); // Join all unique names in one line, separated by commas
                            System.out.println(result);

                            /*
                            names = jnf.findNames(allTracks.get(1)); // This gets the names
                            uniqueNames = new HashSet<>(names); // Remove duplicates
                            result = String.join(", ", uniqueNames); // Join all unique names in one line, separated by commas
                            System.out.println(result);

                            names = jnf.findNames(allTracks.get(2)); // This gets the names
                            uniqueNames = new HashSet<>(names); // Remove duplicates
                            result = String.join(", ", uniqueNames); // Join all unique names in one line, separated by commas
                            System.out.println(result);
                             */

                            String concatenatedTracks = "["+responseBody+","+String.join(",", allTracks)+"]"; // Simplified
                            Response concatenatedResponse = new Response.Builder()
                                    .request(call.request())
                                    .protocol(Protocol.HTTP_1_1)
                                    .code(200) // This should reflect the actual response status
                                    .message("OK")
                                    .body(ResponseBody.create(concatenatedTracks, MediaType.parse("application/json")))
                                    .build();
                            finalCallback.onResponse(call, concatenatedResponse);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).start();

                } catch (JSONException e) {
                    finalCallback.onFailure(call, new IOException("Failed to parse playlists JSON"));
                }
            }
        });
    }

    // Example usage
    public static void main(String[] args) {
        String accessToken = "your_access_token_here";
        SpotifyApiHelper apiHelper = new SpotifyApiHelper(accessToken);

        // Get user profile
        apiHelper.getUserProfile(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println("User Profile: " + response.body().string());
            }
        });

        // Get user playlists
        apiHelper.getUserPlaylists(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println("User Playlists: " + response.body().string());
            }
        });

    }
    @FunctionalInterface
    public interface PlayListUtilFunction {
        void execute(Map<String,PlaylistSongs> songs);
    }

    public void playlistUtil(PlayListUtilFunction fn){
        SpotifyApiHelper spotifyHelper = new SpotifyApiHelper(accessToken);
        System.out.println("inside playlistUtil");
// Fetch the user's top tracks over the medium term
        spotifyHelper.getUserTopTracks(topsongsstr -> {
            PlaylistSongs.setTopItems(new JSONObject(topsongsstr));
            spotifyHelper.getTracksFromAllPlaylists(allplaylistsstr -> {
                //now, we have the top songs, and info from all playlists

                try {
                    //populate songOptions with relevant data
                    HashMap<String,PlaylistSongs> songOptions = new HashMap<>();
                    JSONArray jsonArray = new JSONArray(allplaylistsstr);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    JSONArray playlists = jsonObject.getJSONArray("items");
                    for (int i = 0; i < playlists.length(); ++i){
                        JSONObject playlist = playlists.getJSONObject(i);
                        String playlistname = playlist.getString("name");
                        songOptions.put(playlistname,new PlaylistSongs(jsonArray.getJSONObject(i+1)));
                        System.out.println("Playlist Name");
                        System.out.println(playlistname);
                        System.out.println("Song Options");
                        System.out.println(jsonArray.getJSONObject(i+1));
                    }
                    fn.execute(songOptions);

                } catch (JSONException e) {
                    e.printStackTrace();
                }


                //JsonNameFinder.printFormattedJson(topsongsstr);
            });}, "long_term");
    }
}
