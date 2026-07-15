package net.nukebob.chameleon.dimension;

import com.mojang.serialization.Codec;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.nukebob.chameleon.MCChameleon;

public final class LobbyPlacementData extends SavedData {
    public static final SavedDataType<LobbyPlacementData> TYPE = new SavedDataType<>(
            MCChameleon.id("lobby_placement"),
            () -> new LobbyPlacementData(false),
            Codec.BOOL.xmap(LobbyPlacementData::new, LobbyPlacementData::isPlaced),
            DataFixTypes.SAVED_DATA_MAP_DATA
    );

    private boolean placed;

    private LobbyPlacementData(boolean placed) {
        this.placed = placed;
    }

    public boolean isPlaced() {
        return placed;
    }

    public void markPlaced() {
        placed = true;
        setDirty();
    }
}
