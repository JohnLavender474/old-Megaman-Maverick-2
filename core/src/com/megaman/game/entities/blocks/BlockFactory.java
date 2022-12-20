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

    /*
    public static void set(GameContext2d gameContext, RectangleMapObject blockObj) {
        Block block;
        if (blockObj.getName() != null) {
            switch (blockObj.getName()) {
                case "jeffy" -> block = new JeffBezosLittleDickRocket(gameContext, blockObj);
                case "gear_trolley" -> block = new GearTrolley(gameContext, blockObj);
                case "conveyor_belt" -> block = new ConveyorBelt(gameContext, blockObj);
                case "ice" -> {
                    block = new Block(gameContext, blockObj);
                    gameContext.addEntity(new Ice(gameContext, blockObj));
                }
                default -> throw new IllegalStateException("No block obj assigned to " + blockObj.getName());
            }
        } else {
            block = new Block(gameContext, blockObj);
        }
        gameContext.addEntity(block);
    }
     */

}
