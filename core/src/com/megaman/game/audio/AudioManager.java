package com.megaman.game.audio;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import lombok.Getter;

@Getter
public class AudioManager {

    private float soundEffectsVolume = 5f;
    private float musicVolume = 5f;
    private Music currMusic;

    public void changeSoundVolume(float delta) {
        soundEffectsVolume += delta;
        if (soundEffectsVolume > 10f) {
            soundEffectsVolume = 10f;
        }
        if (soundEffectsVolume < 0f) {
            soundEffectsVolume = 0f;
        }
    }

    public void scaleSoundVolume(float scale) {
        float s = soundEffectsVolume / scale;
        changeSoundVolume(s);
    }

    public void changeMusicVolume(float delta) {
        musicVolume += delta;
        if (musicVolume > 10f) {
            musicVolume = 10f;
        }
        if (musicVolume < 0f) {
            musicVolume = 0f;
        }
    }

    public void scaleMusicVolume(float scale) {
        float s = musicVolume / scale;
        changeMusicVolume(s);
    }

    public void playSound(Sound sound, boolean loop) {
        if (loop) {
            sound.loop(soundEffectsVolume / 10f);
        } else {
            sound.play(soundEffectsVolume / 10f);
        }
    }

    public void playMusic(Music music, boolean loop) {
        if (currMusic != null) {
            currMusic.stop();
        }
        currMusic = music;
        currMusic.setLooping(loop);
        currMusic.setVolume(musicVolume / 10f);
        currMusic.play();
    }

}
