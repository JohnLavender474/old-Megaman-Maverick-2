package com.megaman.game.assets;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.OrderedMap;

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

    public TextureAtlas getTextureAtlas(TextureAsset t) {
        return getAsset(t.getSrc(), TextureAtlas.class);
    }

    public TextureRegion getTextureRegion(TextureAsset t, String region) {
        return getTextureAtlas(t).findRegion(region);
    }

    public Sound getSound(SoundAsset ass) {
        return getAsset(ass.getSrc(), Sound.class);
    }

    public Music getMusic(MusicAsset ass) {
        return getAsset(ass.getSrc(), Music.class);
    }

    public OrderedMap<SoundAsset, Sound> getSound() {
        OrderedMap<SoundAsset, Sound> sounds = new OrderedMap<>();
        for (SoundAsset ass : SoundAsset.values()) {
            sounds.put(ass, getSound(ass));
        }
        return sounds;
    }

    public OrderedMap<MusicAsset, Music> getMusic() {
        OrderedMap<MusicAsset, Music> music = new OrderedMap<>();
        for (MusicAsset ass : MusicAsset.values()) {
            music.put(ass, getMusic(ass));
        }
        return music;
    }

    @Override
    public void dispose() {
        assMan.dispose();
    }

}
