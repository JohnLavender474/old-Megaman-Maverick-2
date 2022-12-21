package com.megaman.game.entities.blocks;

import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.ConstKeys;
import com.megaman.game.MegamanGame;
import com.megaman.game.entities.EntityFactory;
import com.megaman.game.entities.EntityPool;
import com.megaman.game.entities.blocks.impl.ConveyorBelt;

public class BlockFactory implements EntityFactory<Block> {

    public static final String STANDARD = "Standard";
    public static final String CONVEYOR_BELT = "ConveyorBelt";

    private final ObjectMap<String, EntityPool<? extends Block>> pools;

    public BlockFactory(MegamanGame game) {
        this.pools = new ObjectMap<>() {{
            put(STANDARD, new EntityPool<>(25, () -> new Block(game)));
            put(CONVEYOR_BELT, new EntityPool<>(5, () -> new ConveyorBelt(game)));
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
