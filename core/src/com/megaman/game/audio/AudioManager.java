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

    private static final float MAX_VOLUME = 10f;
    private static final float MIN_VOLUME = 0f;
    private static final float DEFAULT_VOLUME = 5f;

    @RequiredArgsConstructor
    private static final class SoundEntry {

        private final long id;
        private final SoundAsset ass;

        private float time;

    }

    private final OrderedMap<SoundAsset, Sound> sounds;
    private final OrderedMap<MusicAsset, Music> music;
    private final Array<SoundEntry> playingSounds;

    private float soundVolume;
    private float musicVolume;
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

    public void pause() {
        for (Sound s : sounds.values()) {
            s.pause();
        }
        pauseMusic();
    }

    public void resume() {
        for (Sound s : sounds.values()) {
            s.resume();
        }
        resumeMusic();
    }

    public void changeSoundVolume(float delta) {
        soundVolume += delta;
        if (soundVolume > MAX_VOLUME) {
            soundVolume = MAX_VOLUME;
        }
        if (soundVolume < MIN_VOLUME) {
            soundVolume = MIN_VOLUME;
        }
        for (SoundEntry e : playingSounds) {
            Sound s = sounds.get(e.ass);
            s.setVolume(e.id, soundVolume);
        }
    }

    public void changeMusicVolume(float delta) {
        musicVolume += delta;
        if (musicVolume > MAX_VOLUME) {
            musicVolume = MAX_VOLUME;
        }
        if (musicVolume < MIN_VOLUME) {
            musicVolume = MIN_VOLUME;
        }
        for (Music m : music.values()) {
            m.setVolume(musicVolume);
        }
    }

    public void playSound(SoundAsset ass) {
        Sound sound = sounds.get(ass);
        long id = sound.play(soundVolume / MAX_VOLUME);
        playingSounds.add(new SoundEntry(id, ass));
    }

    public void stopSound(SoundAsset ass) {
        sounds.get(ass).stop();
    }

    public void playMusic(Music music, boolean loop) {
        if (currMusic != null) {
            currMusic.stop();
        }
        currMusic = music;
        currMusic.setLooping(loop);
        currMusic.setVolume(musicVolume / MAX_VOLUME);
        currMusic.play();
    }

    public void pauseMusic() {
        if (currMusic == null) {
            return;
        }
        currMusic.pause();
    }

    public void resumeMusic() {
        if (currMusic == null || currMusic.isPlaying()) {
            return;
        }
        currMusic.play();
    }

}
