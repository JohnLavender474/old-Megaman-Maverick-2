package com.megaman.game.audio;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import lombok.Getter;

@Getter
public class AudioManager {

    private int soundEffectsVolume = 5;
    private int musicVolume = 5;
    private Music currMusic;

    public void changeSoundVolume(int delta) {
        soundEffectsVolume += delta;
        if (soundEffectsVolume > 10) {
            soundEffectsVolume = 10;
        }
        if (soundEffectsVolume < 0) {
            soundEffectsVolume = 0;
        }
    }

    public void changeMusicVolume(int delta) {
        musicVolume += delta;
        if (musicVolume > 10) {
            musicVolume = 10;
        }
        if (musicVolume < 0) {
            musicVolume = 0;
        }
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
