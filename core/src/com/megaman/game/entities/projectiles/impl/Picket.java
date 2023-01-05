package com.megaman.game.entities.projectiles.impl;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.ConstKeys;
import com.megaman.game.MegamanGame;
import com.megaman.game.animations.Animation;
import com.megaman.game.animations.AnimationComponent;
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.entities.projectiles.Projectile;
import com.megaman.game.sprites.SpriteComponent;
import com.megaman.game.sprites.SpriteHandle;
import com.megaman.game.utils.enums.Position;
import com.megaman.game.world.Fixture;
import com.megaman.game.world.FixtureType;
import com.megaman.game.world.WorldVals;

public class Picket extends Projectile {

    private static final float GRAVITY = -.15f;

    private static TextureRegion picketReg;

    public Picket(MegamanGame game) {
        super(game);
        if (picketReg == null) {
            picketReg = game.getAssMan().getTextureRegion(TextureAsset.PROJECTILES_1, "Picket");
        }
        defineBody();
        putComponent(spriteComponent());
        putComponent(animationComponent());
    }

    @Override
    public void init(Vector2 spawn, ObjectMap<String, Object> data) {
        super.init(spawn, data);
        float impulseX = (float) data.get(ConstKeys.X);
        float impulseY = (float) data.get(ConstKeys.Y);
        body.velocity.set(impulseX, impulseY);
    }

    @Override
    public void hitBlock(Fixture blockFixture) {
        // TODO: certain blocks breakable with picket
    }

    private void defineBody() {
        body.bounds.setSize(.85f * WorldVals.PPM);
        body.gravityOn = true;
        body.gravity.y = GRAVITY * WorldVals.PPM;

        // body fixture
        Fixture bodyFixture = new Fixture(this, FixtureType.BODY,
                new Rectangle().setSize(.2f * WorldVals.PPM));
        body.add(bodyFixture);

        // projectile fixture
        Fixture projectileFixture = new Fixture(this, FixtureType.PROJECTILE,
                new Rectangle().setSize(.2f * WorldVals.PPM));
        body.add(projectileFixture);

        // damager fixture
        Fixture damagerFixture = new Fixture(this, FixtureType.DAMAGER,
                new Rectangle().setSize(.2f * WorldVals.PPM));
        body.add(damagerFixture);
    }

    private SpriteComponent spriteComponent() {
        sprite.setSize(1.5f * WorldVals.PPM, 1.5f * WorldVals.PPM);
        SpriteHandle h = new SpriteHandle(sprite, 5);
        h.updatable = delta -> h.setPosition(body.bounds, Position.CENTER);
        return new SpriteComponent(h);
    }

    private AnimationComponent animationComponent() {
        return new AnimationComponent(sprite, new Animation(picketReg, 4, .1f));
    }

}
