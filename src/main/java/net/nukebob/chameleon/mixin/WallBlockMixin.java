package net.nukebob.chameleon.mixin;

import net.minecraft.world.level.block.WallBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(WallBlock.class)
public class WallBlockMixin {
    @ModifyConstant(method = "<init>", constant = @Constant(floatValue = 24.0F))
    private float mc_chameleon$lowerWallHeight(float original) {
        return 16.0F;
    }
}
