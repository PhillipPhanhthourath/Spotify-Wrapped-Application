package com.example.spotifywrappedapplication;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

public class WordSearchGenerator {
    private final char[][] grid;
    private final int[][] stateGrid;
    private List<String> words;
    private final int difficulty;
    private final List<List<GridPosition>> wordPositions;
    private final Random random = new Random();

    public List<String> getWords(){
        return words;
    }
    public List<String> processWords(List<String> words, int n) {
        List<String> processedWords = new ArrayList<>();

        for (String word : words) {
            // Capitalize letters and remove spaces
            String processed = word.toUpperCase().replace(" ", "");

            // Add to the new list if the processed word meets the criteria
            if (processed.length() <= n && !processed.contains("(") && !processed.contains(")")) {
                processedWords.add(processed);
            }
        }
        // Remove duplicates by converting the list to a LinkedHashSet and back to a list
        Set<String> setWithoutDuplicates = new LinkedHashSet<>(processedWords);
        processedWords = new ArrayList<>(setWithoutDuplicates);
        return processedWords;
    }

    public WordSearchGenerator(List<String> words, int difficulty, int rows, int cols) {
        this.words = new ArrayList<>(processWords(words,rows));
        this.difficulty = difficulty;
        this.grid = new char[rows][cols];
        this.stateGrid = new int[rows][cols];
        this.wordPositions = new ArrayList<>();
        initializeGrid();
        placeWords();
    }

    static class GridPosition {
        int row;
        int col;

        GridPosition(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }

    private void initializeGrid() {
        for (int i = 0; i < grid.length; i++) {
            Arrays.fill(stateGrid[i], 0); // 0 for unselected
            Arrays.fill(grid[i], ' '); // Empty space placeholder
        }
    }

    private void placeWords() {
        List<String> wordsnew = new ArrayList<>();
        for (String word : words) {
            boolean placed = false;
            int attempts = 0;
            List<GridPosition> positions;

            while (!placed && attempts < 500) {
                int row = random.nextInt(grid.length);
                int col = random.nextInt(grid[0].length);
                int direction = random.nextInt(difficulty == 1 ? 2 : (difficulty == 2 ? 4 : 8));
                positions = new ArrayList<>();

                placed = tryPlaceWord(word, row, col, direction, positions);
                if (placed) {
                    wordPositions.add(positions);
                    wordsnew.add(word);
                }
                attempts++;
            }
        }
        this.words=wordsnew;
        fillEmptySpaces();
    }

    private boolean tryPlaceWord(String word, int row, int col, int direction, List<GridPosition> positions) {
        int[] rowIncrement = {0, 1, 1, 1, 0, -1, -1, -1};
        int[] colIncrement = {1, 1, 0, -1, -1, -1, 0, 1};
        int length = word.length();

        int endRow = row + rowIncrement[direction] * (length - 1);
        int endCol = col + colIncrement[direction] * (length - 1);

        // Check boundaries
        if (endRow < 0 || endRow >= grid.length || endCol < 0 || endCol >= grid[0].length) {
            return false;
        }

        // Check if the word fits
        for (int i = 0; i < length; i++) {
            int currentRow = row + i * rowIncrement[direction];
            int currentCol = col + i * colIncrement[direction];

            if (grid[currentRow][currentCol] != ' ' && grid[currentRow][currentCol] != word.charAt(i)) {
                return false;
            }
            positions.add(new GridPosition(currentRow, currentCol));
        }

        // Place the word
        for (int i = 0; i < length; i++) {
            GridPosition position = positions.get(i);
            grid[position.row][position.col] = word.charAt(i);
        }

        return true;
    }

    private void fillEmptySpaces() {
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                if (grid[i][j] == ' ') {
                    grid[i][j] = (char) ('A' + random.nextInt(26)); // Random letter
                }
            }
        }
    }

    public String getDisplayText(int row, int col) {
        return String.valueOf(grid[row][col]);
    }

    public Map<String, Integer> tap(int row, int col) {
        if (stateGrid[row][col] == 0) {
            stateGrid[row][col] = 1; // Selected
        } else if (stateGrid[row][col] == 1) {
            stateGrid[row][col] = 0; // Unselected
        }

        // Check if a word is found
        int wordi=0;
        for (List<GridPosition> wordPosition : wordPositions) {
            if (wordPosition.stream().allMatch(pos -> stateGrid[pos.row][pos.col] == 1)) {
                for (GridPosition pos : wordPosition) {
                    stateGrid[pos.row][pos.col] = 2; // Found
                }
                Map<String, Integer> result = new HashMap<>();
                result.put("state",2);
                result.put("word",wordi);
                return result; // Return the positions of the found word
            }
            wordi++;
        }
        Map<String, Integer> result = new HashMap<>();
        result.put("state",stateGrid[row][col]);
        result.put("word",-1);
        return result; // No word found
    }

    public List<GridPosition> get_word_position_from_id(int id){
        return this.wordPositions.get(id);
    }

    public void printGrid() {
        for (char[] row : grid) {
            for (char cell : row) {
                System.out.print(cell + " ");
            }
            System.out.println();
        }
    }

    public void printWordsWithPositions() {
        System.out.println("Words and their positions:");
        for (int wordIndex = 0; wordIndex < words.size(); wordIndex++) {
            String word = words.get(wordIndex);
            List<GridPosition> positions = wordPositions.get(wordIndex);

            System.out.println(word + ":");
            System.out.println("Row\tCol\tLetter");

            for (GridPosition pos : positions) {
                System.out.println(pos.row + "\t" + pos.col + "\t" + grid[pos.row][pos.col]);
            }
            System.out.println();
        }
    }

}
