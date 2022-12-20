package com.megaman.game.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

public class ControllerManager {

    private static final Map<ControllerBtn, Supplier<Integer>> defaultCtrlCodes = new EnumMap<>(ControllerBtn.class);
    private static final Map<ControllerBtn, Integer> defaultKeyboardCodes = new EnumMap<>(ControllerBtn.class);

    static {
        defaultCtrlCodes.put(ControllerBtn.DPAD_LEFT, () -> getController().getMapping().buttonDpadLeft);
        defaultCtrlCodes.put(ControllerBtn.DPAD_RIGHT, () -> getController().getMapping().buttonDpadRight);
        defaultCtrlCodes.put(ControllerBtn.DPAD_UP, () -> getController().getMapping().buttonDpadUp);
        defaultCtrlCodes.put(ControllerBtn.DPAD_DOWN, () -> getController().getMapping().buttonDpadDown);
        defaultCtrlCodes.put(ControllerBtn.A, () -> getController().getMapping().buttonA);
        defaultCtrlCodes.put(ControllerBtn.X, () -> getController().getMapping().buttonX);
        defaultCtrlCodes.put(ControllerBtn.START, () -> getController().getMapping().buttonStart);
    }

    static {
        defaultKeyboardCodes.put(ControllerBtn.DPAD_LEFT, Input.Keys.LEFT);
        defaultKeyboardCodes.put(ControllerBtn.DPAD_RIGHT, Input.Keys.RIGHT);
        defaultKeyboardCodes.put(ControllerBtn.DPAD_UP, Input.Keys.UP);
        defaultKeyboardCodes.put(ControllerBtn.DPAD_DOWN, Input.Keys.DOWN);
        defaultKeyboardCodes.put(ControllerBtn.A, Input.Keys.W);
        defaultKeyboardCodes.put(ControllerBtn.X, Input.Keys.D);
    }

    private final Map<ControllerBtn, Integer> keyboardCodes = new EnumMap<>(ControllerBtn.class);
    private final Map<ControllerBtn, Supplier<Integer>> ctrlCodes = new EnumMap<>(ControllerBtn.class);
    private final ControllerBtnStat[] ctrlBtnStats = new ControllerBtnStat[ControllerBtn.values().length];

    public boolean doUpdateController;

    public ControllerManager() {
        for (ControllerBtn ctrlBtn : ControllerBtn.values()) {
            keyboardCodes.put(ctrlBtn, defaultKeyboardCodes.get(ctrlBtn));
            ctrlCodes.put(ctrlBtn, defaultCtrlCodes.get(ctrlBtn));
            ctrlBtnStats[ctrlBtn.ordinal()] = ControllerBtnStat.RELEASED;
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
        return ctrlBtnStats[btn.ordinal()] == ControllerBtnStat.PRESSED || isJustPressed(btn);
    }

    public boolean isJustPressed(ControllerBtn btn) {
        return ctrlBtnStats[btn.ordinal()] == ControllerBtnStat.JUST_PRESSED;
    }

    public boolean isJustReleased(ControllerBtn btn) {
        return ctrlBtnStats[btn.ordinal()] == ControllerBtnStat.JUST_RELEASED;
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
            if (pressed) {
                if (ctrlBtnStats[btn.ordinal()] == ControllerBtnStat.JUST_PRESSED) {
                    ctrlBtnStats[btn.ordinal()] = ControllerBtnStat.PRESSED;
                } else {
                    ctrlBtnStats[btn.ordinal()] = ControllerBtnStat.JUST_PRESSED;
                }
            } else {
                if (ctrlBtnStats[btn.ordinal()] == ControllerBtnStat.JUST_RELEASED) {
                    ctrlBtnStats[btn.ordinal()] = ControllerBtnStat.RELEASED;
                } else {
                    ctrlBtnStats[btn.ordinal()] = ControllerBtnStat.JUST_RELEASED;
                }
            }
        }
    }

}
