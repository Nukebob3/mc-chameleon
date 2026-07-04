package net.nukebob.chameleon.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.nukebob.chameleon.MCChameleonClient;
import net.nukebob.chameleon.accessor.PoseTrackerAccessor;
import net.nukebob.chameleon.gameplay.PoseTracker;
import net.nukebob.chameleon.render.ChameleonRenderTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> {
    @Shadow
    protected M model;

    @Shadow
    protected abstract void scale(S state, PoseStack poseStack);

    @Shadow
    protected abstract void setupRotations(S state, PoseStack poseStack, float bodyRot, float entityScale);

    @Inject(
            method = "getRenderType(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;ZZZ)Lnet/minecraft/client/renderer/rendertype/RenderType;",
            at = @At("RETURN"),
            cancellable = true
    )
    private void mc_chameleon$modifyPlayerRenderType(LivingEntityRenderState state, boolean isBodyVisible, boolean forceTransparent, boolean appearGlowing, CallbackInfoReturnable<RenderType> cir) {
        RenderType originalType = cir.getReturnValue();

        if (originalType != null) {
            if (state instanceof AvatarRenderState avatarRenderState) {
                Identifier texture = avatarRenderState.skin.body().texturePath();

                if (state.scale!=1) {
                    avatarRenderState.showHat=false;
                    avatarRenderState.showJacket=false;
                    avatarRenderState.showLeftPants=false;
                    avatarRenderState.showRightPants=false;
                    avatarRenderState.showLeftSleeve=false;
                    avatarRenderState.showRightSleeve=false;

                    return;
                }

                cir.setReturnValue(ChameleonRenderTypes.entityGray(texture));
            }
        }
    }

    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At("TAIL"))
    private void mc_chameleon$submitUvTrackerCopy(
            S state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera,
            CallbackInfo ci
    ) {
        if (!(state instanceof AvatarRenderState avatarState)) return;
        if (Minecraft.getInstance().player == null) return;
        if (avatarState.id != Minecraft.getInstance().player.getId()) return;

        Identifier texture = avatarState.skin.body().texturePath();
        RenderType uvTrackerType = ChameleonRenderTypes.playerUvTracker(texture);

        poseStack.pushPose();
        float scale = state.scale;
        poseStack.scale(scale, scale, scale);
        this.setupRotations(state, poseStack, state.bodyRot, scale);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        this.scale(state, poseStack);
        poseStack.translate(0.0F, -1.501F, 0.0F);

        int overlayCoords = LivingEntityRenderer.getOverlayCoords(state, 0.0F);
        submitNodeCollector.submitModel(this.model, state, poseStack, uvTrackerType, state.lightCoords, overlayCoords, -1, null, state.outlineColor, null);

        poseStack.popPose();
    }

    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
            at = @At("TAIL")
    )
    private void mc_chameleon$capturePlayerUuid(T entity, S state, float partialTicks, CallbackInfo ci) {
        if (entity instanceof Player player) {
            ((PoseTrackerAccessor) state).mc_chameleon$setPoseTracker(MCChameleonClient.POSES.computeIfAbsent(player.getUUID(), uuid -> new PoseTracker()));
        }
    }

    @Inject(method = "shouldShowName(Lnet/minecraft/world/entity/LivingEntity;D)Z", at = @At("HEAD"), cancellable = true)
    private void disableNametags(T entity, double distanceToCameraSq, CallbackInfoReturnable<Boolean> cir) {
        if (!MCChameleonClient.namePlatesDisplay) {
            cir.setReturnValue(false);
            return;
        }
        if (entity.equals(Minecraft.getInstance().getCameraEntity())) cir.setReturnValue(true);
    }
}
