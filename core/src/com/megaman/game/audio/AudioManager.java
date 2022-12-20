package com.megaman.game.audio;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

public class AudioManager {

    public int soundEffectsVolume = 5;
    public int musicVolume = 5;

    public void changeSoundEffectVolume(int delta) {
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
        music.setLooping(loop);
        music.setVolume(musicVolume / 10f);
        music.play();
    }

}
