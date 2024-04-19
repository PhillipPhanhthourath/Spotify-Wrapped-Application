package com.example.spotifywrappedapplication;


public class FragmentData {
    private String color;
    private String name;
    private String imageUrl;
    WordSearchGenerator wordSearch;

    public FragmentData(String color) {
        this.color = color;
    }
    public FragmentData(String color, String name, String imageUrl, WordSearchGenerator wordSearch) {
        this.name = name;
        this.color = color;
        this.wordSearch = wordSearch;
        this.imageUrl = imageUrl;
    }

    public String getColor() {
        return color;
    }
    public String getImageUrl(){
        return imageUrl;
    }
    public String getName(){ return name; }
}
