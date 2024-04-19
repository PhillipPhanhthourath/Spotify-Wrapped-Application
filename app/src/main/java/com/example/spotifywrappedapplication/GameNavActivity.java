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
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

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
    private ArrayList<FragmentData> fdata;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_nav);

        // Initialize the ViewModel
        viewModel = new ViewModelProvider(this).get(SharedViewModel.class);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        if (fdata==null){ //retrieve saved data
            fdata = new ArrayList<FragmentData>();
        }
        adapter = new PlaceholderAdapter(fdata, this);
        recyclerView.setAdapter(adapter);
        Button openAlertButton = findViewById(R.id.openAlertButton);
        openAlertButton.setOnClickListener(view -> {
            FirebaseUser user = FirebaseUtils.getInstance().getFirebaseAuth().getCurrentUser();

            // LAYER 1: FETCH TOKEN
            FirebaseUtils.fetchAccessToken(user, new FirebaseUtils.TokenFetchListener() {
                @Override
                public void onTokenFetched(String token) {
                    Log.d("Firebase", "Access token fetched today: " + token);
                    SpotifyApiHelper helper = new SpotifyApiHelper(token); // Proceed with using the token
                    // testUtils(helper);

                    // LAYER 2: FETCH PLAYLISTS
                    helper.getTracksFromAllPlaylists(jsonResponse -> {
                        JsonNameFinder finder = new JsonNameFinder();
                        try {
                            // Parse the string into a JSONArray
                            JSONArray jsonArray = new JSONArray(jsonResponse);
                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                            JSONArray playlists = jsonObject.getJSONArray("items");
                            List<String> playlistNames = finder.findNames(jsonObject.toString());
                            HashMap<String,List<String>> songOptions = new HashMap<>();
                            HashMap<String,String> urls = new HashMap<>();
                            for (int i = 0; i < playlists.length(); ++i){
                                JSONObject playlist = playlists.getJSONObject(i);
                                String playlistname = playlist.getString("name");
                                List<String> value = finder.findNames(jsonArray.getJSONObject(i+1).toString());
                                songOptions.put(playlistname,value);
                                if (!playlist.get("images").toString().equals("null")) {
                                    JSONArray images = playlist.getJSONArray("images");
                                    urls.put(playlistname,images.getJSONObject(0).getString("url"));
                                }
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
                                    SelectPlaylistDialog dialog = new SelectPlaylistDialog(GameNavActivity.this, songOptions, urls, newData -> {
                                        adapter.updateData(newData);
                                    });
                                    System.out.println("showing dialog");
                                    dialog.show();
                                }
                            });


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }});
                }

                @Override
                public void onError(String error) {
                    Log.e("Firebase", error);
                }
            });
        });
        MaterialButton backButton = findViewById(R.id.back_to_stats_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Implement your navigation logic here
                // This might involve finishing the current activity or navigating with a NavController
                finish();  // For example, if this button is meant to close the current activity
            }
        });
    }


    // Hides all existing elements of the game activity page
    @Override
    public void onItemClick(FragmentData fragmentData) {
        // Hid the components in the current view
        recyclerView.setVisibility(View.GONE);  // Hide RecyclerView
        findViewById(R.id.openAlertButton).setVisibility(View.GONE);  // Hide Button
        findViewById(R.id.back_to_stats_button).setVisibility(View.GONE);
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
