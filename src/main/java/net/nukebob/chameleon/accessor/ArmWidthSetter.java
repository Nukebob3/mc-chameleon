package net.nukebob.chameleon.accessor;

import java.util.ArrayList;
import java.util.List;

public interface ArmWidthSetter {
    List<Object> CANVAS_SKINS_LIST = new ArrayList<>();

    default boolean mc_chameleon$isCanvas() {
        return CANVAS_SKINS_LIST.contains(this);
    }
}
