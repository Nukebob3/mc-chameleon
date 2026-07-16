package net.nukebob.chameleon.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RedStoneOreBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RedStoneOreBlock.class)
public class RedStoneOreBlockMixin {
    @Inject(method = "stepOn", at = @At("HEAD"), cancellable = true)
    private void mc_chameleon$disableRedstoneBlock(Level level, BlockPos pos, BlockState onState, Entity entity, CallbackInfo ci) {
        ci.cancel();
    }
}
