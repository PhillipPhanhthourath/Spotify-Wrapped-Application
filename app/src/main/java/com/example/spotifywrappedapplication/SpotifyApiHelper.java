package com.example.spotifywrappedapplication;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

class JsonNameFinder {
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

    // All the tracks from all the users playlists
    // Returns a list of json files, each jsonfile is a playlist
    public void getTracksFromAllPlaylists(Callback finalCallback) {
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
                    List<String> allTracks = new ArrayList<>();

                    for (int i = 0; i < playlists.length(); i++) {
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
                                        allTracks.add(trackResponseBody);
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
                            System.out.println(allTracks.size());
                            System.out.println(allTracks.get(1).length());

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

                            String concatenatedTracks = "["+String.join(",", allTracks)+"]"; // Simplified
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

    public void getCurrentlyPlayingTrack(Callback callback) {
        String url = "https://api.spotify.com/v1/me/player/currently-playing";
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + accessToken)
                .build();

        client.newCall(request).enqueue(callback);
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

        // Get currently playing track
        apiHelper.getCurrentlyPlayingTrack(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println("Currently Playing Track: " + response.body().string());
            }
        });
    }
}
