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
import com.megaman.game.utils.Logger;
import com.megaman.game.world.*;

public class Block extends Entity {

    private static final Logger logger = new Logger(Block.class, MegamanGame.DEBUG && true);

    public static final String RESIST_ON = "ResistOn";
    public static final String GRAVITY_ON = "GravityOn";
    public static final String FRICTION_X = "FrictionX";
    public static final String FRICTION_Y = "FrictionY";

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
        body.add(blockFixture);
        putComponent(new BodyComponent(body));
        if (MegamanGame.DEBUG) {
            putComponent(shapeComponent());
        }
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
        if (data.containsKey(FRICTION_X)) {
            body.friction.x = (float) data.get(FRICTION_X);
        } else {
            body.friction.x = STANDARD_FRIC_X;
        }
        if (data.containsKey(FRICTION_Y)) {
            body.friction.y = (float) data.get(FRICTION_Y);
        } else {
            body.friction.y = STANDARD_FRIC_Y;
        }
        if (data.containsKey(GRAVITY_ON)) {
            body.gravityOn = (boolean) data.get(GRAVITY_ON);
        }
        if (data.containsKey(RESIST_ON)) {
            body.affectedByResistance = (boolean) data.get(RESIST_ON);
        }
        if (data.containsKey(BodyLabel.BODY_LABEL)) {
            for (String label : ((String) data.get(BodyLabel.BODY_LABEL)).replace("\\s+", "").split(",")) {
                logger.log("Adding label: " + label);
                body.labels.add(label);
            }
        }
    }

}
