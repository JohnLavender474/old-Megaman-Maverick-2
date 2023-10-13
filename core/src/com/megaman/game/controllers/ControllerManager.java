package com.megaman.game.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

public class ControllerManager implements Runnable {

    private static final Map<CtrlBtn, Supplier<Integer>> defaultCtrlCodes = new EnumMap<>(CtrlBtn.class) {{
        put(CtrlBtn.DPAD_LEFT, () -> getController().getMapping().buttonDpadLeft);
        put(CtrlBtn.DPAD_RIGHT, () -> getController().getMapping().buttonDpadRight);
        put(CtrlBtn.DPAD_UP, () -> getController().getMapping().buttonDpadUp);
        put(CtrlBtn.DPAD_DOWN, () -> getController().getMapping().buttonDpadDown);
        put(CtrlBtn.A, () -> getController().getMapping().buttonA);
        put(CtrlBtn.X, () -> getController().getMapping().buttonX);
        put(CtrlBtn.START, () -> getController().getMapping().buttonStart);
        put(CtrlBtn.SELECT, () -> getController().getMapping().buttonL1);
    }};
    private static final Map<CtrlBtn, Integer> defaultKeyboardCodes = new EnumMap<>(CtrlBtn.class) {{
        put(CtrlBtn.DPAD_LEFT, Input.Keys.LEFT);
        put(CtrlBtn.DPAD_RIGHT, Input.Keys.RIGHT);
        put(CtrlBtn.DPAD_UP, Input.Keys.UP);
        put(CtrlBtn.DPAD_DOWN, Input.Keys.DOWN);
        put(CtrlBtn.A, Input.Keys.W);
        put(CtrlBtn.X, Input.Keys.D);
        put(CtrlBtn.START, Input.Keys.S);
        put(CtrlBtn.SELECT, Input.Keys.A);
    }};

    private final Map<CtrlBtn, Integer> keyboardCodes = new EnumMap<>(CtrlBtn.class);
    private final Map<CtrlBtn, Supplier<Integer>> ctrlCodes = new EnumMap<>(CtrlBtn.class);
    private final Map<CtrlBtn, ControllerBtnStat> ctrlBtnStats = new EnumMap<>(CtrlBtn.class);

    public boolean doUpdateController = true;

    public ControllerManager() {
        for (CtrlBtn ctrlBtn : CtrlBtn.values()) {
            keyboardCodes.put(ctrlBtn, defaultKeyboardCodes.get(ctrlBtn));
            ctrlCodes.put(ctrlBtn, defaultCtrlCodes.get(ctrlBtn));
            ctrlBtnStats.put(ctrlBtn, ControllerBtnStat.RELEASED);
        }
    }

    @Override
    public void run() {
        if (!doUpdateController) {
            return;
        }
        for (CtrlBtn btn : CtrlBtn.values()) {
            boolean pressed = (isControllerConnected() && isCtrlBtnPressed(btn)) || isKeyboardBtnPressed(btn);
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

    public static Controller getController() {
        if (Controllers.getControllers().isEmpty()) {
            return null;
        }
        return Controllers.getControllers().get(0);
    }

    public static boolean isControllerConnected() {
        return getController() != null;
    }

    public void setCtrlCode(CtrlBtn btn, int code) {
        ctrlCodes.put(btn, () -> code);
    }

    public void setKeyboardCode(CtrlBtn btn, int code) {
        keyboardCodes.put(btn, code);
    }

    public boolean isPressed(CtrlBtn btn) {
        return ctrlBtnStats.get(btn) == ControllerBtnStat.PRESSED || isJustPressed(btn);
    }

    public boolean isAnyPressed(CtrlBtn... btns) {
        for (CtrlBtn btn : btns) {
            if (isPressed(btn)) {
                return true;
            }
        }
        return false;
    }

    public boolean isAllPressed(CtrlBtn... btns) {
        for (CtrlBtn btn : btns) {
            if (!isPressed(btn)) {
                return false;
            }
        }
        return true;
    }

    public boolean isJustPressed(CtrlBtn btn) {
        return ctrlBtnStats.get(btn) == ControllerBtnStat.JUST_PRESSED;
    }

    public boolean isJustReleased(CtrlBtn btn) {
        return ctrlBtnStats.get(btn) == ControllerBtnStat.JUST_RELEASED;
    }

    private boolean isCtrlBtnPressed(CtrlBtn btn) {
        return getController().getButton(ctrlCodes.get(btn).get());
    }

    private boolean isKeyboardBtnPressed(CtrlBtn btn) {
        return Gdx.input.isKeyPressed(keyboardCodes.get(btn));
    }

}
