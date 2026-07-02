package net.nukebob.chameleon.screen;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.nukebob.chameleon.MCChameleon;
import net.nukebob.chameleon.util.ColourUtil;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public class ColourWheelWidget extends AbstractWidget {
    private static Identifier wheelTextureId;
    private static DynamicTexture wheelTexture;

    private float hue = 0f;
    private float saturation = 1f;
    private boolean dragging = false;

    private float xSelected;
    private float ySelected;

    private final Consumer<int[]> onColourChanged;

    public ColourWheelWidget(int x, int y, int size, Consumer<int[]> onColourChanged, int colour) {
        super(x, y, size, size, Component.literal("Colour Wheel"));
        this.onColourChanged = onColourChanged;
        ensureWheelTexture(size*10);

        this.xSelected = 0f;
        this.ySelected = 0f;


        setColour(colour, true);
    }

    private static void ensureWheelTexture(int size) {
        if (wheelTexture != null) return;

        NativeImage image = new NativeImage(size, size, true);
        float center = size / 2f;
        float radius = center - 2;

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = x - center;
                float dy = y - center;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                if (dist <= radius) {
                    float angle = (float) Math.toDegrees(Math.atan2(dy, dx));
                    if (angle < 0) angle += 360f;
                    float sat = Math.min(100f, (dist / radius) * 100f);
                    int[] rgb = ColourUtil.hsvToRgb(angle, sat, 100f);
                    int abgr = 0xFF000000 | (rgb[2] << 16) | (rgb[1] << 8) | rgb[0];
                    image.setPixel(x, y, abgr);
                } else {
                    image.setPixel(x, y, 0x00000000);
                }
            }
        }

        wheelTexture = new DynamicTexture(() -> "chameleon-color-wheel", image);
        wheelTextureId = MCChameleon.id("color_wheel");
        Minecraft.getInstance().getTextureManager().register(wheelTextureId, wheelTexture);
    }

    public void setColour(int colour, boolean quiet) {
        int[] rgb = ColourUtil.intToRgb(colour);

        int[] hsv = ColourUtil.rgbToHsv(rgb[0],rgb[1],rgb[2]);
        this.hue = hsv[0];
        this.saturation = hsv[1];

        double angleRad = Math.toRadians(hue);
        double normDist = saturation / 100.0;

        this.xSelected = (float) (Math.cos(angleRad) * normDist);
        this.ySelected = (float) (Math.sin(angleRad) * normDist);

        if (!quiet) onColourChanged.accept(getSelectedRgb());
    }

    public int[] getSelectedRgb() {
        return ColourUtil.hsvToRgb(hue, saturation, 100f);
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, wheelTextureId, getX(), getY(), 0f, 0f, getWidth(), getHeight(), getWidth(), getHeight());

        double center = getWidth() / 2.0;
        int markerX = (int) Math.round(getX() + center + (xSelected * center));
        int markerY = (int) Math.round(getY() + center + (ySelected * center));
         graphics.outline(markerX - 2, markerY - 2, 4, 4, 0xFFFFFFFF);
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        dragging = true;
        pick(event.x(), event.y());
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double centerX = this.getX() + (this.width / 2.0);
        double centerY = this.getY() + (this.height / 2.0);

        double dx = event.x() - centerX;
        double dy = event.y() - centerY;

        double radius = this.width / 2.0;
        double radiusSq = radius * radius;

        if ((dx * dx) + (dy * dy) > radiusSq) {
            return false;
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void playDownSound(@NonNull SoundManager soundManager) {}

    @Override
    protected void onDrag(@NonNull MouseButtonEvent event, double dx, double dy) {
        if (dragging) {
            pick(event.x(), event.y());
        }
    }

    @Override
    public void onRelease(@NonNull MouseButtonEvent event) {
        dragging = false;
    }

    private void pick(double mouseX, double mouseY) {
        double center = getWidth() / 2.0;
        double dx = mouseX - (getX() + center);
        double dy = mouseY - (getY() + center);

        double dist = Math.sqrt(dx * dx + dy * dy);

        if (dist > center) {
            dx = (dx / dist) * center;
            dy = (dy / dist) * center;
            dist = center;
        }

        float rawX = (float) (dx / center);
        float rawY = (float) (dy / center);

        float mag = (float) Math.sqrt(rawX * rawX + rawY * rawY);
        if (mag > 1.0f) {
            rawX /= mag;
            rawY /= mag;
        }

        this.xSelected = rawX;
        this.ySelected = rawY;

        double angle = Math.toDegrees(Math.atan2(dy, dx));
        if (angle < 0) angle += 360;

        hue = (float) angle;
        saturation = (float) Math.min(100.0, (dist / center) * 100.0);

        onColourChanged.accept(getSelectedRgb());
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(net.minecraft.client.gui.narration.NarratedElementType.TITLE, getMessage());
    }
}
