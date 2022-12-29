package com.megaman.game.world;

import com.badlogic.gdx.math.Rectangle;
import com.megaman.game.MegamanGame;
import com.megaman.game.controllers.CtrlBtn;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SpecialCollisionHandlerImpl implements SpecialCollisionHandler {

    private final MegamanGame game;

    @Override
    public boolean handleSpecial(Body dynamicBody, Body staticBody, Rectangle overlap) {
        if (staticBody.labels.contains(BodyLabel.COLLIDE_DOWN_ONLY) &&
                (dynamicBody.velocity.y >= 0f || overlap.width < overlap.height)) {
            return true;
        } else if (staticBody.labels.contains(BodyLabel.PRESS_UP_FALL_THRU) &&
                dynamicBody.labels.contains(BodyLabel.PLAYER_BODY) &&
                game.getCtrlMan().isJustPressed(CtrlBtn.DPAD_UP)) {
            dynamicBody.setMaxY(staticBody.getMaxY());
            return true;
        }
        return false;
    }

}
