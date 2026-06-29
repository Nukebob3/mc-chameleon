package net.nukebob.chameleon.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.nukebob.chameleon.MCChameleon;
import net.nukebob.chameleon.MCChameleonClient;
import net.nukebob.chameleon.camera.ChameleonOrbitCamera;
import net.nukebob.chameleon.gameplay.PoseTracker;
import net.nukebob.chameleon.gameplay.Poses;
import net.nukebob.chameleon.networking.Payloads;
import net.nukebob.chameleon.texture.BrushGeometry;
import net.nukebob.chameleon.texture.ChameleonTexture;
import net.nukebob.chameleon.texture.ColourLocation;
import net.nukebob.chameleon.util.ColourUtil;
import net.nukebob.chameleon.util.UvPicker;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PaintScreen extends Screen {
    private final ChameleonTexture texture;
    private boolean mouseLeftDown;
    private boolean mouseRightDown;
    private boolean mouseMiddleDown;

    private double mousePrevX = 0;
    private double mousePrevY = 0;
    private boolean spaceDown;

    private int selectedColour = 0xFF000000;
    private float hue;
    private float saturation;
    private float value;

    private ColourWheelWidget wheel;
    private ColourSliderWidget satSlider;
    private ColourSliderWidget valSlider;

    private final List<ColourLocation.ColLoc> pendingPixels = new ArrayList<>();
    private long lastFlushTime = 0;
    private static final long FLUSH_INTERVAL_MS = 50;

    private float age = 0;

    public PaintScreen() {
        super(Component.literal("Paint Screen"));
        texture = ChameleonTexture.getChameleonTexture(Minecraft.getInstance().player.getUUID());

        float[] sv = ColourUtil.getSaturationAndValue(MCChameleonClient.selectedColour);
        this.saturation = sv[0];
        this.value = sv[1];
    }

    @Override
    protected void init() {
        switch (new Random().nextInt(4)) {
            case 0 -> PoseTracker.setTargetPose(Poses.T_POSE);
            case 1 -> PoseTracker.setTargetPose(Poses.ARCH);
            case 2 -> PoseTracker.setTargetPose(Poses.FLAT);
            default -> PoseTracker.setTargetPose(null);
        }

        int panelSize = width/4;

        this.selectedColour = MCChameleonClient.selectedColour;


        wheel = addRenderableWidget(new ColourWheelWidget(10, 10, width/4/2-10, rgb -> {
            this.saturation = ColourUtil.getSaturationAndValue(ColourUtil.rgbToInt(rgb[0],rgb[1],rgb[2]))[0];
            recombineColour();
        },selectedColour));

        float satY = 1f - (saturation / 100f);
        float valY = 1f - (value / 100f);

        int[] pureHueRgb = wheel.getPureHueRgb();
        int pureHue = 0xFF000000 | (pureHueRgb[2] << 16) | (pureHueRgb[1] << 8) | pureHueRgb[0];

        satSlider  = addRenderableWidget(new ColourSliderWidget(
                panelSize/2+10,10,10, panelSize /2-10,
                pureHue, 0xFFFFFFFF, satY,
                sat -> {saturation = (1f - sat) * 100f; recombineColour();}));

        valSlider = addRenderableWidget(new ColourSliderWidget(
                panelSize/2+28,10,10, panelSize /2-10,
                ColourUtil.getMaxValue(selectedColour), 0xFF000000, valY,
                val -> {value = (1f-val)*100f; recombineColour();}));



        var camera = Minecraft.getInstance().getCameraEntity();
        if (camera != null && !ChameleonOrbitCamera.isActive()) {
            ChameleonOrbitCamera.syncToEntityLookDirection(camera.getYRot(), camera.getXRot());
        }
        ChameleonOrbitCamera.setActive(true);
        ChameleonOrbitCamera.setDistance(2);
    }

    private void recombineColour() {
        if (wheel == null || satSlider == null || valSlider == null) return;

        int[] rgb = wheel.getHueOnlyRgb(saturation, value);
        this.selectedColour = 0xFF000000 | (rgb[2] << 16) | (rgb[1] << 8) | rgb[0];
        MCChameleonClient.selectedColour = this.selectedColour;

        wheel.setSaturationQuiet(saturation);
        satSlider.setYSelected(1f - (saturation / 100f));

        int[] pureHueRgb = wheel.getPureHueRgb();
        int pureHue = 0xFF000000 | (pureHueRgb[2] << 16) | (pureHueRgb[1] << 8) | pureHueRgb[0];
        satSlider.setCol1(pureHue);
        valSlider.setCol1(ColourUtil.getMaxValue(selectedColour));
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        int panelSize = width/4;
        graphics.fill(5, 5, panelSize, panelSize, 0x66000000);

        super.extractRenderState(graphics, mouseX, mouseY, a);

        int x = mouseX * Minecraft.getInstance().getWindow().getGuiScale();
        int y = mouseY * Minecraft.getInstance().getWindow().getGuiScale();

        MCChameleonClient.mouseX = x;
        MCChameleonClient.mouseY = y;

        if ((MCChameleonClient.uvCol!=0&&MCChameleonClient.uvCol!=-1)||mouseRightDown) {
            double basePixelSize = ((MCChameleonClient.brushSize - 1) / 7.0) * 20.0 + 4.0;
            float cameraDist = ChameleonOrbitCamera.getDistance();
            if (cameraDist < 0.1f) cameraDist = 0.1f;

            int brushSize = (int) Math.round(basePixelSize * (2.0f / cameraDist));

            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, MCChameleon.id("paint/circle"), mouseX-brushSize/2, mouseY-brushSize/2, brushSize,brushSize, 0xFFFFFFFF);
        }

        graphics.fill(panelSize/2+46, 10, panelSize-10, panelSize /5, selectedColour);


        age += a;
        if (age>300) {
            age = 0;
            ClientPlayNetworking.send(new Payloads.ServerBoundUpdatePixelsPayload(MCChameleonClient.localSkinCache));
            pendingPixels.clear();
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.input()== GLFW.GLFW_KEY_SPACE) {
            spaceDown = true;
            pickColour();
        }

        return super.keyPressed(event);
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        if (event.input()== GLFW.GLFW_KEY_SPACE) spaceDown = false;
        if (event.input()==GLFW.GLFW_KEY_F) onClose();

        return super.keyReleased(event);
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {}

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        mousePrevX = event.x();
        mousePrevY = event.y();
        if (event.input()==0) {
            mouseLeftDown = true;

            paintAtCursor();
        } else if (event.input()==1) {
            mouseRightDown = true;
        } else if (event.input()==2) {
            mouseMiddleDown = true;
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        ChameleonOrbitCamera.setDistance((float) (ChameleonOrbitCamera.getDistance()-0.25*scrollY));

        return super.mouseScrolled(x, y, scrollX, scrollY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.input()==0) {
            mouseLeftDown = false;
            flushPending();
        }
        else if (event.input()==1) mouseRightDown = false;
        else if (event.input()==2) mouseMiddleDown = false;

        return super.mouseReleased(event);
    }

    @Override
    public void afterMouseMove() {
        super.afterMouseMove();

        if (mouseLeftDown) paintAtCursor();
        if (spaceDown) pickColour();
    }

    @Override
    public void mouseMoved(double x, double y) {
        super.mouseMoved(x, y);

        double dx = x - mousePrevX;
        double dy = y - mousePrevY;
        mousePrevX = x;
        mousePrevY = y;

        if (mouseLeftDown) paintAtCursor();
        if (spaceDown) pickColour();

        if (mouseRightDown) {

            float direction = (float) Math.signum(dx);

            if (direction != 0) {
                MCChameleonClient.brushSize += (float) (Math.abs(dx) * direction * 0.15f);

                MCChameleonClient.brushSize = Math.clamp(MCChameleonClient.brushSize, 1.0f, 8.0f);
            }
        }

        if (mouseMiddleDown) {
            ChameleonOrbitCamera.rotate((float) dx, (float) dy);
        }
    }

    private void paintAtCursor() {
        if (MCChameleonClient.uvCol == 0 || MCChameleonClient.uvCol == -1) return;

        int centerU = UvPicker.decodeU(MCChameleonClient.uvCol);
        int centerV = UvPicker.decodeV(MCChameleonClient.uvCol);
        int centerLocation = ChameleonTexture.reversePixelIndex(centerU, centerV);
        if (centerLocation == -1) return;

        float radius = (float) (MCChameleonClient.brushSize / 2.0);

        for (int location = 0; location < BrushGeometry.getTexelCount(); location++) {
            float dist = BrushGeometry.distance(centerLocation, location);
            if (dist <= radius) {
                ColourLocation.ColLoc pixelUpdate = new ColourLocation.ColLoc(selectedColour, location);
                texture.updatePixel(pixelUpdate);
                pendingPixels.add(pixelUpdate);
            }
        }

        maybeFlushPending();
    }

    private void maybeFlushPending() {
        long now = System.currentTimeMillis();
        if (pendingPixels.isEmpty() || now - lastFlushTime < FLUSH_INTERVAL_MS) return;
        flushPending();
    }

    private void flushPending() {
        if (pendingPixels.isEmpty()) return;
        ClientPlayNetworking.send(new Payloads.ServerBoundUpdateSpecificPixelsPayload(pendingPixels.toArray(new ColourLocation.ColLoc[0])));
        pendingPixels.clear();
        lastFlushTime = System.currentTimeMillis();
    }

    private void pickColour() {
        UvPicker.pickPixel(
                Minecraft.getInstance().gameRenderer.mainRenderTarget(),
                (int) Minecraft.getInstance().mouseHandler.xpos(),
                (int) Minecraft.getInstance().mouseHandler.ypos(),
                col -> {
                    int r = (col >> 16) & 0xFF;
                    int g = (col >> 8) & 0xFF;
                    int b = col & 0xFF;

                    this.selectedColour = 0xFF000000 | (b << 16) | (g << 8) | r;
                    MCChameleonClient.selectedColour = this.selectedColour;

                    float[] sv = ColourUtil.getSaturationAndValue(this.selectedColour);
                    this.saturation = sv[0];
                    this.value = sv[1];

                    if (wheel != null) {
                        wheel.setColour(this.selectedColour);
                        this.hue = wheel.getPureHueRgb()[0];
                    }

                    if (satSlider != null && valSlider != null) {
                        satSlider.setYSelected(1f - (saturation / 100f));
                        valSlider.setYSelected(1f - (value / 100f));

                        int[] pureHueRgb = wheel.getPureHueRgb();
                        int pureHue = 0xFF000000 | (pureHueRgb[2] << 16) | (pureHueRgb[1] << 8) | pureHueRgb[0];
                        satSlider.setCol1(pureHue);
                        valSlider.setCol1(pureHue);
                    }
                }
        );
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        super.onClose();
        ClientPlayNetworking.send(new Payloads.ServerBoundUpdatePixelsPayload(MCChameleonClient.localSkinCache));
    }
}
