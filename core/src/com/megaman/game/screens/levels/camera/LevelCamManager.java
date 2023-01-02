package com.megaman.game.screens.levels.camera;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.utils.UtilMethods;
import com.megaman.game.utils.enums.Direction;
import com.megaman.game.utils.enums.ProcessState;
import com.megaman.game.utils.interfaces.Positional;
import com.megaman.game.utils.interfaces.Resettable;
import com.megaman.game.utils.interfaces.Updatable;
import com.megaman.game.utils.objs.Timer;
import com.megaman.game.world.WorldVals;
import lombok.Getter;
import lombok.Setter;

import static com.megaman.game.utils.UtilMethods.interpolate;
import static java.lang.Math.min;

public class LevelCamManager implements Updatable, Resettable {

    public static final float TRANS_DUR = 1f;
    public static final float DIST_ON_TRANS = 1.5f;

    private final Camera cam;
    private final Timer transTimer;
    private final Vector2 transStartPos;
    private final Vector2 transTargetPos;
    private final Vector2 focusableStartPos;
    private final Vector2 focusableTargetPos;

    private Positional focusable;
    private ObjectMap<String, RectangleMapObject> gameRooms;

    @Getter
    private RectangleMapObject priorGameRoom;
    @Getter
    private RectangleMapObject currGameRoom;

    @Setter
    private Runnable runOnBeginTrans;
    @Setter
    private Updatable updateOnTrans;
    @Setter
    private Runnable runOnEndTrans;

    @Getter
    private ProcessState transState;
    @Getter
    private Direction transDirection;

    private boolean reset;

    public LevelCamManager(Camera cam) {
        this.cam = cam;
        transTimer = new Timer(TRANS_DUR);
        transStartPos = new Vector2();
        transTargetPos = new Vector2();
        focusableStartPos = new Vector2();
        focusableTargetPos = new Vector2();
    }

    @Override
    public void update(float delta) {
        if (reset) {
            reset = false;
            currGameRoom = priorGameRoom = null;
            transDirection = null;
            transState = null;
            setCamToFocusable();
            currGameRoom = nextGameRoom();
        } else if (!isTransitioning()) {
            onNoTrans();
        } else {
            onTrans(delta);
        }
    }

    @Override
    public void reset() {
        reset = true;
    }

    public void set(Array<RectangleMapObject> gameRooms, Positional focusable) {
        this.gameRooms = new ObjectMap<>();
        for (RectangleMapObject r : gameRooms) {
            this.gameRooms.put(r.getName(), r);
        }
        setFocusable(focusable);
    }

    public void setFocusable(Positional focusable) {
        this.focusable = focusable;
        Vector2 pos = focusable.getPosition();
        cam.position.x = pos.x;
        cam.position.y = pos.y;
        reset = true;
    }

    public boolean isTransitioning() {
        return !isTransState(null);
    }

    public boolean isTransState(ProcessState state) {
        return transState == state;
    }

    public void transToRoom(String name) {
        if (currGameRoom == null) {
            throw new IllegalStateException("Cannot trans to room " + name + " if there is no current game room");
        }
        RectangleMapObject nextGameRoom = gameRooms.get(name);
        transDirection = UtilMethods.getSingleMostDirectionFromStartToTarget(
                currGameRoom.getRectangle(), nextGameRoom.getRectangle());
        setTransVals(nextGameRoom.getRectangle());
        priorGameRoom = currGameRoom;
        currGameRoom = nextGameRoom;
    }

    public float getTransTimerRatio() {
        return transTimer.getRatio();
    }

    public Vector2 getTransInterpolation() {
        Vector2 startCopy = focusableStartPos.cpy();
        Vector2 targetCopy = focusableTargetPos.cpy();
        return UtilMethods.interpolate(startCopy, targetCopy, getTransTimerRatio());
    }

