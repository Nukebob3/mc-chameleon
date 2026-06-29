package net.nukebob.chameleon.util;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import net.minecraft.util.ARGB;

import java.util.function.IntConsumer;

public final class UvPicker {

    private UvPicker() {}
    public static void pickPixel(RenderTarget pickingTarget, int cursorX, int cursorY, IntConsumer onResult) {
        GpuTexture sourceTexture = pickingTarget.getColorTexture();
        if (sourceTexture == null) {
            throw new IllegalStateException("Picking render target has no color texture");
        }

        int width = sourceTexture.getWidth(0);
        int height = sourceTexture.getHeight(0);

        int x = Math.clamp(cursorX, 0, width - 1);
        int flippedY = height - cursorY - 1;
        int y = Math.clamp(flippedY, 0, height - 1);

        int blockSize = sourceTexture.getFormat().blockSize();
        GpuBuffer buffer = RenderSystem.getDevice().createBuffer(() -> "UV pick buffer", 9, blockSize);

        RenderSystem.getDevice().createCommandEncoder().copyTextureToBuffer(
                sourceTexture,
                buffer,
                0L,
                () -> {
                    try (GpuBufferSlice.MappedView read = buffer.map(true, false)) {
                        int argb = read.data().getInt(0);
                        onResult.accept(argb);
                    } finally {
                        buffer.close();
                    }
                },
                0,
                x, y,
                1, 1
        );
    }

    public static int decodeU(int argb) {
        int rawByte = ARGB.blue(argb);
        int u = (int) ((rawByte / 255.0f) * 64.0f);
        return Math.clamp(u, 0, 63);
    }

    public static int decodeV(int argb) {
        int rawByte = ARGB.green(argb);
        int v = (int) ((rawByte / 255.0f) * 64.0f);
        return Math.clamp(v, 0, 63);
    }
}