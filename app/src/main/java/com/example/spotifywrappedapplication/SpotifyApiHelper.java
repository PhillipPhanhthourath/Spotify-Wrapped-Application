package com.example.spotifywrappedapplication;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;

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
