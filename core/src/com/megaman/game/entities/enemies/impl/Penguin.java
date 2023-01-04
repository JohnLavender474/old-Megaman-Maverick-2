package com.megaman.game.entities.enemies.impl;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.megaman.game.MegamanGame;
import com.megaman.game.animations.AnimationComponent;
import com.megaman.game.entities.DamageNegotiation;
import com.megaman.game.entities.Damager;
import com.megaman.game.entities.Faceable;
import com.megaman.game.entities.Facing;
import com.megaman.game.entities.enemies.Enemy;
import com.megaman.game.shapes.ShapeHandle;
import com.megaman.game.sprites.SpriteComponent;
import com.megaman.game.utils.objs.Timer;
import com.megaman.game.world.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public class Penguin extends Enemy implements Faceable {

    private enum PenguinBehavior {
        STANDING,
        JUMPING,
        SLIDING
    }

    private static final float STAND_DUR = 1f;
    private static final float SLIDING_DUR = 1f;
    private static final float GRAVITY = -.15f;
    private static final float JUMP_IMPULSE = 15f;
    private static final float VEL_X = 10f;
    private static final float VEL_CLAMP_X = 8f;

    private final Sprite sprite;
    private final Timer standTimer;
    private final Timer slideTimer;

    private PenguinBehavior behav;
    @Getter
    @Setter
    private Facing facing;

    public Penguin(MegamanGame game, BodyType bodyType) {
        super(game, bodyType);
        sprite = new Sprite();
        standTimer = new Timer(STAND_DUR);
        slideTimer = new Timer(SLIDING_DUR);
        putComponent(spriteComponent());
        putComponent(animationComponent());
    }

    public boolean isSliding() {
        return !slideTimer.isFinished() && is(BodySense.FEET_ON_GROUND);
    }

    public boolean isJumping() {
        return !slideTimer.isFinished() && !is(BodySense.FEET_ON_GROUND);
    }

    public boolean isStanding() {
        return slideTimer.isFinished();
    }

    @Override
    protected Map<Class<? extends Damager>, DamageNegotiation> defineDamageNegotiations() {
        return new HashMap<>() {{

        }};
    }

    @Override
    protected void defineBody(Body body) {
        body.gravityOn = true;
        body.velClamp.x = VEL_CLAMP_X * WorldVals.PPM;
        // TODO: body bounds changes depending on behav
        Array<ShapeHandle> h = new Array<>();

        // body fixture
        Fixture bodyFixture = new Fixture(this, FixtureType.BODY, new Rectangle());
        body.add(bodyFixture);

        // feet fixture
        Fixture feetFixture = new Fixture(this, FixtureType.FEET,
                new Rectangle().setSize(.25f * WorldVals.PPM, .2f * WorldVals.PPM));
        // TODO: feet fixture offset changes depending on behav
        h.add(new ShapeHandle(feetFixture.shape, Color.GREEN));
        body.add(feetFixture);

        // damageable fixture
        Fixture damageableFixture = new Fixture(this, FixtureType.DAMAGEABLE, new Rectangle());
        // TODO: damageable fixture offset changes depending on behav
        h.add(new ShapeHandle(damageableFixture.shape, Color.PURPLE));
        body.add(damageableFixture);

        // damager fixture
        Fixture damagerFixture = new Fixture(this, FixtureType.DAMAGER, new Rectangle());
        h.add(new ShapeHandle(damagerFixture.shape, Color.RED));
        body.add(damagerFixture);

        // pre-process
        body.preProcess = delta -> {

        };
    }

    private SpriteComponent spriteComponent() {
        return null;
    }

    private AnimationComponent animationComponent() {
        return null;
    }

}
