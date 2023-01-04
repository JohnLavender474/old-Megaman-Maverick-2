package com.megaman.game.world;

import com.megaman.game.MegamanGame;
import com.megaman.game.controllers.CtrlBtn;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SpecialCollisionHandlerImpl implements SpecialCollisionHandler {

    private final MegamanGame game;

    @Override
    public boolean handleSpecial(Body dynamicBody, Body staticBody) {
        if (staticBody.labels.contains(BodyLabel.PRESS_UP_FALL_THRU) &&
                dynamicBody.labels.contains(BodyLabel.PLAYER_BODY) &&
                game.getCtrlMan().isJustPressed(CtrlBtn.DPAD_UP)) {
            dynamicBody.setMaxY(staticBody.getY());
            return true;
        }
        if (staticBody.labels.contains(BodyLabel.COLLIDE_DOWN_ONLY)) {
            if (dynamicBody.is(BodySense.FEET_ON_GROUND)) {
                dynamicBody.setY(staticBody.getMaxY());
                dynamicBody.resistance.x += staticBody.friction.x;
                return true;
            }
            return dynamicBody.getY() < staticBody.getMaxY();
        }
        return false;
    }

}
