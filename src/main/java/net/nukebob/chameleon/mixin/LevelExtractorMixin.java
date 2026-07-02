package net.nukebob.chameleon.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.nukebob.chameleon.camera.ChameleonOrbitCamera;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelExtractor.class)
public abstract class LevelExtractorMixin {
    @Shadow
    protected abstract EntityRenderState extractEntity(Entity entity, float partialTickTime);

    @Shadow
    @Final
    private LevelRenderState levelRenderState;

    @Inject(method = "extractVisibleEntities", at = @At("TAIL"))
    private void mc_chameleon$addClientPlayer(Camera camera, Frustum frustum, DeltaTracker deltaTracker, LevelRenderState output, CallbackInfo ci) {
        if (ChameleonOrbitCamera.getInstance().isActive()) {
            float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);
            EntityRenderState state = extractEntity(Minecraft.getInstance().player, partialTick);
            if (state.nameTag!=null) state.nameTag = Component.literal(state.nameTag.getString()).withColor(0xFF00FF00);
            levelRenderState.entityRenderStates.add(state);
        }
    }
}
