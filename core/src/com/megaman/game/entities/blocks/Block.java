package com.megaman.game.entities.blocks;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.MegamanGame;
import com.megaman.game.cull.CullOutOfBoundsComponent;
import com.megaman.game.entities.Entity;
import com.megaman.game.entities.EntityType;
import com.megaman.game.shapes.ShapeComponent;
import com.megaman.game.shapes.ShapeHandle;
import com.megaman.game.world.*;

public class Block extends Entity {

    public static final float STANDARD_FRIC_X = .035f;
    public static final float STANDARD_FRIC_Y = 0f;

    public final Body body;
    public final Fixture blockFixture;

    public Block(MegamanGame game, boolean cullOnBodyOOB) {
        this(game, BodyType.STATIC, cullOnBodyOOB);
    }

    public Block(MegamanGame game, BodyType bodyType, boolean cullOnBodyOOB) {
        super(game, EntityType.BLOCK);
        body = new Body(bodyType);
        blockFixture = new Fixture(this, FixtureType.BLOCK, new Rectangle());
        body.fixtures.add(blockFixture);
        putComponent(new BodyComponent(body));
        putComponent(shapeComponent());
        if (cullOnBodyOOB) {
            putComponent(new CullOutOfBoundsComponent(() -> body.bounds));
        }
    }

    protected ShapeComponent shapeComponent() {
        Array<ShapeHandle> h = new Array<>();
        h.add(new ShapeHandle(body.bounds));
        return new ShapeComponent(h);
    }

    @Override
    public void init(Rectangle bounds, ObjectMap<String, Object> data) {
        body.bounds.set(bounds);
        ((Rectangle) blockFixture.shape).set(body.bounds);
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
    }

}
