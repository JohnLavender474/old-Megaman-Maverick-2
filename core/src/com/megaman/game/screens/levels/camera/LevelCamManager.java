package com.megaman.game.screens.levels.camera;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.utils.UtilMethods;
import com.megaman.game.utils.enums.Direction;
import com.megaman.game.utils.enums.ProcessState;
import com.megaman.game.utils.interfaces.Positional;
import com.megaman.game.utils.interfaces.Updatable;
import com.megaman.game.utils.objs.Timer;
import com.megaman.game.world.WorldConstVals;
import lombok.Getter;

import static com.megaman.game.utils.UtilMethods.interpolate;
import static java.lang.Math.min;

public class LevelCamManager implements Updatable {

    public static final float CAM_TRANS_DUR = 1f;
    public static final float DIST_ON_TRANS = 3f;

    public Camera cam;

    private final Timer transTimer = new Timer(CAM_TRANS_DUR);

    private final Vector2 transStartPos = new Vector2();
    private final Vector2 transTargetPos = new Vector2();
    private final Vector2 focusableStartPos = new Vector2();
    private final Vector2 focusableTargetPos = new Vector2();

    private Positional focusable;
    @Getter
    private RectangleMapObject currGameRoom;
    private ObjectMap<String, RectangleMapObject> gameRooms;
    @Getter
    private ProcessState transState;
    @Getter
    private Direction transDirection;
    private boolean reset;
    @Getter
    private boolean updating;

    public LevelCamManager(Camera cam) {
        this.cam = cam;
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
        Vector2 pos = focusable.getPos();
        cam.position.x = pos.x;
        cam.position.y = pos.y;
        reset = true;
    }

    @Override
    public void update(float delta) {
        updating = true;
        if (reset) {
            setCamToFocusable(delta);
            currGameRoom = nextGameRoom();
            reset = false;
        } else if (transState == null) {
            onNullTrans(delta);
        } else {
            onTrans(delta);
        }
        updating = false;
    }

    public void transToRoom(String name) {
        if (currGameRoom == null) {
            throw new IllegalStateException("Cannot trans if there is no current game room");
        }
        RectangleMapObject nextGameRoom = gameRooms.get(name);
        transDirection = UtilMethods.getSingleMostDirectionFromStartToTarget(
                currGameRoom.getRectangle(), nextGameRoom.getRectangle());
        setTransVals(nextGameRoom.getRectangle());
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

    private void setTransVals(Rectangle nextGameRoom) {
        transState = ProcessState.BEGIN;
        transStartPos.set(UtilMethods.toVec2(cam.position));
        transTargetPos.set(transStartPos);
        focusableStartPos.set(focusable.getPos());
        focusableTargetPos.set(focusableStartPos);
        switch (transDirection) {
            case DIR_LEFT -> {
                transTargetPos.x = (nextGameRoom.x + nextGameRoom.width) - min(nextGameRoom.width / 2.0f,
                        cam.viewportWidth / 2.0f);
                focusableTargetPos.x = (nextGameRoom.x + nextGameRoom.width) - DIST_ON_TRANS;
            }
            case DIR_RIGHT -> {
                transTargetPos.x = nextGameRoom.x + min(nextGameRoom.width / 2.0f,
                        cam.viewportWidth / 2.0f);
                focusableTargetPos.x = nextGameRoom.x + DIST_ON_TRANS;
            }
            case DIR_UP -> {
                transTargetPos.y = nextGameRoom.y + min(nextGameRoom.height / 2.0f,
                        cam.viewportHeight / 2.0f);
                focusableTargetPos.y = nextGameRoom.y + DIST_ON_TRANS;
            }
            case DIR_DOWN -> {
                transTargetPos.y = (nextGameRoom.y + nextGameRoom.height) - min(nextGameRoom.height / 2.0f,
                        cam.viewportHeight / 2.0f);
                focusableTargetPos.y = (nextGameRoom.y + nextGameRoom.height) - DIST_ON_TRANS;
            }
        }
    }

    private void onNullTrans(float delta) {
        /*
        case 1: if current game room is null, try to find next game room and assign it to current game room,
        wait until next resize cycle to attempt another action

        case 2: if current game room contains focusable, then set gameCam center to current focus and
        correct bounds if necessary

        case 3: if current game room is not null and doesn't contain focusable, then setBounds next game room,
        and if next game room is a neighbour, then init transition process, otherwise jump directly to
        focusable on next resize cycle
        */
        // case 1
        if (currGameRoom == null) {
            currGameRoom = nextGameRoom();
            return;
        }
        Rectangle currRoomBounds = currGameRoom.getRectangle();
        // case 2
        if (currRoomBounds.contains(focusable.getPos())) {
            setCamToFocusable(delta);
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
        // case 3
        RectangleMapObject nextGameRoom = nextGameRoom();
        if (nextGameRoom == null) {
            return;
        }
        // generic 5 * PPM by 5 * PPM square is used to determine push direction
        Rectangle overlap = new Rectangle();
        float width = 5f * WorldConstVals.PPM;
        float height = 5f * WorldConstVals.PPM;
        Rectangle boundingBox = new Rectangle(0f, 0f, width, height).setCenter(focusable.getPos());
        transDirection = UtilMethods.getOverlapPushDirection(boundingBox, currGameRoom.getRectangle(), overlap);
        // go ahead and set current game room to next room, which needs to be done even if
        // transition direction is null
        currGameRoom = nextGameRoom;
        if (transDirection == null) {
            return;
        }
        // set trans vals
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
                break;
            case BEGIN:
                transState = ProcessState.CONTINUE;
            case CONTINUE:
                transTimer.update(delta);
                Vector2 pos = interpolate(transStartPos, transTargetPos, getTransTimerRatio());
                cam.position.x = pos.x;
                cam.position.y = pos.y;
                transState = transTimer.isFinished() ? ProcessState.END : ProcessState.CONTINUE;
        }
    }

    private RectangleMapObject nextGameRoom() {
        if (focusable == null || focusable.getPos() == null) {
            return null;
        }
        RectangleMapObject nextGameRoom = null;
        for (RectangleMapObject room : gameRooms.values()) {
            if (room.getRectangle().contains(focusable.getPos())) {
                nextGameRoom = room;
                break;
            }
        }
        return nextGameRoom;
    }

    private void setCamToFocusable(float delta) {
        Vector2 pos = interpolate(UtilMethods.toVec2(cam.position), focusable.getPos(), delta * 10f);
        cam.position.x = pos.x;
        cam.position.y = pos.y;
    }

}
