package com.github.moribund.audio;

import com.badlogic.gdx.audio.Music;
import lombok.val;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@code MusicPlayer} class plays {@link MusicFile}s using the
 * {@link MusicContainer} class.
 */
public class MusicPlayer {
    /**
     * The list of {@link Music} playing to keep track of them for disposal.
     */
    private List<Music> musicPlaying;

    /**
     * The constructor to make a new music player dependency.
     */
    public MusicPlayer() {
        musicPlaying = new ArrayList<>();
    }

    /**
     * Plays a given {@link MusicFile}.
     * @param musicFile The {@link MusicFile} to play.
     * @param looping If this {@link Music} will loop or not.
     */
    public void play(MusicFile musicFile, boolean looping) {
        val music = MusicContainer.getInstance().getMusic(musicFile);
        music.setLooping(looping);
        music.play();
        musicPlaying.add(music);
    }

    /**
     * Disposes of all the current {@link MusicPlayer#musicPlaying}
     * using the {@link Music#dispose()} method of each item in the list.
     */
    public void dispose() {
        musicPlaying.forEach(Music::dispose);
    }
}
