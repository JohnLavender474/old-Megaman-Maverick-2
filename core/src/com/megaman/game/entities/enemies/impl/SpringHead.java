package com.megaman.game.entities.enemies.impl;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.megaman.game.ConstKeys;
import com.megaman.game.MegamanGame;
import com.megaman.game.ViewVals;
import com.megaman.game.entities.DamageNegotiation;
import com.megaman.game.entities.Damager;
import com.megaman.game.entities.Faceable;
import com.megaman.game.entities.Facing;
import com.megaman.game.entities.enemies.Enemy;
import com.megaman.game.updatables.UpdatableComponent;
import com.megaman.game.utils.objs.Timer;
import com.megaman.game.world.*;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.function.Function;

public class SpringHead extends Enemy implements Faceable {

    private static final float DAMAGE_DURATION = .5f;
    private static final float BOUNCE_DURATION = 1.5f;
    private static final float TURN_DELAY = .25f;
    private static final float X_BOUNCE = .125f;
    private static final float Y_BOUNCE = .5f;
    private static final float SPEED_NORMAL = 1.5f;
    private static final float SPEED_SUPER = 5f;

    private final Sprite sprite;
    private final Timer turnTimer;
    private final Timer bounceTimer;
    private final Rectangle speedUpScanner;

    @Getter
    @Setter
    private Facing facing;

    public SpringHead(MegamanGame game) {
        super(game, BodyType.DYNAMIC);
        sprite = new Sprite();
        turnTimer = new Timer(TURN_DELAY);
        bounceTimer = new Timer(BOUNCE_DURATION, true);
        speedUpScanner = new Rectangle().setSize(ViewVals.VIEW_WIDTH * WorldVals.PPM, WorldVals.PPM / 4f);
    }

    @Override
    protected Map<Class<? extends Damager>, DamageNegotiation> defineDamageNegotiations() {
        return new HashMap<>() {{

        }};
    }

    @Override
    protected void defineBody(Body body) {
        body.bounds.setSize(WorldVals.PPM / 4f, WorldVals.PPM / 4f);
        Circle c1 = new Circle();
        c1.radius = .35f * WorldVals.PPM;
        Fixture damagerFixture = new Fixture(this, FixtureType.DAMAGER, new Circle(c1));
        body.fixtures.add(damagerFixture);
        Fixture damageableFixture = new Fixture(this, FixtureType.DAMAGEABLE, new Circle(c1));
        body.fixtures.add(damageableFixture);
        Fixture shieldFixture = new Fixture(this, FixtureType.SHIELD,
                new Rectangle().setSize(.85f * WorldVals.PPM, .6f * WorldVals.PPM));
        shieldFixture.offset.y = .1f * WorldVals.PPM;
        shieldFixture.putUserData(ConstKeys.REFLECT, ConstKeys.UP);
        body.fixtures.add(shieldFixture);
        Circle c2 = new Circle();
        c2.radius = WorldVals.PPM / 4f;
        Fixture bounceFixture = new Fixture(this, FixtureType.BOUNCER, c2);
        bounceFixture.putUserData(ConstKeys.FUNCTION, (Function<Fixture, Vector2>) f -> {

            // TODO: return force and reset bouncer timer

            return null;
        });
    }

    public boolean isBouncing() {
        return !bounceTimer.isFinished();
    }

    private boolean isMegamanRight() {
        return game.getMegaman().body.isRightOf(body);
    }

    private boolean megamanOverlapSpeedUpScanner() {
        return game.getMegaman().body.overlaps(speedUpScanner);
    }

    @Override
    protected void defineUpdateComponent(UpdatableComponent c) {
        super.defineUpdateComponent(c);
        c.add(delta -> {
            bounceTimer.update(delta);
            if (isBouncing()) {
                body.velocity.x = 0f;
                return;
            }
            turnTimer.update(delta);
            if (turnTimer.isJustFinished()) {
                setFacing(isMegamanRight() ? Facing.RIGHT : Facing.LEFT);
            }
            if (turnTimer.isFinished() &&
                    ((isMegamanRight() && is(Facing.LEFT)) || (!isMegamanRight() && is(Facing.RIGHT)))) {
                turnTimer.reset();
            }
            if ((is(Facing.LEFT) && !is(BodySense.TOUCHING_BLOCK_LEFT)) ||
                    (is(Facing.RIGHT) && !is(BodySense.TOUCHING_BLOCK_RIGHT))) {
                body.velocity.x = 0f;
            } else {
                float vel = (megamanOverlapSpeedUpScanner() ? SPEED_SUPER : SPEED_NORMAL) * WorldVals.PPM;
                body.velocity.x = is(Facing.LEFT) ? -vel : vel;
            }
        });
    }

}
