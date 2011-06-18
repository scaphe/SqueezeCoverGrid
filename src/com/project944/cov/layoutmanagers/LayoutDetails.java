package com.project944.cov.layoutmanagers;

public class LayoutDetails {

    public final int id;
    public final String artist;
    public final String album;
    public final int x;
    public final int y;
    public final boolean undefPosn;
    public final boolean hidden;
    public final boolean variousArtists;
            
    public LayoutDetails(int id,
                         String artist,
                         String album,
                         int x,
                         int y,
                         boolean undefPosn,
                         boolean hidden,
                         boolean variousArtists) {
        this.id = id;
        this.artist = artist;
        this.album = album;
        this.x = x;
        this.y = y;
        this.undefPosn = undefPosn;
        this.hidden = hidden;
        this.variousArtists = variousArtists;
    }

}
