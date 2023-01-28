package com.megaman.game.entities.impl.blocks;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.MegamanGame;
import com.megaman.game.cull.CullOutOfBoundsComponent;
import com.megaman.game.entities.Entity;
import com.megaman.game.entities.EntityType;
import com.megaman.game.shapes.ShapeComponent;
import com.megaman.game.utils.Logger;
import com.megaman.game.world.*;

public class Block extends Entity {

    protected static final Logger logger = new Logger(Block.class, MegamanGame.DEBUG && true);

    protected static final String RESIST_ON = "ResistOn";
    protected static final String GRAVITY_ON = "GravityOn";
    protected static final String FRICTION_X = "FrictionX";
    protected static final String FRICTION_Y = "FrictionY";

    protected static final float STANDARD_FRIC_X = .035f;
    protected static final float STANDARD_FRIC_Y = 0f;

    public final Body body;
    public final Fixture blockFixture;

    public Block(MegamanGame game, boolean cullOnBodyOOB) {
        this(game, BodyType.STATIC, cullOnBodyOOB);
    }

    public Block(MegamanGame game, boolean cullOnBodyOOB, float width, float height) {
        this(game, BodyType.STATIC, cullOnBodyOOB, width, height);
    }

    public Block(MegamanGame game, BodyType bodyType, boolean cullOnBodyOOB, float width, float height) {
        this(game, bodyType, cullOnBodyOOB);
        setSize(width, height);
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

    @Override
    public void init(Vector2 spawn, ObjectMap<String, Object> data) {
        setPos(spawn);
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

    @Override
    public void init(Rectangle bounds, ObjectMap<String, Object> data) {
        setSize(bounds.width, bounds.height);
        Vector2 pos = new Vector2();
        bounds.getPosition(pos);
        init(pos, data);
    }

    protected ShapeComponent shapeComponent() {
        return new ShapeComponent(body.bounds);
    }

    public void setX(float x) {
        body.setX(x);
    }

    public void setMaxX(float x) {
        body.setMaxX(x);
    }

    public void setY(float y) {
        body.setY(y);
    }

    public void setMaxY(float y) {
        body.setMaxY(y);
    }

    public void setPos(Vector2 pos) {
        setPos(pos.x, pos.y);
    }

    public void setPos(float x, float y) {
        setX(x);
        setY(y);
    }

    public void setWidth(float width) {
        body.setWidth(width);
        ((Rectangle) blockFixture.shape).setWidth(width);
    }

    public void setHeight(float height) {
        body.setHeight(height);
        ((Rectangle) blockFixture.shape).setHeight(height);
    }

    public void setSize(float width, float height) {
        setWidth(width);
        setHeight(height);
    }

    public void setBounds(Rectangle bounds) {
        setBounds(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public void setBounds(float x, float y, float width, float height) {
        setPos(x, y);
        setSize(width, height);
    }

    public void translate(float x, float y) {
        body.translate(x, y);
    }

}
