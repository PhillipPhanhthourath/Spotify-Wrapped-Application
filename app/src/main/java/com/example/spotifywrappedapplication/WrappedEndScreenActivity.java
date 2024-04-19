package com.example.spotifywrappedapplication;

import java.io.File;
import java.io.OutputStream;
import java.time.LocalDateTime;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

public class WrappedEndScreenActivity extends AppCompatActivity {
    private ConstraintLayout frontCard;
    private TextView summaryName;
    private TextView summaryDate;
    private ImageView[] artistImages;
    private TextView[] artistNames;
    private ImageView[] songImages;
    private TextView[] songNames;
    private TextView[] genres;
    private ImageView[] covers;
    private View backCard;
    private View downloadButton;
    private String mAccessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_of_wrapped);
        mAccessToken = this.getIntent().getStringExtra("ACCESS_TOKEN");

        // view on arrival to page
        summaryName = findViewById(R.id.summary_name);
        summaryName.setText("Your Wrapped");
        summaryDate = findViewById(R.id.summary_date);
        summaryDate.setText(LocalDateTime.now().toString().substring(0, 10));
        covers = new ImageView[]{findViewById(R.id.cover1), findViewById(R.id.cover2), findViewById(R.id.cover3), findViewById(R.id.cover4),
                findViewById(R.id.cover5), findViewById(R.id.cover6), findViewById(R.id.cover7), findViewById(R.id.cover8),
                findViewById(R.id.cover9), findViewById(R.id.cover10), findViewById(R.id.cover11), findViewById(R.id.cover12),
                findViewById(R.id.cover13), findViewById(R.id.cover14), findViewById(R.id.cover15), findViewById(R.id.cover16)};
        populateGrid();
        artistImages = new ImageView[]{findViewById(R.id.image1a), findViewById(R.id.image2a), findViewById(R.id.image3a)};
        songImages =  new ImageView[]{findViewById(R.id.image1b), findViewById(R.id.image2b), findViewById(R.id.image3b)};
        artistNames = new TextView[]{findViewById(R.id.text1a), findViewById(R.id.text2a), findViewById(R.id.text3a)};
        songNames = new TextView[]{findViewById(R.id.text1b), findViewById(R.id.text2b), findViewById(R.id.text3b)};
        populateTop5s();
        genres = new TextView[]{findViewById(R.id.bar1), findViewById(R.id.bar2), findViewById(R.id.bar3), findViewById(R.id.bar4), findViewById(R.id.bar5)};
        populateBars();
        TextView title = findViewById(R.id.title_text);
        Button buttonBack = findViewById(R.id.back_button);
        Button buttonContinue = findViewById(R.id.continue_button);
        frontCard = findViewById(R.id.summary_image);
        backCard = findViewById(R.id.download_background);
        downloadButton = findViewById(R.id.download_button);
        TextView gamePrompt = findViewById(R.id.game_prompt);

        title.startAnimation(WrappedHelper.animation(this, "fade in"));
        frontCard.startAnimation(WrappedHelper.animation(this, "fade in"));

        gamePrompt.setVisibility(View.VISIBLE);
        Animation flyInBottom = WrappedHelper.animation(this, "fly in bottom");
        gamePrompt.startAnimation(flyInBottom);
        Animation floatVertical = WrappedHelper.animation(this, "float");
        gamePrompt.startAnimation(floatVertical);
        Animation fadeInWithOffset = WrappedHelper.animation(this, "fade in");
        fadeInWithOffset.setStartOffset(3000);
        buttonContinue.startAnimation(fadeInWithOffset);
        buttonBack.startAnimation(fadeInWithOffset);

        // Set the click listeners for the buttons

        buttonBack.setOnClickListener((v) -> {
            returnToMain();
        });

        buttonContinue.setOnClickListener((v) -> {
            continueToGame();
        });

        frontCard.setOnClickListener((v) -> {
            WrappedHelper.flipCard(this, frontCard, backCard);
            downloadButton.setVisibility(View.VISIBLE);
            downloadButton.startAnimation(WrappedHelper.animation(this, "fade in"));
        });

        backCard.setOnClickListener((v) -> {
            WrappedHelper.flipCard(this, frontCard, backCard);
            downloadButton.startAnimation(WrappedHelper.animation(this, "fade out"));
            downloadButton.setVisibility(View.INVISIBLE);
        });

        downloadButton.setOnClickListener((v) -> {
            //downloadImage(frontCard);
            ImageExporter.exportConstraintLayoutAsImage(this, frontCard);
        });
    }

    /**
     * Download the final wrapped summary onto the device
     */
    public void downloadImage(ConstraintLayout image) {
        Bitmap bitmap = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        image.draw(canvas);
        String filename = "summary_image.jpeg";
        FileOutputStream outputStream;
        try {
            outputStream = this.openFileOutput(filename, Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.close();
            Toast.makeText(this, "Successfully downloaded!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to download", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Return to the main page
     */
    public void returnToMain() {
        Intent intent = new Intent(WrappedEndScreenActivity.this, MainActivity.class);
        intent.putExtra("ACCESS_TOKEN", getIntent().getStringExtra("ACCESS_TOKEN"));
        startActivity(intent);
    }

    /**
     * Continue onwards in the Wrapped Summary
     */
    public void continueToGame() {
        Intent intent = new Intent(WrappedEndScreenActivity.this, GameNavActivity.class);
        intent.putExtra("ACCESS_TOKEN", getIntent().getStringExtra("ACCESS_TOKEN"));
        startActivity(intent);
    }

    public void populateBars() {
        SpotifyApiHelper helper = new SpotifyApiHelper(this.getIntent().getStringExtra("ACCESS_TOKEN"));
        helper.getUserSavedTracks((responseStr) -> {
            System.out.println("WrappedPartTwoActivity.java - made it into getUserSavedTracks");
            JSONObject response = new JSONObject(responseStr);
            JSONArray tracks = response.getJSONArray("items");
            Map<String, Integer> genreFreqs = new HashMap<>();
            // List<String> genresList = new ArrayList<>();
            StringBuilder genreStr = new StringBuilder();
            for (int i = 0; i < tracks.length(); i++) {
                JSONObject track = tracks.getJSONObject(i).getJSONObject("track");
                JSONArray artists = track.getJSONArray("artists");
                helper.getArtistFromID((artistResponseStr) -> {
                    System.out.println("made it into getArtistFromID");
                    JSONObject artist = new JSONObject(artistResponseStr);
                    JSONArray genres = artist.getJSONArray("genres");
                    for (int j = 0; j < genres.length(); j++) {
                        System.out.print(genres.getString(j));
                        String genre = genres.getString(j);
                        genreStr.append(genre).append(" ");
                        int freq = genreFreqs.getOrDefault(genre, 0);
                        genreFreqs.put(genre, freq + 1);
                    }
                }, artists.getJSONObject(0).getString("id"));
            }
            /*for (String genre: genreFreqs.keySet()) {
                System.out.print(genre + " ");
            }*/
            List<String> genresSorted = StringIntPair.retrieveKeysSorted(genreFreqs);
            // System.out.println("genresSorted " + genresSorted.get(0));
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    System.out.println("genreStr: " + genreStr.toString());
                    Random rand = new Random();
                    Integer[] colors = new Integer[]{
                            Color.parseColor("#FF1493"), // Neon Pink
                            Color.parseColor("#00BFFF"), // Neon Blue
                            Color.parseColor("#FF00FF"), // Neon Magenta
                            Color.parseColor("#FFA500"), // Neon Orange
                            Color.parseColor("#FF6347"), // Neon Tomato
                            Color.parseColor("#9932CC"), // Neon Purple
                            Color.parseColor("#FF4500"), // Neon Orange Red
                            Color.parseColor("#FFD700"), // Neon Gold
                            Color.parseColor("#7FFF00"), // Neon Chartreuse
                            Color.parseColor("#FFA07A"), // Light Salmon
                            Color.parseColor("#FF8C00"),  // Dark Orange
                            Color.parseColor("#9370DB"), // Medium Purple
                    };
                    for (int i = 0; i < genresSorted.size() && i < 5; i++) {
                        genres[i].setText(genresSorted.get(i));
                        genres[i].setBackgroundColor(colors[rand.nextInt(colors.length)]);
                        ViewGroup.LayoutParams layoutParams = genres[i].getLayoutParams();
                        layoutParams.height = rand.nextInt(800) + 200; // Set height dynamically
                        genres[i].setLayoutParams(layoutParams);
                    }
                }
            });
        });
    }


    protected void populateTop5s() {
        SpotifyApiHelper helper = new SpotifyApiHelper(mAccessToken);
        helper.getUserTopArtists((responseStr) -> {
            System.out.println("EndOfWrapped.java- made it into getUserTopArtists");
            JSONObject response = new JSONObject(responseStr);
            JSONArray artists = response.getJSONArray("items");
            for (int i = 0; i < 3; i++) {
                JSONObject artist = artists.getJSONObject(i);
                int finalI = i;
                helper.getArtistFromID((a) -> {
                    System.out.println("EndOfWrapped.java- made it into getArtistFromID");
                    JSONObject fullArtist = new JSONObject(a);
                    JSONArray images = fullArtist.getJSONArray("images");
                    JSONObject icon = images.getJSONObject(0);
                    String url = icon.getString("url");
                    String name = fullArtist.getString("name");

                    // populate card
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println("EndOfWrapped.java - runOnUiThread " + url);
                            Picasso.get().load(url).resize(30, 0).into(artistImages[finalI]);
                            artistNames[finalI].setText(name);
                            if (name.length() > 12) {
                                artistNames[finalI].setTextSize(8);
                            }
                        }
                    });
                }, artist.getString("id"));
            }
        }, "short_term");


        helper.getUserTopTracks((responseStr) -> {
            System.out.println("EndOfWrapped - Got user top tracks.");
            JSONObject response = new JSONObject(responseStr);
            JSONArray tracks = response.getJSONArray("items");
            for (int i = 0; i < 3; i++) {
                JSONObject track = tracks.getJSONObject(i);
                JSONObject album = track.getJSONObject("album");
                JSONObject image = album.getJSONArray("images").getJSONObject(0);
                String url = image.getString("url");
                String name = track.getString("name");
                int finalI = i;
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        System.out.println("EndOfWrapped.java - runOnUiThread " + url);
                        Picasso.get().load(url).resize(10, 0).into(songImages[finalI]);
                        songNames[finalI].setText(name);
                        if (name.length() > 12) {
                            songNames[finalI].setTextSize(8);
                        }
                    }
                });
            }
        }, "short_term");
    }

    public void populateGrid() {
        SpotifyApiHelper apiHelper = new SpotifyApiHelper(mAccessToken);
        apiHelper.playlistUtil((playlists) -> {
            String[] imageUrls = new String[covers.length];
            // getting 16 (or fewer) random playlists
            System.out.println("EndOfWrapped - made it into playlistUtil call");
            List<String> temp = new ArrayList<>(playlists.keySet());
            List<String> playlistNames = new ArrayList<>();
            // if someone has a lot of playlists
            if (temp.size() > covers.length * 10) {
                Random rand = new Random();
                Set<Integer> visited = new HashSet<>();
                while (visited.size() < covers.length) {
                    int index = rand.nextInt(playlistNames.size());
                    playlistNames.add(temp.get(index));
                    visited.add(index);
                }
            } else if (temp.size() > covers.length) {
                Random rand = new Random();
                int index = rand.nextInt(temp.size() - covers.length);
                playlistNames = temp.subList(index, index + covers.length);
            } else {
                playlistNames = temp;
            }

            // TODO: this part is super inefficient because of how many times we're calling .top5SongsFreq()
            // getting image URLs for the front card
            // creating the name list for the back card
            int index = 0;
            int songIndex = 0;
            while (index < imageUrls.length && songIndex < 5) {
                for (String name: playlistNames) {
                    List<PlaylistSongs.Song> topSongs = Objects.requireNonNull(playlists.get(name)).top5SongsFreq();
                    if (songIndex < topSongs.size()) {
                        PlaylistSongs.Song topSong = topSongs.get(songIndex);
                        imageUrls[index++] = topSong.getUrlToImage();
                    }
                    if (index == imageUrls.length) {
                        break;
                    }
                }
                songIndex++;
            }

            // load into UI
            List<String> finalPlaylistNames = playlistNames;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("EndOfWrapped runOnUiThread() call");
                    for (int i = 0; i < covers.length; i++) {
                        // System.out.println(imageUrls[i]);
                        Picasso.get().load(imageUrls[i]).into(covers[i]);
                    }
                }
            });
        });
    }

}
