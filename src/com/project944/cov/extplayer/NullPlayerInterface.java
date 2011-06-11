package com.project944.cov.extplayer;

import javax.swing.JButton;

import com.project944.cov.CoverDetails;

public class NullPlayerInterface implements PlayerInterface {

    public void clearPlayQueue() {
    }
    public void enqueueAlbum(CoverDetails cover, boolean playImmediately) {
    }
    public void enqueueTrack(CoverDetails cover, String trackName, boolean playImmediately) {
    }
    public boolean isPlaying() {
        return false;
    }
    public void pause() {
    }
    public void play() {
    }
    public void playNext() {
    }
    public void playPrev() {
    }
    public void setPlayPauseButton(JButton button) {
    }
}
