package com.project944.cov.sources;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.bff.slimserver.SlimDatabase;
import org.bff.slimserver.SlimServer;
import org.bff.slimserver.exception.SlimDatabaseException;
import org.bff.slimserver.musicobjects.SlimAlbum;
import org.bff.slimserver.musicobjects.SlimArtist;
import org.bff.slimserver.musicobjects.SlimSong;

import com.project944.cov.CoverDetails;
import com.project944.cov.TrackDetails;
import com.project944.cov.utils.MyLogger;

public class SlimCoverSource implements CoverSource {

    private SlimServer slimServer;
    private BufferedImage defaultImage;
    private CachedOnFileSystemCS cache;

    public SlimCoverSource(SlimServer slimServer, String defaultImagePath,
                            CachedOnFileSystemCS cache) throws IOException {
        this.slimServer = slimServer;
        this.cache = cache;
        try {
            URL url;
            String filename = defaultImagePath;
            if ( filename.startsWith("/com/project944/cov/resources") ) {
                url = CoverDetails.class.getResource(filename);
                filename = url.getFile();
            } else {
                url = new URL(filename);
            }
            this.defaultImage = ImageIO.read(url);
            //System.out.println("Read image of "+filename);
        } catch  (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }
    
    public List<CoverDetails> getCovers(MyLogger logger) {
        List<CoverDetails> ans = cache.getCovers(logger);
        if ( ans.size() == 0 ) {
            System.out.println("No cache so reading from slimserver");
            ans = refreshFromServer(ans, true, logger);
        }
        return ans;
    }

    public List<CoverDetails> refreshFromServer(List<CoverDetails> prevCovers, boolean forceRefreshAttempt, MyLogger logger) {
        List<CoverDetails> covers = new LinkedList<CoverDetails>();
        SlimDatabase db = new SlimDatabase(slimServer);
        Collection<SlimAlbum> albums = db.getAlbums();
        if ( prevCovers.size() == albums.size() && !forceRefreshAttempt ) {
            // Don't bother doing refresh
            return new ArrayList<CoverDetails>(prevCovers);
        }
        if ( prevCovers.size() == albums.size() ) {
            logger.log("Diff size, I have "+prevCovers.size()+" vs "+albums.size());
        }
        int count = 0;
        for (SlimAlbum album : albums) {
            count++;
//            if ( count > 10 ) break;
//            if ( album.getTitle().equals("3121") ) System.exit(1);
            if ( (count%10) == 0 ) {
                logger.log("On "+count+" out of "+albums.size());
            }
            Collection<SlimArtist> artists;
            String albumTitle = null;
            String artistName = null;
            try {
                int id = Integer.parseInt(album.getId());
                albumTitle = album.getTitle();
                artistName = getArtistForAlbum(db, album);
                // Check if we already have this one, if so then don't bother getting the image which takes ages
                CoverDetails matchingPrevCd = null;
                for (CoverDetails prevCd : prevCovers) {
                    if ( prevCd.getId() == id && prevCd.getArtist().equals(artistName) && prevCd.getAlbum().equals(albumTitle) ) {
                        matchingPrevCd = prevCd;
                        break;
                    }
                }
                if ( matchingPrevCd != null ) {
                    covers.add(matchingPrevCd);
                    continue;
                }
//                    System.out.println("Image url for "+album.getTitle()+", id=["+album.getId()+"] is "+album.getImageUrl());
                Image image = album.getImage();
                int disc = album.getDisc();
                if ( disc == 0 ) {
                    disc = extractDisc(albumTitle);
                }
                CoverDetails cover = new CoverDetails(id,
                        artistName, album.getTitle(), image==null?defaultImage:image, disc, image==null);
                covers.add(cover);
                List<SlimSong> songs = new ArrayList<SlimSong>(db.listSongsForAlbum(album));
                Collections.sort(songs, new Comparator<SlimSong>() {
                    public int compare(SlimSong o1, SlimSong o2) {
                        if ( o1.getTrack() < o2.getTrack() ) return -1;
                        if ( o1.getTrack() == o2.getTrack() ) return 0;
                        return 1;
                    }
                });
                List<TrackDetails> trackNames = new ArrayList<TrackDetails>(songs.size());
                for (SlimSong song : songs) {
                    trackNames.add(new TrackDetails(song.getTitle(), song.getLength()));
//                        System.out.println("Got track "+song.getTrack()+" : "+song.getTitle());
                }
                cover.setTrackNames(trackNames);
            } catch (Exception e) {
                System.out.println("Failed at ["+albumTitle+"] by ["+artistName+"] with "+e);
                e.printStackTrace();
            }
        }
        CoverDetails.fixMultiDiscAlbums(covers);
        List<CoverDetails> ans = covers;
        try {
            cache.saveCovers(ans, logger);
        } catch (Exception e) {
            System.out.println("Failed to save covers to cache with "+e);
            e.printStackTrace();
        }
        logger.finished();
        return ans;
    }

    public static String getArtistForAlbum(SlimDatabase db, SlimAlbum album) {
        String artistName = null;
        Collection<SlimArtist> artists;
        try {
            artists = db.getArtistsForAlbum(album);
            if ( artists != null && artists.size() > 0 ) {
                SlimArtist artist = artists.iterator().next();
                artistName = artist.getArtist();
            }
        } catch (SlimDatabaseException e) {
            e.printStackTrace();
        }
        if ( artistName == null ) {
            System.out.println("Failed to find artists for album "+album.getName());
            return "Unknown";
        }
        return artistName;
    }

    private static int extractDisc(String albumTitle) {
        if ( albumTitle.matches(".* \\(disc [0-9]+\\)") ) {
            // Dig out disc number from album title, odd that server doesn't know it
            try {
                Pattern p = Pattern.compile(".* \\(disc ([0-9]+)\\)");
                Matcher m = p.matcher(albumTitle);
                if ( m.matches() && m.groupCount() > 0 ) {
                    return Integer.parseInt(m.group(1));
                }
            } catch (Exception e) {
                System.out.println("Failed to parse disc out of ["+albumTitle+"] with "+e);
                e.printStackTrace();
            }
        }
        return 0;
    }

    public static void main(String[] args) {
        System.out.println(extractDisc("something (disc 2)"));
    }
}
