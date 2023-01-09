package com.megaman.game.world;

import com.megaman.game.MegamanGame;
import com.megaman.game.behaviors.BehaviorType;
import com.megaman.game.controllers.CtrlBtn;
import com.megaman.game.entities.megaman.Megaman;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SpecialCollisionHandlerImpl implements SpecialCollisionHandler {

    private final MegamanGame game;

    @Override
    public boolean handleSpecial(Body dynamicBody, Body staticBody) {
        Megaman megaman = game.getMegaman();
        if (staticBody.labels.contains(BodyLabel.PRESS_UP_FALL_THRU) && dynamicBody == megaman.body &&
                !megaman.is(BehaviorType.CLIMBING) && game.getCtrlMan().isJustPressed(CtrlBtn.DPAD_UP)) {
            dynamicBody.setMaxY(staticBody.getMaxY());
            return true;
        }
        if (staticBody.labels.contains(BodyLabel.COLLIDE_DOWN_ONLY)) {
            if (dynamicBody == megaman.body && megaman.is(BehaviorType.CLIMBING)) {
                return true;
            }
            if (dynamicBody.is(BodySense.FEET_ON_GROUND)) {
                dynamicBody.setY(staticBody.getMaxY());
                dynamicBody.resistance.x += staticBody.friction.x;
                return true;
            }
            return dynamicBody.getY() < staticBody.getMaxY();
        } else if (staticBody.labels.contains(BodyLabel.COLLIDE_UP_ONLY)) {
            if (dynamicBody == megaman.body && megaman.is(BehaviorType.CLIMBING)) {
                return true;
            }
            if (dynamicBody.is(BodySense.FEET_ON_GROUND)) {
                dynamicBody.setMaxY(staticBody.getY());
                dynamicBody.resistance.x += staticBody.friction.x;
                return true;
            }
            return dynamicBody.getMaxY() > staticBody.getY();
        }
        return false;
    }

}
