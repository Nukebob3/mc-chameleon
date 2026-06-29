package net.nukebob.chameleon.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public class ColourSliderWidget extends AbstractWidget {
    boolean hue = false;
    private int col1;
    private final int col2;

    private float ySelected;
    private boolean dragging = false;

    private final Consumer<Float> onValueChanged;

    public ColourSliderWidget(int x, int y, int width, int height, int col1, int col2, float ySelected, Consumer<Float> onValueChanged) {
        super(x, y, width, height, Component.literal("Colour Slider"));

        this.col1 = col1;
        this.col2 = col2;

        this.onValueChanged = onValueChanged;

        this.ySelected = ySelected;
    }

    public void setCol1(int col1) {
        this.col1 = col1;
    }

    public void setYSelected(float ySelected) {
        this.ySelected = Math.max(0f, Math.min(1f, ySelected));
    }

    public float getYSelected() {
        return ySelected;
    }


    @Override
    protected void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        if (!hue)
            graphics.fillGradient(getX(), getY(), getX()+width, getY()+height, col1, col2);

        int markerY = getY() + Math.round(ySelected * getHeight());
        graphics.outline(getX() - 1, markerY - 1, getWidth() + 2, 3, 0xFFFFFFFF);
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        dragging = true;
        pick(event.y());
    }

    @Override
    protected void onDrag(@NonNull MouseButtonEvent event, double dx, double dy) {
        if (dragging) {
            pick(event.y());
        }
    }

    @Override
    public void onRelease(@NonNull MouseButtonEvent event) {
        dragging = false;
    }

    private void pick(double mouseY) {
        float t = (float) ((mouseY - getY()) / (double) getHeight());
        setYSelected(t);
        onValueChanged.accept(ySelected);
    }

    @Override
    protected void updateWidgetNarration(@NonNull NarrationElementOutput output) {}
}
