package com.project944.cov.extplayer;

import javax.swing.JButton;

import com.project944.cov.CoverDetails;

public interface PlayerInterface {

	void enqueueAlbum(CoverDetails cover, boolean playImmediately);

    void enqueueTrack(CoverDetails cover, String trackName, boolean playImmediately);

    boolean isPlaying();
    void play();
    void playPrev();
    void playNext();
    void pause();

    void clearPlayQueue();

    void setPlayPauseButton(JButton button);
}
