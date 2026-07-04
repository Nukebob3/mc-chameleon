package net.nukebob.chameleon.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.PlayerModelType;
import net.nukebob.chameleon.camera.ChameleonOrbitCamera;
import net.nukebob.chameleon.setter.ArmWidthSetter;
import net.nukebob.chameleon.texture.ChameleonTexture;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    @Inject(method = "shouldRender", at=@At("HEAD"), cancellable = true)
    private void mc_chameleon$renderIfOrbitCamera(Entity entity, Frustum culler, double camX, double camY, double camZ, CallbackInfoReturnable<Boolean> cir) {
        if (ChameleonOrbitCamera.getInstance().isActive()) cir.setReturnValue(true);
    }

    @Inject(method = "getAvatarRenderer", at = @At("RETURN"), cancellable = true)
    private <T extends Avatar & ClientAvatarEntity> void mc_chameleon$forceWideForCanvas(
            Map<PlayerModelType, AvatarRenderer<@NonNull T>> renderers,
            T entity,
            CallbackInfoReturnable<AvatarRenderer<@NonNull T>> cir) {
        if (ArmWidthSetter.CANVAS_SKINS_LIST.contains(entity.getSkin())) {
            AvatarRenderer<@NonNull T> wide = renderers.get(PlayerModelType.WIDE);
            if (wide != null) cir.setReturnValue(wide);
        }
    }

    @Inject(method = "getRenderer(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;)Lnet/minecraft/client/renderer/entity/EntityRenderer;", at = @At("RETURN"), cancellable = true)
    private <S extends EntityRenderState> void mc_chameleon$forceWideRenderer(
            S entityRenderState,
            CallbackInfoReturnable<EntityRenderer<?, ? super S>> cir) {
        if (entityRenderState instanceof AvatarRenderState player) {
            if (Minecraft.getInstance().level == null) return;
            Entity entity = Minecraft.getInstance().level.getEntity(player.id);
            if (entity != null && ChameleonTexture.skins.containsKey(entity.getUUID())) {
                @SuppressWarnings("unchecked")
                EntityRenderer<?, ? super S> wide = (EntityRenderer<?, ? super S>)
                        ((EntityRenderDispatcher)(Object)this).getPlayerRenderer((AbstractClientPlayer) entity);
                if (wide != null) cir.setReturnValue(wide);
            }
        }
    }
}
