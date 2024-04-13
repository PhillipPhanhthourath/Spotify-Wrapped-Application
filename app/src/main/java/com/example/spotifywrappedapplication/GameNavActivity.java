package com.example.spotifywrappedapplication;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class GameNavActivity extends AppCompatActivity implements PlaceholderAdapter.OnItemClicked {

    private RecyclerView recyclerView;
    private SharedViewModel viewModel;
    private PlaceholderAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_nav);

        // Initialize the ViewModel
        viewModel = new ViewModelProvider(this).get(SharedViewModel.class);


        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PlaceholderAdapter(new ArrayList<FragmentData>(), this);
        recyclerView.setAdapter(adapter);
        Button openAlertButton = findViewById(R.id.openAlertButton);
        openAlertButton.setOnClickListener(view -> {
            FirebaseUser user = FirebaseUtils.getInstance().getFirebaseAuth().getCurrentUser();

            // LAYER 1: FETCH TOKEN
            FirebaseUtils.fetchAccessToken(user, new FirebaseUtils.TokenFetchListener() {
                @Override
                public void onTokenFetched(String token) {
                    Log.d("Firebase", "Access token fetched: " + token);
                    SpotifyApiHelper helper = new SpotifyApiHelper(token); // Proceed with using the token

                    // LAYER 2: FETCH PLAYLISTS
                    helper.getTracksFromAllPlaylists(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            Log.e("Spotify API", "Failed to fetch playlists", e);
                        }
                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            if (response.isSuccessful() && response.body() != null) {
                                String jsonResponse = response.body().string();
                                JsonNameFinder finder = new JsonNameFinder();
                                List<String> allsongs = finder.findNames(jsonResponse);
                                for (String key : allsongs) {
                                    System.out.println(key);
                                }
                                try {
                                    // Parse the string into a JSONArray
                                    JSONArray jsonArray = new JSONArray(jsonResponse);
                                    List<String> names = finder.findNames(jsonArray.getJSONObject(0).toString());
                                    HashMap<String,List<String>> songOptions = new HashMap<>();
                                    for (int i = 1; i <= names.size(); ++i){
                                        String key = names.get(i-1);
                                        List<String> value = finder.findNames(jsonArray.getJSONObject(i).toString());
                                        System.out.println(key);
                                        System.out.println(value);
                                        songOptions.put(key,value);
                                    }
                                    //Map<String,List<String>> songOptions: holds the song options
                                    //int difficulty: [1,3] difficulty of the game
                                    //int rows: fixed to 10
                                    //int cols: fixed to 10
                                    //public WordSearchGenerator(List<String> words, int difficulty, int rows, int cols)
                                    //public FragmentData(String color, WordSearchGenerator wordSearch)
                                    //adapter.updateData(FragmentData newData)
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            // Code to show dialog goes here
                                            SelectPlaylistDialog dialog = new SelectPlaylistDialog(GameNavActivity.this, songOptions, newData -> {
                                                adapter.updateData(newData);
                                            });
                                            dialog.show();
                                        }
                                    });


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Log.e("Spotify API", "Response not successful or body is null");
                            }
                        }
                    });
                }

                @Override
                public void onError(String error) {
                    Log.e("Firebase", error);
                }
            });
        });
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_game);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent;
                if (item.getItemId() == R.id.navigation_user_profile) {
                    // Navigate to UserProfileActivity
                    intent = new Intent(GameNavActivity.this, UserProfileActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                    return true;
                } else if (item.getItemId() == R.id.navigation_game) {
                    // We're already in GameActivity, no need to do anything
                    return true;
                }
                return false;
            }
        });
    }

    // Hides all existing elements of the game activity page
    @Override
    public void onItemClick(FragmentData fragmentData) {
        // Hid the components in the current view
        recyclerView.setVisibility(View.GONE);  // Hide RecyclerView
        findViewById(R.id.openAlertButton).setVisibility(View.GONE);  // Hide Button
        findViewById(R.id.bottom_navigation).setVisibility(View.GONE);
        // Make a fragment with the new page
        // Assume you retrieve or create the object here
        viewModel.setComplexData(fragmentData);
        FullScreenRedFragment redFragment = new FullScreenRedFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, redFragment)
                .addToBackStack(null)  // This line allows the user to return to the previous view by pressing the back button.
                .commit();
    }
}
