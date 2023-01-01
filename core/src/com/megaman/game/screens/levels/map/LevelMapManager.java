package com.megaman.game.screens.levels.map;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import lombok.Getter;

import java.util.EnumMap;
import java.util.Map;

public class LevelMapManager implements Disposable {

    private final OrthographicCamera cam;
    private final SpriteBatch batch;

    private TiledMap map;
    private CustomMapRenderer renderer;

    @Getter
    private int worldWidth;
    @Getter
    private int worldHeight;

    public LevelMapManager(OrthographicCamera cam, SpriteBatch batch) {
        this.cam = cam;
        this.batch = batch;
    }

    public Map<LevelMapLayer, Array<RectangleMapObject>> set(String tmxFile) {
        dispose();
        TmxMapLoader.Parameters params = new TmxMapLoader.Parameters();
        params.textureMinFilter = Texture.TextureFilter.Nearest;
        params.textureMagFilter = Texture.TextureFilter.Nearest;
        map = new TmxMapLoader().load(tmxFile, params);
        renderer = new CustomMapRenderer(map);
        worldWidth = map.getProperties().get("width", Integer.class);
        worldHeight = map.getProperties().get("height", Integer.class);
        Map<LevelMapLayer, Array<RectangleMapObject>> m = new EnumMap<>(LevelMapLayer.class);
        for (LevelMapLayer layerEnum : LevelMapLayer.values()) {
            Array<RectangleMapObject> objs = new Array<>();
            MapLayer layer = map.getLayers().get(layerEnum.name);
            if (layer == null) {
                continue;
            }
            for (MapObject mapObj : layer.getObjects()) {
                if (mapObj instanceof RectangleMapObject rectObj) {
                    objs.add(rectObj);
                }
            }
            m.put(layerEnum, objs);
        }
        return m;
    }

    public void draw() {
        renderer.render(cam, batch);
    }

    @Override
    public void dispose() {
        if (map != null) {
            map.dispose();
        }
        map = null;
        if (renderer != null) {
            renderer.dispose();
        }
        renderer = null;
    }

    private static final class CustomMapRenderer extends OrthogonalTiledMapRenderer {

        private CustomMapRenderer(TiledMap map) {
            super(map);
        }

        public void render(OrthographicCamera cam, Batch batch) {
            this.batch = batch;
            setView(cam);
            super.render();
        }

        @Override
        protected void beginRender() {
            AnimatedTiledMapTile.updateAnimationBaseTime();
        }



        @Override
        protected void endRender() {
        }

        @Override
        public void dispose() {
        }

    }

}
