package com.megaman.game.entities.blocks;

import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.MegamanGame;
import com.megaman.game.entities.EntityFactory;
import com.megaman.game.entities.EntityPool;

public class BlockFactory implements EntityFactory<Block> {

    private final EntityPool<Block> standardBlockPool;
    private final ObjectMap<String, EntityPool<? extends Block>> pools = new ObjectMap<>();

    public BlockFactory(MegamanGame game) {
        standardBlockPool = new EntityPool<>(200, () -> new Block(game));
    }

    @Override
    public Block fetch(String key) {
        if (key == null) {
            return standardBlockPool.fetch();
        }
        return pools.get(key).fetch();
    }

}
