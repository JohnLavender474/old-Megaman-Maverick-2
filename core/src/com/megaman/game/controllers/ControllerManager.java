package com.megaman.game.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

public class ControllerManager {

    private static final Map<ControllerBtn, Supplier<Integer>> defaultCtrlCodes = new EnumMap<>(ControllerBtn.class) {{
        put(ControllerBtn.DPAD_LEFT, () -> getController().getMapping().buttonDpadLeft);
        put(ControllerBtn.DPAD_RIGHT, () -> getController().getMapping().buttonDpadRight);
        put(ControllerBtn.DPAD_UP, () -> getController().getMapping().buttonDpadUp);
        put(ControllerBtn.DPAD_DOWN, () -> getController().getMapping().buttonDpadDown);
        put(ControllerBtn.A, () -> getController().getMapping().buttonA);
        put(ControllerBtn.X, () -> getController().getMapping().buttonX);
        put(ControllerBtn.START, () -> getController().getMapping().buttonStart);
        put(ControllerBtn.SELECT, () -> getController().getMapping().buttonB);
    }};
    private static final Map<ControllerBtn, Integer> defaultKeyboardCodes = new EnumMap<>(ControllerBtn.class) {{
        put(ControllerBtn.DPAD_LEFT, Input.Keys.LEFT);
        put(ControllerBtn.DPAD_RIGHT, Input.Keys.RIGHT);
        put(ControllerBtn.DPAD_UP, Input.Keys.UP);
        put(ControllerBtn.DPAD_DOWN, Input.Keys.DOWN);
        put(ControllerBtn.A, Input.Keys.W);
        put(ControllerBtn.X, Input.Keys.D);
        put(ControllerBtn.START, Input.Keys.S);
        put(ControllerBtn.SELECT, Input.Keys.A);
    }};

    private final Map<ControllerBtn, Integer> keyboardCodes = new EnumMap<>(ControllerBtn.class);
    private final Map<ControllerBtn, Supplier<Integer>> ctrlCodes = new EnumMap<>(ControllerBtn.class);
    private final Map<ControllerBtn, ControllerBtnStat> ctrlBtnStats = new EnumMap<>(ControllerBtn.class);

    public boolean doUpdateController = true;

    public ControllerManager() {
        for (ControllerBtn ctrlBtn : ControllerBtn.values()) {
            keyboardCodes.put(ctrlBtn, defaultKeyboardCodes.get(ctrlBtn));
            ctrlCodes.put(ctrlBtn, defaultCtrlCodes.get(ctrlBtn));
            ctrlBtnStats.put(ctrlBtn, ControllerBtnStat.RELEASED);
        }
    }

    public static Controller getController() {
        if (Controllers.getControllers().isEmpty()) {
            return null;
        }
        return Controllers.getControllers().get(0);
    }

    public static boolean isControllerConnected() {
        return getController() != null;
    }

    public boolean isPressed(ControllerBtn btn) {
        return ctrlBtnStats.get(btn) == ControllerBtnStat.PRESSED || isJustPressed(btn);
    }

    public boolean isJustPressed(ControllerBtn btn) {
        return ctrlBtnStats.get(btn) == ControllerBtnStat.JUST_PRESSED;
    }

    public boolean isJustReleased(ControllerBtn btn) {
        return ctrlBtnStats.get(btn) == ControllerBtnStat.JUST_RELEASED;
    }

    private boolean isCtrlBtnPressed(ControllerBtn btn) {
        return getController().getButton(ctrlCodes.get(btn).get());
    }

    private boolean isKeyboardBtnPressed(ControllerBtn btn) {
        return Gdx.input.isKeyPressed(keyboardCodes.get(btn));
    }

    public void update() {
        if (!doUpdateController) {
            return;
        }
        for (ControllerBtn btn : ControllerBtn.values()) {
            boolean pressed = (isControllerConnected() && isCtrlBtnPressed(btn)) || isKeyboardBtnPressed(btn);
            // boolean pressed = isKeyboardBtnPressed(btn);
            ControllerBtnStat stat = ctrlBtnStats.get(btn);
            ControllerBtnStat newStat;
            if (pressed) {
                if (stat == ControllerBtnStat.RELEASED || stat == ControllerBtnStat.JUST_RELEASED) {
                    newStat = ControllerBtnStat.JUST_PRESSED;
                } else {
                    newStat = ControllerBtnStat.PRESSED;
                }
            } else if (stat == ControllerBtnStat.RELEASED || stat == ControllerBtnStat.JUST_RELEASED) {
                newStat = ControllerBtnStat.RELEASED;
            } else {
                newStat = ControllerBtnStat.JUST_RELEASED;
            }
            ctrlBtnStats.replace(btn, newStat);
        }
    }

}
