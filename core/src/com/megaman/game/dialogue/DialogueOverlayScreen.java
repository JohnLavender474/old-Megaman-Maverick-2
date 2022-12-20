package com.megaman.game.dialogue;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megaman.game.MegamanGame;
import com.megaman.game.assets.AssetsManager;
import com.megaman.game.audio.AudioManager;

import java.util.Collection;
import java.util.function.Supplier;

public class DialogueOverlayScreen extends ScreenAdapter {

    private final AssetsManager assetsManager;
    private final OrthographicCamera uiCam;
    private final AudioManager audioManager;
    private final SpriteBatch batch;

    public Dialogue dialogue;

    public DialogueOverlayScreen(MegamanGame game) {
        this.assetsManager = game.getAssMan();
        this.audioManager = game.getAudioMan();
        this.batch = game.getBatch();
        this.uiCam = game.getViewportMan().getCam(ViewportType.UI);
    }

    public void set(Collection<String> lines, Supplier<Boolean> finisher, Supplier<Boolean> speedUp) {
        this.dialogue = new Dialogue(assetsManager, audioManager, lines, finisher, speedUp);
    }

    @Override
    public void show() {
        if (dialogue == null) {
            throw new IllegalStateException("Dialogue has not yet been setVertices");
        }
    }

    @Override
    public void render(float delta) {
        dialogue.update(delta);
        batch.setProjectionMatrix(uiCam.combined);
        boolean drawing = batch.isDrawing();
        if (!drawing) {
            batch.begin();
        }
        dialogue.draw(batch);
        batch.end();
    }

    @Override
    public void dispose() {
        dialogue = null;
    }

}
