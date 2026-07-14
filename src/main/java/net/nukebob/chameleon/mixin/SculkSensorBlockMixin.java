package net.nukebob.chameleon.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SculkSensorBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SculkSensorBlock.class)
public class SculkSensorBlockMixin {
    @Inject(method = "stepOn", at = @At("HEAD"), cancellable = true)
    private  void mc_chameleon$disableSculkSensor(Level level, BlockPos pos, BlockState onState, Entity entity, CallbackInfo ci) {
        ci.cancel();
    }
}
