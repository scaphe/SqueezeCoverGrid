package com.project944.cov;

public class TrackDetails {
    private String title;
    private int lengthSeconds;
    
    public TrackDetails(String title, int lengthSeconds) {
        this.title = title;
        this.lengthSeconds = lengthSeconds;
    }
    
    public String getTitle() {
        return title;
    }
    
    public int getLengthSeconds() {
        return lengthSeconds;
    }
}
