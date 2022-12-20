package com.megaman.game.screens.menus;

import com.megaman.game.utils.enums.Direction;

public interface MenuButton {

    boolean onSelect(float delta);

    void onNavigate(Direction direction, float delta);

}
