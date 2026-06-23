package net.nukebob.chameleon.Render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import java.util.function.BiFunction;

public class ChameleonRenderTypes {
    private static final BiFunction<Identifier, Boolean, RenderType> ENTITY_GRAY;

    static {
        ENTITY_GRAY = Util.memoize((texture, affectsOutline) -> {
            RenderSetup state = RenderSetup.builder(ChameleonRenderPipelines.ENTITY_GRAY).withTexture("Sampler0", texture).useLightmap().useOverlay().affectsCrumbling().sortOnUpload().setOutline(affectsOutline ? RenderSetup.OutlineProperty.AFFECTS_OUTLINE : RenderSetup.OutlineProperty.NONE).createRenderSetup();
            return RenderType.create("entity_gray", state);
        });
    }

    public static RenderType entityGray(final Identifier texture) {
        return ENTITY_GRAY.apply(texture, false
        );
    }
}
