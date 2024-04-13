package com.example.spotifywrappedapplication;


public class FragmentData {
    private String color;
    private String name;
    WordSearchGenerator wordSearch;

    public FragmentData(String color) {
        this.color = color;
    }
    public FragmentData(String color, String name, WordSearchGenerator wordSearch) {
        this.name = name;
        this.color = color;
        this.wordSearch = wordSearch;
    }

    public String getColor() {
        return color;
    }
    public String getName(){ return name; }
}
