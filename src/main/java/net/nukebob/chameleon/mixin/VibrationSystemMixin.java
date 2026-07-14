package net.nukebob.chameleon.mixin;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VibrationSystem.Listener.class)
public class VibrationSystemMixin {
    @Inject(method = "handleGameEvent", at = @At("HEAD"), cancellable = true)
    private void mc_chameleon$disableVibration(ServerLevel level, Holder<GameEvent> event, GameEvent.Context context, Vec3 sourcePosition, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
