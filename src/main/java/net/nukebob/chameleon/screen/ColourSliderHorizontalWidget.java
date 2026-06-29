package net.nukebob.chameleon.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public class ColourSliderHorizontalWidget extends AbstractWidget {
    boolean hue = false;
    private int col1;
    private final int col2;

    private float xSelected;
    private boolean dragging = false;

    private final Consumer<Float> onValueChanged;

    public ColourSliderHorizontalWidget(int x, int y, int width, int height, int col1, int col2, float ySelected, Consumer<Float> onValueChanged) {
        super(x, y, width, height, Component.literal("Colour Slider"));

        this.col1 = col1;
        this.col2 = col2;

        this.onValueChanged = onValueChanged;

        this.xSelected = ySelected;
    }

    public void setCol1(int col1) {
        this.col1 = col1;
    }

    public void setXSelected(float xSelected) {
        this.xSelected = Math.max(0f, Math.min(1f, xSelected));
    }

    public float getXSelected() {
        return xSelected;
    }


    @Override
    protected void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        if (!hue)
            graphics.fillGradient(getX(), getY(), getX()+width, getY()+height, col1, col2);

        int markerX = getX() + Math.round(xSelected * getWidth());
        graphics.outline(markerX - 1, getY() - 1, 3, getHeight()+2, 0xFFFFFFFF);
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
        setXSelected(t);
        onValueChanged.accept(xSelected);
    }

    @Override
    protected void updateWidgetNarration(@NonNull NarrationElementOutput output) {}
}

