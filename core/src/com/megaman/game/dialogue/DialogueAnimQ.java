package com.megaman.game.dialogue;

import com.badlogic.gdx.audio.Sound;
import com.megaman.game.audio.AudioManager;
import com.megaman.game.screens.ui.TextHandle;

import java.util.LinkedList;
import java.util.Queue;

public class DialogueAnimQ {

    public static Queue<Runnable> getDialogueAnimQ(AudioManager audioManager, TextHandle m, String s, Sound sound) {
        Queue<Runnable> q = new LinkedList<>();
        for (int i = 0; i < s.length(); i++) {
            final int finalI = i;
            q.add(() -> {
                m.setText(s.substring(0, finalI + 1));
                if (Character.isWhitespace(s.charAt(finalI))) {
                    return;
                }
                audioManager.playSound(sound, false);
            });
        }
        return q;
    }

}
