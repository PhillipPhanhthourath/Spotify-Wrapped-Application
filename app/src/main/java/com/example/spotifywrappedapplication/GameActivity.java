package com.example.spotifywrappedapplication;
import android.os.Handler;
import android.os.Looper;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.view.ViewGroup;
import android.util.DisplayMetrics;
import android.widget.TextView;
import android.widget.Toast;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import java.io.FileWriter;
import java.io.IOException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GameActivity extends AppCompatActivity {

    final int rowCount = 10;
    final int colCount = 10;
    final int difficulty = 3;

    GridLayout wordSearchGrid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        wordSearchGrid = findViewById(R.id.word_search_grid);


        // Initialize game logic
        String mAccessToken = getIntent().getStringExtra("ACCESS_TOKEN");
        System.out.println("access token");
        System.out.println(mAccessToken);
        SpotifyApiHelper apiHelper = new SpotifyApiHelper(mAccessToken);

        apiHelper.getTracksFromAllPlaylists(new PlaylistCallbackHandler());

        bottomNavigationView.setSelectedItemId(R.id.navigation_game);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent;
                if (item.getItemId() == R.id.navigation_user_profile) {
                    // Navigate to UserProfileActivity
                    intent = new Intent(GameActivity.this, UserProfileActivity.class);
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

    private class PlaylistCallbackHandler implements Callback {
        private List<String> artistList = new ArrayList<>();

        @Override
        public void onFailure(Call call, IOException e) {
            runOnUiThread(() -> Toast.makeText(GameActivity.this, "Failed to Retrieve Artist List from API!", Toast.LENGTH_SHORT).show());
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            if (response.isSuccessful() && response.body() != null) {
                String jsonResponse = response.body().string();
                //System.out.println(jsonResponse);
                //logJsonForDebugging(jsonResponse);
                JsonNameFinder finder = new JsonNameFinder();
                List<String> names = finder.findNames(jsonResponse);
                runOnUiThread(() -> { buildWordSearch(names); });
            } else {
                runOnUiThread(() -> Toast.makeText(GameActivity.this, "Failed to Retrieve Artist List because of invalid response!", Toast.LENGTH_SHORT).show());
            }
        }
    }
    public void logJsonForDebugging(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            String formattedJson = jsonObject.toString(4); // Indentation of 4 spaces for better readability

            Log.d("JSONOutput", formattedJson); // Log the formatted JSON with a tag for easy filtering
        } catch (Exception e) {
            Log.e("JSONError", "Error parsing JSON", e);
        }
    }

    public void buildWordSearch(List<String> wordList){
        WordSearchGenerator wordSearch = new WordSearchGenerator(wordList, difficulty, rowCount, colCount);
        wordSearch.printGrid();
        wordSearch.printWordsWithPositions();
        Set<String> visited = new HashSet<>();

        // Get screen width
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int cellSize = screenWidth / colCount; // Calculate cell size to fit screen width

        wordSearchGrid.setColumnCount(colCount);
        wordSearchGrid.setRowCount(rowCount);

        // Populate the grid with buttons
        for (int row = 0; row < rowCount; row++) {
            for (int col = 0; col < colCount; col++) {
                Button button = new Button(this);
                button.setLayoutParams(new ViewGroup.LayoutParams(cellSize, cellSize)); // Square cells
                button.setText(wordSearch.getDisplayText(row,col)); // Example content
                final int finalRow = row;
                final int finalCol = col;
                int finalRow1 = row;
                int finalCol1 = col;
                button.setOnClickListener(view -> {Map<String, Integer> group = wordSearch.tap(finalRow1, finalCol1);
                    Button gridButton = (Button) view;

                    switch (group.get("state")) {
                        case 0:
                            gridButton.setBackgroundColor(Color.WHITE); // Default background color
                            gridButton.setTextColor(Color.BLACK); // Default text color
                            break;
                        case 1:
                            gridButton.setBackgroundColor(Color.BLACK); // Selected background color
                            gridButton.setTextColor(Color.WHITE); // Selected text color
                            break;
                        case 2:
                            gridButton.setBackgroundColor(Color.BLUE); // Default background color
                            gridButton.setTextColor(Color.WHITE); // Default text color
                            break;
                        case 3:
                            gridButton.setBackgroundColor(Color.BLACK); // Selected background color
                            gridButton.setTextColor(Color.WHITE); // Selected text color
                            break;

                    }
                    if (group.get("word")!=-1){ // Word found - set accordingly
                        for (WordSearchGenerator.GridPosition gp: wordSearch.inlets()){
                            // Assuming you can reference buttons directly by their grid position
                            Button wordButton = (Button) wordSearchGrid.getChildAt(gp.row * colCount + gp.col);
                            wordButton.setBackgroundColor(Color.BLUE); // Found word background color
                            wordButton.setTextColor(Color.WHITE); // Found word text color
                        }
                    }
                    System.out.println("Tile at (" + finalRow + "," + finalCol + ") touched");
                });
                switch (wordSearch.getState(row,col)) {
                    case 0:
                        button.setBackgroundColor(Color.WHITE); // Default background color
                        button.setTextColor(Color.BLACK); // Default text color
                        break;
                    case 1:
                        button.setBackgroundColor(Color.BLACK); // Selected background color
                        button.setTextColor(Color.WHITE); // Selected text color
                        break;
                    case 2:
                        button.setBackgroundColor(Color.BLUE); // Default background color
                        button.setTextColor(Color.WHITE); // Default text color
                        break;
                    case 3:
                        button.setBackgroundColor(Color.BLACK); // Selected background color
                        button.setTextColor(Color.WHITE); // Selected text color
                        break;
                }

                wordSearchGrid.addView(button);
            }
        }
        TextView bottomtext = findViewById(R.id.game_strat_view);
        bottomtext.setText(String.join("\n", wordSearch.getWords()));

    }


}
