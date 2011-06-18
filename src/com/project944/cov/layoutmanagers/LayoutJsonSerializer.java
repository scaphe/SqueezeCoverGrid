package com.project944.cov.layoutmanagers;

import org.json.JSONObject;

import com.project944.cov.CoverDetails;

public class LayoutJsonSerializer {

    private static final String id_key = "id";
    private static final String artist_key = "artist";
    private static final String album_key = "album";
    private static final String x_key = "x";
    private static final String y_key = "y";
    private static final String undefPosn_key = "undefPos";
    private static final String hidden_key = "hidden";
    private static final String variousArtists_key = "va";
    
    public JSONObject serialize(CoverDetails cover) throws Exception {
        JSONObject ans = new JSONObject();
        ans.put(id_key, cover.getId());
        ans.put(artist_key, cover.getArtist());
        ans.put(album_key, cover.getAlbum());
        ans.put(x_key, cover.getX());
        ans.put(y_key, cover.getY());
        ans.put(undefPosn_key, cover.isUndefinedPosition());
        ans.put(hidden_key, cover.isHidden());
        ans.put(variousArtists_key, cover.isVariousArtists());
        return ans;
    }
    
    public LayoutDetails deserialize(JSONObject json) throws Exception {
        Integer id = (Integer) json.get(id_key);
        String artist = (String) json.get(artist_key);
        String album = (String) json.get(album_key);
        Integer x = (Integer) json.get(x_key);
        Integer y = (Integer) json.get(y_key);
        Boolean undefPos = (Boolean) json.get(undefPosn_key);
        Boolean hidden = (Boolean) json.get(hidden_key);
        Boolean variousArtists = (Boolean) json.get(variousArtists_key);
        
        LayoutDetails details = new LayoutDetails(id.intValue(),
                artist, album,
                x.intValue(), y.intValue(),
                undefPos.booleanValue(),
                hidden.booleanValue(),
                variousArtists.booleanValue()
                );
        return details;
    }
}
