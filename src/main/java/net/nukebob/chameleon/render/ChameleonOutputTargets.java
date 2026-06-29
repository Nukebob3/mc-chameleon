package net.nukebob.chameleon.render;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.RenderTargetDescriptor;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import org.joml.Vector4f;

public class ChameleonOutputTargets {
    private static final Vector4f CLEAR_COLOR = new Vector4f(0.0f, 0.0f, 0.0f, 0.0f);
    public static RenderTarget uvTrackerBufferInstance = null;

    public static OutputTarget UV_PICKER_TARGET = new OutputTarget("mc-chameleon:player_uv_buffer",
        () -> {
            Minecraft mc = Minecraft.getInstance();
            int width = mc.getWindow().getWidth();
            int height = mc.getWindow().getHeight();

            if (uvTrackerBufferInstance == null) {
                uvTrackerBufferInstance = new RenderTargetDescriptor(
                        width, height, true, CLEAR_COLOR, GpuFormat.RGBA8_UNORM
                ).allocate();
            } else if (uvTrackerBufferInstance.width != width || uvTrackerBufferInstance.height != height) {
                uvTrackerBufferInstance.resize(width, height);
            }

            return uvTrackerBufferInstance;
        });

    public static void clearUvPickerTarget() {
        if (uvTrackerBufferInstance != null && uvTrackerBufferInstance.getColorTexture()!=null && uvTrackerBufferInstance.getDepthTexture()!=null) {
            RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(
                    uvTrackerBufferInstance.getColorTexture(),
                    CLEAR_COLOR,
                    uvTrackerBufferInstance.getDepthTexture(),
                    0.0
            );
        }
    }

    //public static OutputTarget UV_PICKER_TARGET = Minecraft.getInstance().levelRenderer.entityOutlineTarget();
    //public static OutputTarget UV_PICKER_TARGET = new OutputTarget("uv_picker_target", () -> new TextureTarget("uv_picker_target", Minecraft.getInstance().getWindow().getWidth(), Minecraft.getInstance().getWindow().getHeight(), true, GpuFormat.RGBA8_UNORM));
}
