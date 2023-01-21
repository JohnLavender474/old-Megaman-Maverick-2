package com.megaman.game.screens.levels.camera;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.megaman.game.utils.interfaces.Resettable;
import com.megaman.game.utils.interfaces.Updatable;
import com.megaman.game.utils.objs.Timer;

public class CamShaker implements Updatable, Resettable {

    private final Timer durTimer;
    private final Timer intervalTimer;
    private final Camera cam;

    private float shakeX;
    private float shakeY;
    private Vector3 startPos;
    private boolean shakeLeft;

    public CamShaker(Camera cam) {
        this.cam = cam;
        durTimer = new Timer();
        intervalTimer = new Timer();
    }

    public void startShake(float dur, float interval, float shakeX, float shakeY) {
        this.shakeX = shakeX;
        this.shakeY = shakeY;
        shakeLeft = false;
        startPos = new Vector3(cam.position);
        durTimer.setDuration(dur).reset();
        intervalTimer.setDuration(interval).reset();
    }

    public boolean isFinished() {
        return durTimer.isFinished();
    }

    public boolean isJustFinished() {
        return durTimer.isJustFinished();
    }

    @Override
    public void update(float delta) {
        durTimer.update(delta);
        if (isJustFinished()) {
            cam.position.set(startPos);
            return;
        }
        if (isFinished()) {
            return;
        }
        intervalTimer.update(delta);
        if (intervalTimer.isFinished()) {
            if (shakeLeft) {
                cam.position.x = startPos.x - shakeX;
                cam.position.y = startPos.y - shakeY;
            } else {
                cam.position.x = startPos.x + shakeX;
                cam.position.y = startPos.y + shakeY;
            }
            intervalTimer.reset();
            shakeLeft = !shakeLeft;
        }
    }

    @Override
    public void reset() {
        durTimer.setDuration(0f).reset();
        intervalTimer.setDuration(0f).reset();
    }

}