    private void setTransVals(Rectangle next) {
        transState = ProcessState.BEGIN;
        transStartPos.set(UtilMethods.toVec2(cam.position));
        transTargetPos.set(transStartPos);
        focusableStartPos.set(focusable.getPosition());
        focusableTargetPos.set(focusableStartPos);
        switch (transDirection) {
            case LEFT -> {
                transTargetPos.x = (next.x + next.width) - min(next.width / 2.0f, cam.viewportWidth / 2.0f);
                focusableTargetPos.x = (next.x + next.width) - DIST_ON_TRANS * WorldVals.PPM;
            }
            case RIGHT -> {
                transTargetPos.x = next.x + min(next.width / 2.0f, cam.viewportWidth / 2.0f);
                focusableTargetPos.x = next.x + DIST_ON_TRANS * WorldVals.PPM;
            }
            case UP -> {
                transTargetPos.y = next.y + min(next.height / 2.0f, cam.viewportHeight / 2.0f);
                focusableTargetPos.y = next.y + DIST_ON_TRANS * WorldVals.PPM;
            }
            case DOWN -> {
                transTargetPos.y = (next.y + next.height) - min(next.height / 2.0f, cam.viewportHeight / 2.0f);
                focusableTargetPos.y = (next.y + next.height) - DIST_ON_TRANS * WorldVals.PPM;
            }
        }
    }

    private void onNoTrans() {
        if (currGameRoom == null) {
            RectangleMapObject nextGameRoom = nextGameRoom();
            if (nextGameRoom != null) {
                priorGameRoom = currGameRoom;
                currGameRoom = nextGameRoom;
            }
            cam.position.x = UtilMethods.roundedFloat(focusable.getPosition().x, 3);
            return;
        }
        Rectangle currRoomBounds = currGameRoom.getRectangle();
        if (currRoomBounds.contains(focusable.getPosition())) {
            setCamToFocusable();
            if (cam.position.y > (currRoomBounds.y + currRoomBounds.height) - cam.viewportHeight / 2.0f) {
                cam.position.y = (currRoomBounds.y + currRoomBounds.height) - cam.viewportHeight / 2.0f;
            }
            if (cam.position.y < currRoomBounds.y + cam.viewportHeight / 2.0f) {
                cam.position.y = currRoomBounds.y + cam.viewportHeight / 2.0f;
            }
            if (cam.position.x > (currRoomBounds.x + currRoomBounds.width) - cam.viewportWidth / 2.0f) {
                cam.position.x = (currRoomBounds.x + currRoomBounds.width) - cam.viewportWidth / 2.0f;
            }
            if (cam.position.x < currRoomBounds.x + cam.viewportWidth / 2.0f) {
                cam.position.x = currRoomBounds.x + cam.viewportWidth / 2.0f;
            }
            return;
        }
        RectangleMapObject nextGameRoom = nextGameRoom();
        if (nextGameRoom == null) {
            cam.position.x = UtilMethods.roundedFloat(focusable.getPosition().x, 3);
            return;
        }
        Rectangle overlap = new Rectangle();
        float width = 5f * WorldVals.PPM;
        float height = 5f * WorldVals.PPM;
        Rectangle boundingBox = new Rectangle()
                .setSize(width, height)
                .setCenter(focusable.getPosition());
        transDirection = UtilMethods.getOverlapPushDirection(boundingBox, currGameRoom.getRectangle(), overlap);
        priorGameRoom = currGameRoom;
        currGameRoom = nextGameRoom;
        if (transDirection == null) {
            return;
        }
        setTransVals(nextGameRoom.getRectangle());
    }

    private void onTrans(float delta) {
        switch (transState) {
            case END:
                transDirection = null;
                transState = null;
                transTimer.reset();
                transStartPos.setZero();
                transTargetPos.setZero();
                if (runOnEndTrans != null) {
                    runOnEndTrans.run();
                }
                break;
            case BEGIN:
                transState = ProcessState.CONTINUE;
                if (runOnBeginTrans != null) {
                    runOnBeginTrans.run();
                }
            case CONTINUE:
                transTimer.update(delta);
                Vector2 pos = interpolate(transStartPos, transTargetPos, getTransTimerRatio());
                cam.position.x = pos.x;
                cam.position.y = pos.y;
                transState = transTimer.isFinished() ? ProcessState.END : ProcessState.CONTINUE;
                if (updateOnTrans != null) {
                    updateOnTrans.update(delta);
                }
        }
    }

    private RectangleMapObject nextGameRoom() {
        if (focusable == null || focusable.getPosition() == null) {
            return null;
        }
        RectangleMapObject nextGameRoom = null;
        for (RectangleMapObject room : gameRooms.values()) {
            if (room.getRectangle().contains(focusable.getPosition())) {
                nextGameRoom = room;
                break;
            }
        }
        return nextGameRoom;
    }

    private void setCamToFocusable() {
        Vector2 pos = focusable.getPosition();
        // cam.position.x = UtilMethods.roundedFloat(pos.x, 3);
        cam.position.x = pos.x;
        cam.position.y = pos.y;
    }

}
