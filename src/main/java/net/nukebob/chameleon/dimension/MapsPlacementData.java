package net.nukebob.chameleon.dimension;

import com.mojang.serialization.Codec;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.nukebob.chameleon.MCChameleon;

public final class MapsPlacementData extends SavedData {
    public static final SavedDataType<MapsPlacementData> TYPE = new SavedDataType<>(
            MCChameleon.id("map_placement"),
            () -> new MapsPlacementData(false),
            Codec.BOOL.xmap(MapsPlacementData::new, MapsPlacementData::isPlaced),
            DataFixTypes.SAVED_DATA_MAP_DATA
    );

    private boolean placed;

    private MapsPlacementData(boolean placed) {
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
