package com.megaman.game.dialogue;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.megaman.game.assets.AssetsManager;
import com.megaman.game.assets.SoundAsset;
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.audio.AudioManager;
import com.megaman.game.screens.ui.TextHandle;
import com.megaman.game.utils.interfaces.Drawable;
import com.megaman.game.utils.interfaces.Updatable;
import com.megaman.game.utils.objs.Timer;
import lombok.Getter;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Supplier;

public class Dialogue implements Updatable, Drawable {

    private final TextHandle text;
    private final Supplier<Boolean> speedUp;
    private final Supplier<Boolean> finisher;
    private final Sprite blackBox = new Sprite();
    private final Timer dialogueTypingTimer = new Timer(.1f);
    private final Queue<Queue<Runnable>> dialogueQ = new LinkedList<>();

    @Getter
    private boolean finished;

    public Dialogue(AssetsManager assetsManager, AudioManager audioManager, Collection<String> lines,
                    Supplier<Boolean> finisher, Supplier<Boolean> speedUp) {
        this.finisher = finisher;
        this.speedUp = speedUp;
        // text
        text = new TextHandle(new Vector2());
        // black box
        TextureRegion blackRegion = assetsManager.getAsset(TextureAsset.DECORATIONS.getSrc(), TextureAtlas.class)
                .findRegion("Black");
        blackBox.setRegion(blackRegion);
        blackBox.setBounds(0f, 0f, 0f, 0f);
        // sound
        Sound typingSound = assetsManager.getAsset(SoundAsset.THUMP_SOUND.getSrc(), Sound.class);
        // lines anim queue
        lines.forEach(line -> dialogueQ.add(DialogueAnimQ.getDialogueAnimQ(audioManager, text, line, typingSound)));
    }

    @Override
    public void draw(SpriteBatch batch) {
        blackBox.draw(batch);
        text.draw(batch);
    }

    @Override
    public void update(float delta) {
        // if dialogue q is empty and the finished returns true at least once, then this sequence is finished
        if (dialogueQ.isEmpty()) {
            if (finisher.get()) {
                finished = true;
            }
            return;
        }
        // if current line q is finished, then remove it
        if (dialogueQ.peek().isEmpty()) {
            if (finisher.get()) {
                dialogueQ.poll();
            }
            return;
        }
        // if timer is finished, then run next anim updatable
        if (speedUp.get()) {
            dialogueTypingTimer.update(delta * 2f);
        } else {
            dialogueTypingTimer.update(delta);
        }
        if (dialogueTypingTimer.isFinished()) {
            dialogueQ.peek().poll().run();
            dialogueTypingTimer.reset();
        }
    }

}
