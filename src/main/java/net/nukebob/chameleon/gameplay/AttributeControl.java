package net.nukebob.chameleon.gameplay;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class AttributeControl {
    public static void setCommonAttributes(ServerPlayer player) {
        player.getAttributes().getInstance(Attributes.NAME_TAG_DISTANCE).setBaseValue(128);
        player.getAttributes().getInstance(Attributes.JUMP_STRENGTH).setBaseValue(0.25);
        player.getAttributes().getInstance(Attributes.GRAVITY).setBaseValue(0.06);
        player.getAttributes().getInstance(Attributes.AIR_DRAG_MODIFIER).setBaseValue(3.0);
        player.getAttributes().getInstance(Attributes.ATTACK_DAMAGE).setBaseValue(0);
    }
}
