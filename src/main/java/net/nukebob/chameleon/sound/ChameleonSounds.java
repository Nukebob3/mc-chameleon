package net.nukebob.chameleon.sound;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.nukebob.chameleon.MCChameleon;

public class ChameleonSounds {
    public static final SoundEvent WHISTLE = registerSound("whistle");
    public static final SoundEvent FART = registerSound("fart");
    public static final SoundEvent BELL_START = registerSound("bell_start");
    public static final SoundEvent BELL_END = registerSound("bell_end");

    public static SoundEvent registerSound(String id) {
        Identifier identifier = MCChameleon.id(id);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, identifier, SoundEvent.createVariableRangeEvent(identifier));
    }

    public static void initialize() {}
}
