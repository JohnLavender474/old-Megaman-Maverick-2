package com.megaman.game.audio;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.OrderedMap;
import com.megaman.game.assets.MusicAsset;
import com.megaman.game.assets.SoundAsset;
import com.megaman.game.utils.interfaces.Updatable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Iterator;

@Getter
public class AudioManager implements Updatable {

    private static final int MIN_VOLUME = 0;
    private static final int MAX_VOLUME = 10;
    private static final int DEFAULT_VOLUME = 5;
    // private static final int DEFAULT_VOLUME = 0;

    @RequiredArgsConstructor
    private static final class SoundEntry {

        private final long id;
        private final SoundAsset ass;

        private float time;

    }

    private final OrderedMap<SoundAsset, Sound> sounds;
    private final OrderedMap<MusicAsset, Music> music;
    private final Array<SoundEntry> playingSounds;

    private int soundVolume;
    private int musicVolume;
    private Music currMusic;

    public AudioManager(OrderedMap<SoundAsset, Sound> sounds, OrderedMap<MusicAsset, Music> music) {
        this.music = music;
        this.sounds = sounds;
        playingSounds = new Array<>();
        musicVolume = DEFAULT_VOLUME;
        soundVolume = DEFAULT_VOLUME;
    }

    @Override
    public void update(float delta) {
        Iterator<SoundEntry> eIter = playingSounds.iterator();
        while (eIter.hasNext()) {
            SoundEntry e = eIter.next();
            e.time += delta;
            if (e.time > e.ass.getSeconds()) {
                eIter.remove();
            }
        }
    }

    public void setSoundVolume(int newVolume) {
        soundVolume = newVolume;
        if (soundVolume > MAX_VOLUME) {
            soundVolume = MAX_VOLUME;
        }
        if (soundVolume < MIN_VOLUME) {
            soundVolume = MIN_VOLUME;
        }
        for (SoundEntry e : playingSounds) {
            Sound s = sounds.get(e.ass);
            s.setVolume(e.id, (float) soundVolume / MAX_VOLUME);
        }
    }

    public void setMusicVolume(int newVolume) {
        musicVolume = newVolume;
        if (musicVolume > MAX_VOLUME) {
            musicVolume = MAX_VOLUME;
        }
        if (musicVolume < MIN_VOLUME) {
            musicVolume = MIN_VOLUME;
        }
        for (Music m : music.values()) {
            m.setVolume((float) musicVolume / MAX_VOLUME);
        }
    }

    public void playMusic(SoundAsset ass) {
        Sound sound = sounds.get(ass);
        long id = sound.play((float) soundVolume / MAX_VOLUME);
        playingSounds.add(new SoundEntry(id, ass));
    }

    public void stop(SoundAsset ass) {
        sounds.get(ass).stop();
    }

    public void clearMusic() {
        if (currMusic != null) {
            currMusic.stop();
        }
        currMusic = null;
    }

    public void set(MusicAsset ass, boolean loop) {
        if (currMusic != null) {
            currMusic.stop();
        }
        currMusic = music.get(ass);
        currMusic.setLooping(loop);
        currMusic.setVolume((float) musicVolume / MAX_VOLUME);
    }

    public void playMusic(MusicAsset ass, boolean loop) {
        set(ass, loop);
        if (currMusic != null) {
            currMusic.play();
        }
    }

    public void playMusic() {
        if (currMusic != null && !currMusic.isPlaying()) {
            currMusic.play();
        }
    }

    public void pauseMusic() {
        if (currMusic != null) {
            currMusic.pause();
        }
    }

    public void stopMusic() {
        if (currMusic != null) {
            currMusic.stop();
        }
    }

    public void resumeMusic() {
        if (currMusic != null && !currMusic.isPlaying()) {
            currMusic.play();
        }
    }

    public void pauseSound() {
        for (Sound s : sounds.values()) {
            s.pause();
        }
    }

    public void resumeSound() {
        for (Sound s : sounds.values()) {
            s.resume();
        }
    }

}
