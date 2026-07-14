package net.nukebob.chameleon.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TurtleEggBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TurtleEggBlock.class)
public class TurtleEggBlockMixin {
    @Inject(method = "stepOn", at = @At("HEAD"), cancellable = true)
    private void mc_chameleon$disableTurtleEggBreakOnStep(Level level, BlockPos pos, BlockState onState, Entity entity, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "fallOn", at = @At("HEAD"), cancellable = true)
    private void mc_chameleon$disableTurtleEggBreakOnFall(Level level, BlockState state, BlockPos pos, Entity entity, double fallDistance, CallbackInfo ci) {
        ci.cancel();
    }
}
