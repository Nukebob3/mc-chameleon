package net.nukebob.chameleon.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import net.nukebob.chameleon.camera.ChameleonOrbitCamera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "move", at = @At("HEAD"), cancellable = true)
    private void mc_chameleon$redirectToCam(MoverType moverType, Vec3 delta, CallbackInfo ci) {
        if (ChameleonOrbitCamera.getInstance().isActive()&&ChameleonOrbitCamera.getInstance().isInFreeCam()) ci.cancel();
    }
}
