package com.megaman.game.entities.blocks;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.MegamanGame;
import com.megaman.game.entities.Entity;
import com.megaman.game.entities.EntityType;
import com.megaman.game.world.Body;
import com.megaman.game.world.BodyType;
import com.megaman.game.world.Fixture;
import com.megaman.game.world.FixtureType;

public class Block extends Entity {

    public static final float STANDARD_FRIC_X = .035f;
    public static final float STANDARD_FRIC_Y = .035f;

    public final Body body = new Body(BodyType.STATIC);

    public Block(MegamanGame game) {
        super(game, EntityType.BLOCK);
    }

    @Override
    public void init(Rectangle bounds, ObjectMap<String, Object> data) {
        body.bounds.set(bounds);
        if (data.containsKey(BlockDataKey.FRICTION_X.key)) {
            body.friction.x = (float) data.get(BlockDataKey.FRICTION_X.key);
        } else {
            body.friction.x = STANDARD_FRIC_X;
        }
        if (data.containsKey(BlockDataKey.FRICTION_Y.key)) {
            body.friction.y = (float) data.get(BlockDataKey.FRICTION_Y.key);
        } else {
            body.friction.y = STANDARD_FRIC_Y;
        }
        if (data.containsKey(BlockDataKey.GRAVITY_ON.key)) {
            body.gravityOn = (boolean) data.get(BlockDataKey.GRAVITY_ON.key);
        }
        if (data.containsKey(BlockDataKey.RESIST_ON.key)) {
            body.affectedByResistance = (boolean) data.get(BlockDataKey.RESIST_ON.key);
        }
        Fixture blockFixture = new Fixture(this, FixtureType.BLOCK, bounds);
        body.fixtures.add(blockFixture);
    }

}
