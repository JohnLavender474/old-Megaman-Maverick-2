package com.megaman.game.controllers;

import com.megaman.game.Component;

import java.util.EnumMap;
import java.util.Map;

public class ControllerComponent implements Component {

    public final Map<CtrlBtn, ControllerActuator> ctrlAdapters = new EnumMap<>(CtrlBtn.class);

}
