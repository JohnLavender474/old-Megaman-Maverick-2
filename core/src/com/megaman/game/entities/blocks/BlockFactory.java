package com.megaman.game.entities.blocks;

import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.ConstKeys;
import com.megaman.game.MegamanGame;
import com.megaman.game.entities.EntityFactory;
import com.megaman.game.entities.EntityPool;

public class BlockFactory implements EntityFactory<Block> {

    public static final String STANDARD = "Standard";

    private final ObjectMap<String, EntityPool<? extends Block>> pools;

    public BlockFactory(MegamanGame game) {
        this.pools = new ObjectMap<>() {{
            put(STANDARD, new EntityPool<>(200, () -> new Block(game)));
        }};
    }

    @Override
    public Block fetch(String key) {
        if (key == null) {
            key = STANDARD;
        }
        return pools.get(key).fetch();
    }

}
