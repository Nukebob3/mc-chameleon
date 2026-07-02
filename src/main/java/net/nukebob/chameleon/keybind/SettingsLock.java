package net.nukebob.chameleon.keybind;

import com.google.common.collect.ImmutableSet;
import net.minecraft.client.Options;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.world.entity.player.PlayerModelPart;

public class SettingsLock {
    public static final ImmutableSet<String> disabledKeybinds = ImmutableSet.of(
            "drop",//shift as well but not key so come back pls
            "inventory",
            "pickItem",
            "toggleSpectatorShaderEffects",
            "togglePerspective",
            "spectatorOutlines",
            "spectatorHotbar",
            "swapOffhand",
            "saveToolbarActivator",
            "loadToolbarActivator",
            "advancements",
            "quickActions",
            "debug.overlay",
            "debug.modifier",
            "debug.showHitboxes",
            "hotbar.1",
            "hotbar.2",
            "hotbar.3",
            "hotbar.4",
            "hotbar.5",
            "hotbar.6",
            "hotbar.7",
            "hotbar.8",
            "hotbar.9",
            "debug.showAdvancedTooltips",
            "debugOptions"
    );
    public static final ImmutableSet<String> lockedSettings = ImmutableSet.of(
            "entityShadows",
            "gamma",
            "particles",
            "modelPart.cape"
    );

    public static void applyLockedSettings(Options options) {
        options.entityShadows().set(false);
        options.gamma().set(1.0);
        options.particles().set(ParticleStatus.MINIMAL);
        options.setModelPart(PlayerModelPart.CAPE, false);
    }
}
