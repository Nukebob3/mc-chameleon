package net.nukebob.chameleon.render;

import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import java.util.function.BiFunction;

public class ChameleonRenderTypes {
    private static final BiFunction<Identifier, Boolean, RenderType> ENTITY_GRAY;
    private static final BiFunction<Identifier, Boolean, RenderType> PLAYER_UV_TRACKER;
    private static final RenderType GUN_SHOT;


    static {
        ENTITY_GRAY = Util.memoize((texture, affectsOutline) -> {
            RenderSetup state = RenderSetup.builder(ChameleonRenderPipelines.ENTITY_GRAY).withTexture("Sampler0", texture).useLightmap().useOverlay().affectsCrumbling().sortOnUpload().setOutline(affectsOutline ? RenderSetup.OutlineProperty.AFFECTS_OUTLINE : RenderSetup.OutlineProperty.NONE).createRenderSetup();
            return RenderType.create("entity_gray", state);
        });
        PLAYER_UV_TRACKER = Util.memoize((texture, affectsOutline) -> {
            RenderSetup state = RenderSetup.builder(ChameleonRenderPipelines.PLAYER_UV_TRACKER).withTexture("Sampler0", texture)
                    .setOutputTarget(ChameleonOutputTargets.UV_PICKER_TARGET)
                    .createRenderSetup();
            return RenderType.create("player_uv_tracker", state);
        });
        GUN_SHOT = RenderType.create("gun_shot", RenderSetup.builder(ChameleonRenderPipelines.GUN_SHOT).createRenderSetup());
    }

    public static RenderType entityGray(final Identifier texture) {
        return ENTITY_GRAY.apply(texture, false);
    }

    public static RenderType playerUvTracker(final Identifier texture) {
        return PLAYER_UV_TRACKER.apply(texture, false);
    }

    public static RenderType gunShot() {
        return GUN_SHOT;
    }
}
