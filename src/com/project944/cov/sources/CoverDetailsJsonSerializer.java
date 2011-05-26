package com.project944.cov.sources;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.project944.cov.CoverDetails;
import com.project944.cov.TrackDetails;

public class CoverDetailsJsonSerializer {

    private static final String id_key = "id";
    private static final String artist_key = "artist";
    private static final String album_key = "album";
    private static final String disc_key = "disc";
    private static final String noImage_key = "noImg";
    private static final String tracks_key = "tracks";
    private static final String title_key = "title";
    private static final String length_key = "length";
    
    public JSONObject serialize(CoverDetails cover) throws Exception {
        JSONObject ans = new JSONObject();
        ans.put(id_key, cover.getId());
        ans.put(artist_key, cover.getArtist());
        ans.put(album_key, cover.getAlbum());
        ans.put(disc_key, cover.getDiscNumber());
        ans.put(noImage_key, cover.isNoImage());
        JSONArray tracks = new JSONArray();
        for (TrackDetails track : cover.getTrackNames()) {
            JSONObject trackDetails = new JSONObject();
            trackDetails.put(title_key, track.getTitle());
            trackDetails.put(length_key, track.getLengthSeconds());
            tracks.put(trackDetails);
        }
        ans.put(tracks_key, tracks);
        return ans;
    }
    
    public CoverDetails deserialize(JSONObject json) throws Exception {
        Integer id = (Integer) json.get(id_key);
        String artist = (String) json.get(artist_key);
        String album = (String) json.get(album_key);
        Integer discNumber = (Integer) json.get(disc_key);
        Boolean noImage = (Boolean) json.get(noImage_key);
        
        JSONArray tracks = (JSONArray) json.get(tracks_key);
        int len = tracks.length();
        List<TrackDetails> trackNames = new ArrayList<TrackDetails>(len);
        for (int i = 0; i < len; i++) {
            JSONObject trackJson = (JSONObject) tracks.get(i);
            String  title = (String) trackJson.get(title_key);
            Integer lengthSeconds = (Integer) trackJson.get(length_key);
            trackNames.add(new TrackDetails(title, lengthSeconds.intValue()));
        }
        
        Image image = null;  // Not included in this serializer
        CoverDetails cover = new CoverDetails(id.intValue(), artist, album, image, discNumber.intValue(), noImage.booleanValue());
        cover.setTrackNames(trackNames);
        return cover;
    }
}
