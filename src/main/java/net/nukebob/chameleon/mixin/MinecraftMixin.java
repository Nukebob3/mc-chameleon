package net.nukebob.chameleon.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.nukebob.chameleon.MCChameleonClient;
import net.nukebob.chameleon.item.ChameleonItems;
import net.nukebob.chameleon.render.GunBeamRenderer;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow
    @Final
    private DeltaTracker.Timer deltaTracker;

    @Shadow
    @Nullable
    public LocalPlayer player;

    @Shadow
    @Nullable
    public abstract Entity getCameraEntity();

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void cancelAttack(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
        if (player!=null&&player.getActiveItem().is(ChameleonItems.GUN)) {
            MCChameleonClient.shots.add(new GunBeamRenderer(player.getPosition(deltaTracker.getGameTimeDeltaPartialTick(false)), player.raycastHitResult(1000f, getCameraEntity()).getLocation()));
        }
    }

    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void cancelUse(CallbackInfo ci) {
        ci.cancel();
    }
}
