package com.example.spotifywrappedapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FullScreenRedFragment extends Fragment {
    final int rowCount = 10;
    final int colCount = 10;
    final int difficulty = 3;

    View view;

    GridLayout wordSearchGrid;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.fragment_full_screen_red,container, false);


        // Initialize game logic

        // User can select from several different types of games

        // The only game that will be implemented in this round is the word search game

        // The word search window will be have saved games, game 1, game 2, etc

        // If user wants to make new word search game, they can select from their playlist and a new game will come up

        SharedViewModel viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        viewModel.getComplexData().observe(getViewLifecycleOwner(), new Observer<FragmentData>() {
            @Override
            public void onChanged(FragmentData myObject) {
                // Use the shared object here
                updateUI(myObject);
            }
        });

        Button exitButton = view.findViewById(R.id.exitButton);
        exitButton.setOnClickListener(v -> {
            // Launch an asynchronous process to save the data to firebase
            viewModel.storeDataInFirebase();

            // Handle back press or exit
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
                getActivity().findViewById(R.id.recyclerView).setVisibility(View.VISIBLE);  // Show RecyclerView
                getActivity().findViewById(R.id.openAlertButton).setVisibility(View.VISIBLE);  // Show Button
                getActivity().findViewById(R.id.back_to_stats_button).setVisibility(View.VISIBLE);
            }
        });
        return view;
    }

    private void updateUI(FragmentData myObject) {
        // Update your UI here based on the shared object
        WordSearchGenerator wordSearch = myObject.wordSearch;
        wordSearch.printGrid();
        wordSearch.printWordsWithPositions();
        Set<String> visited = new HashSet<>();

        // Get screen width
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int cellSize = screenWidth / colCount; // Calculate cell size to fit screen width
        wordSearchGrid = view.findViewById(R.id.word_search_grid);
        wordSearchGrid.setColumnCount(colCount);
        wordSearchGrid.setRowCount(rowCount);

        // Populate the grid with buttons
        for (int row = 0; row < rowCount; row++) {
            for (int col = 0; col < colCount; col++) {
                Button button = new Button(getContext());
                button.setLayoutParams(new ViewGroup.LayoutParams(cellSize, cellSize)); // Square cells
                button.setText(wordSearch.getDisplayText(row,col)); // Example content
                final int finalRow = row;
                final int finalCol = col;
                int finalRow1 = row;
                int finalCol1 = col;
                button.setOnClickListener(view -> {
                    Map<String, Integer> group = wordSearch.tap(finalRow1, finalCol1);
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
        TextView bottomtext = view.findViewById(R.id.game_strat_view);
        bottomtext.setText(String.join("\n", wordSearch.getWords()));

    }


}
