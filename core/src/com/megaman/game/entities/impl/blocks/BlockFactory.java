package com.megaman.game.entities.impl.blocks;

import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.MegamanGame;
import com.megaman.game.entities.Entity;
import com.megaman.game.entities.factories.EntityFactory;
import com.megaman.game.entities.factories.EntityPool;
import com.megaman.game.entities.impl.blocks.impl.ConveyorBelt;
import com.megaman.game.entities.impl.blocks.impl.GearTrolley;
import com.megaman.game.entities.impl.blocks.impl.IceBlock;
import com.megaman.game.entities.impl.blocks.impl.RocketPlatform;

public class BlockFactory implements EntityFactory {

    public static final String STANDARD = "Standard";
    public static final String ICE_BLOCK = "IceBlock";
    public static final String GEAR_TROLLEY = "GearTrolley";
    public static final String CONVEYOR_BELT = "ConveyorBelt";
    public static final String ROCKET_PLATFORM = "RocketPlatform";

    private final ObjectMap<String, EntityPool> pools;

    public BlockFactory(MegamanGame game) {
        this.pools = new ObjectMap<>() {{
            put(ICE_BLOCK, new EntityPool(25, () -> new IceBlock(game)));
            put(STANDARD, new EntityPool(25, () -> new Block(game, true)));
            put(GEAR_TROLLEY, new EntityPool(5, () -> new GearTrolley(game)));
            put(CONVEYOR_BELT, new EntityPool(5, () -> new ConveyorBelt(game)));
            put(ROCKET_PLATFORM, new EntityPool(10, () -> new RocketPlatform(game)));
        }};
    }

    @Override
    public Entity fetch(String key) {
        if (key == null) {
            key = STANDARD;
        }
        return pools.get(key).fetch();
    }

}
