package net.nukebob.chameleon.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.nukebob.chameleon.MCChameleon;

import static net.minecraft.client.renderer.RenderPipelines.ENTITY_SNIPPET;

public class ChameleonRenderPipelines {
    public static final RenderPipeline ENTITY_GRAY;
    public static final RenderPipeline PLAYER_UV_TRACKER;
    public static final RenderPipeline GUN_SHOT;

    static {
        ENTITY_GRAY = RenderPipelines.register(RenderPipeline.builder(ENTITY_SNIPPET)
                .withLocation("pipeline/entity_gray")
                        .withFragmentShader(MCChameleon.id("core/entity_gray"))
                        .withVertexShader("core/entity")
                .withShaderDefine("ALPHA_CUTOUT", 0.1F)
                .withShaderDefine("PER_FACE_LIGHTING")
                .withBindGroupLayout(BindGroupLayouts.SAMPLER1)
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withCull(false).build());

        PLAYER_UV_TRACKER = RenderPipelines.register(RenderPipeline.builder(ENTITY_SNIPPET)
                .withLocation("pipeline/uv_picker_target")
                .withFragmentShader(MCChameleon.id("core/player_uv_tracker"))
                .withVertexShader("core/entity")
                .withShaderDefine("ALPHA_CUTOUT", 0.1F)
                .withShaderDefine("PER_FACE_LIGHTING")
                .withBindGroupLayout(BindGroupLayouts.SAMPLER1)
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withCull(false).build());

        GUN_SHOT = RenderPipelines.register(RenderPipeline.builder(ENTITY_SNIPPET)
                .withLocation("pipeline/gun_shot")
                .withFragmentShader(MCChameleon.id("core/gun_shot"))
                .withVertexShader(MCChameleon.id("core/gun_shot"))
                .withShaderDefine("APPLY_TEXTURE_MATRIX", 0)
                .withBindGroupLayout(BindGroupLayouts.SAMPLER1)
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withCull(false).build());
    }
}
