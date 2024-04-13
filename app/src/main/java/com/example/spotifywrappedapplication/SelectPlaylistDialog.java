package com.example.spotifywrappedapplication;

import android.app.Dialog;
import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SelectPlaylistDialog extends Dialog {

    public interface DialogListener {
        void onWordSearchCreated(FragmentData newData);
    }

    public SelectPlaylistDialog(Context context, HashMap<String,List<String>> songOptions, DialogListener listener) {
        super(context);
        setContentView(R.layout.dialog_playlist_difficulty);

        List<String> playlistNames = new ArrayList<>(songOptions.keySet()); // Assuming songOptions is available here

        Spinner spinnerPlaylists = findViewById(R.id.spinnerPlaylists);
        Spinner spinnerDifficulty = findViewById(R.id.spinnerDifficulty);
        Button buttonCreate = findViewById(R.id.buttonCreate);
        Button buttonCancel = findViewById(R.id.buttonCancel);

        // Setting up playlist spinner
        ArrayAdapter<String> playlistAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, playlistNames);
        spinnerPlaylists.setAdapter(playlistAdapter);

        // Setting up difficulty spinner
        ArrayAdapter<String> difficultyAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, new String[]{"1", "2", "3"});
        spinnerDifficulty.setAdapter(difficultyAdapter);

        // Create button
        buttonCreate.setOnClickListener(v -> {
            int selectedDifficulty = Integer.parseInt(spinnerDifficulty.getSelectedItem().toString());
            String selectedPlaylist = spinnerPlaylists.getSelectedItem().toString();
            List<String> words = songOptions.get(selectedPlaylist);  // Assuming songOptions map from playlist name to list of words

            WordSearchGenerator wordSearch = new WordSearchGenerator(words, selectedDifficulty, 10, 10);
            FragmentData newData = new FragmentData("#FFFFFF", selectedPlaylist, wordSearch);  // Assuming default color is white

            listener.onWordSearchCreated(newData);
            dismiss();
        });

        // Cancel button
        buttonCancel.setOnClickListener(v -> dismiss());
    }
}
