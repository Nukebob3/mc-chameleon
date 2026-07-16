package net.nukebob.chameleon.mixin;

import net.minecraft.world.level.block.FenceBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(FenceBlock.class)
public class FenceBlockMixin {
    @ModifyConstant(method = "<init>", constant = @Constant(floatValue = 24.0F))
    private static float mc_chameleon$lowerFenceHeight(float original) {
        return 16.0F;
    }
}
