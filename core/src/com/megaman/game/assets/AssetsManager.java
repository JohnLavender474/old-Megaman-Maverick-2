package com.megaman.game.assets;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;

public class AssetsManager implements Disposable {

    private final AssetManager assMan = new AssetManager();

    public void loadAssets() {
        for (MusicAsset musicAsset : MusicAsset.values()) {
            assMan.load(musicAsset.getSrc(), musicAsset.getAssClass());
        }
        for (SoundAsset soundAsset : SoundAsset.values()) {
            assMan.load(soundAsset.getSrc(), soundAsset.getAssClass());
        }
        for (TextureAsset textureAsset : TextureAsset.values()) {
            assMan.load(textureAsset.getSrc(), textureAsset.getAssClass());
        }
        assMan.finishLoading();
    }

    public <T> T getAsset(String asset, Class<T> tClass) {
        return assMan.get(asset, tClass);
    }

    public TextureRegion getTextureRegion(TextureAsset t, String region) {
        return getAsset(t.getSrc(), TextureAtlas.class).findRegion(region);
    }

    public Sound getSound(SoundAsset s) {
        return getAsset(s.getSrc(), Sound.class);
    }

    public Music getMusic(MusicAsset m) {
        return getAsset(m.getSrc(), Music.class);
    }

    @Override
    public void dispose() {
        assMan.dispose();
    }

}
