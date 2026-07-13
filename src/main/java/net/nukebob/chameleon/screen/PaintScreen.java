package net.nukebob.chameleon.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.nukebob.chameleon.MCChameleon;
import net.nukebob.chameleon.MCChameleonClient;
import net.nukebob.chameleon.camera.ChameleonOrbitCamera;
import net.nukebob.chameleon.gameplay.PoseTracker;
import net.nukebob.chameleon.gameplay.Poses;
import net.nukebob.chameleon.gameplay.TeamControl;
import net.nukebob.chameleon.networking.Payloads;
import net.nukebob.chameleon.render.ChameleonHud;
import net.nukebob.chameleon.texture.BrushGeometry;
import net.nukebob.chameleon.texture.ChameleonTexture;
import net.nukebob.chameleon.texture.ColourLocation;
import net.nukebob.chameleon.texture.Pixel;
import net.nukebob.chameleon.util.ColourUtil;
import net.nukebob.chameleon.util.UvPicker;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class PaintScreen extends Screen {
    private final ChameleonTexture texture;
    private boolean mouseLeftDown;
    private boolean mouseRightDown;
    private boolean mouseMiddleDown;

    private double mousePrevX = 0;
    private double mousePrevY = 0;
    private boolean spaceDown;
    private boolean altDown;

    private static int selectedColour = 0xFF000000;
    private float hue;
    private float saturation;
    private float value;

    private ColourWheelWidget wheel;
    private ColourSliderWidget satSlider;
    private ColourSliderWidget valSlider;
    private ColourSliderHorizontalWidget redSlider;
    private ColourSliderHorizontalWidget greenSlider;
    private ColourSliderHorizontalWidget blueSlider;
    private ColourSliderHorizontalWidget hueSlider;
    private ColourSliderHorizontalWidget satHSlider;
    private ColourSliderHorizontalWidget valHSlider;

    private final List<ColourLocation.ColLoc> pendingPixels = new ArrayList<>();
    private long lastFlushTime = 0;
    private static final long FLUSH_INTERVAL_MS = 50;

    private float age = 0;
    private float timeOpen;

    public PaintScreen() {
        super(Component.literal("Paint Screen"));
        texture = ChameleonTexture.getChameleonTexture(Minecraft.getInstance().player.getUUID());

        int[] hsv = ColourUtil.rgbToHsv(MCChameleonClient.selectedColour);
        this.hue = hsv[0];
        this.saturation = hsv[1];
        this.value = hsv[2];

        timeOpen = 0;
    }

    @Override
    protected void init() {
        int panelSize = width/4;

        selectedColour = MCChameleonClient.selectedColour;

        wheel = addRenderableWidget(new ColourWheelWidget(10, 10, width/4/2-10, rgb -> {
            int[] hsv = ColourUtil.rgbToHsv(rgb[0], rgb[1], rgb[2]);
            this.hue = hsv[0];
            this.saturation = hsv[1];
            recombineColour(true);
        }, selectedColour));

        float satY = 1f - (saturation / 100f);
        float valY = 1f - (value / 100f);

        int pureHue = ColourUtil.hsvToInt(hue, 100f, 100f);

        satSlider = addRenderableWidget(new ColourSliderWidget(
                10 + width/4/2-10 + 10, 10, 10, panelSize/2-10,
                pureHue, 0xFFFFFFFF, satY,
                sat -> {
                    saturation = (1f - sat) * 100f;
                    recombineColour(true);
                }));

        valSlider = addRenderableWidget(new ColourSliderWidget(
                10 + width/4/2-10 + 10 + 18, 10, 10, panelSize/2-10,
                ColourUtil.hsvToInt(hue, saturation, 100f), 0xFF000000, valY,
                val -> {
                    value = (1f - val) * 100f;
                    recombineColour(true);
                }));

        redSlider = addRenderableWidget(new ColourSliderHorizontalWidget(
                10+10, 10 + width/4/2-10 + 10, (width/4/2-10)*2/3, 10, 0xFF000000, 0xFFFF0000, 0, red -> {
            int[] rgb = ColourUtil.intToRgb(selectedColour);
            selectedColour = ColourUtil.rgbToInt(rgb[0], rgb[1], (int)(red*255));
            recombineColour(false);
        }));

        greenSlider = addRenderableWidget(new ColourSliderHorizontalWidget(
                10+10, 10 + width/4/2-10 + 10+13, (width/4/2-10)*2/3, 10, 0xFF000000, 0xFF00FF00, 0, green -> {
            int[] rgb = ColourUtil.intToRgb(selectedColour);
            selectedColour = ColourUtil.rgbToInt(rgb[0], (int)(green*255), rgb[2]);
            recombineColour(false);
        }));

        blueSlider = addRenderableWidget(new ColourSliderHorizontalWidget(
                10+10, 10 + width/4/2-10 + 10+13+13, (width/4/2-10)*2/3, 10, 0xFF000000, 0xFF0000FF, 0, blue -> {
            int[] rgb = ColourUtil.intToRgb(selectedColour);
            selectedColour = ColourUtil.rgbToInt((int)(blue*255), rgb[1], rgb[2]);
            recombineColour(false);
        }));

        hueSlider = addRenderableWidget(new ColourSliderHorizontalWidget(
                10 + width/4/2-10 + 10+5, 10 + width/4/2-10 + 10, (width/4/2-10)*2/3, 10, 0, 0, 0, hueUnprocessed -> {
            float shifted = hueUnprocessed - (2f / 3f);
            float delta = 1f - (shifted - (float) Mth.floor(shifted));
            this.hue = delta * 360f;
            recombineColour(true);
        }));
        hueSlider.hue = true;

        satHSlider = addRenderableWidget(new ColourSliderHorizontalWidget(
                10 + width/4/2-10 + 10+5, 10 + width/4/2-10 + 10+13, (width/4/2-10)*2/3, 10, pureHue, 0xFFFFFFFF, 0, sat -> {
            saturation = (1f - sat) * 100f;
            recombineColour(true);
        }));

        valHSlider = addRenderableWidget(new ColourSliderHorizontalWidget(
                10 + width/4/2-10 + 10+5, 10 + width/4/2-10 + 10+13+13, (width/4/2-10)*2/3, 10, ColourUtil.hsvToInt(hue, saturation, 100f), 0xFF000000, 0, val -> {
            value = (1f - val) * 100f;
            recombineColour(true);
        }));

        recombineColour(true);

        var camera = minecraft.getCameraEntity();
        if (camera != null && !ChameleonOrbitCamera.getInstance().isActive()) {
            ChameleonOrbitCamera.getInstance().syncToEntityLookDirection(camera.getYRot(), camera.getXRot());
            ChameleonOrbitCamera.getInstance().setDistance(2);
        }
        ChameleonOrbitCamera.getInstance().setActive(true);
    }

    private void recombineColour(boolean update) {
        int[] rgb = ColourUtil.intToRgb(selectedColour);
        if (wheel == null || satSlider == null || valSlider == null) return;

        if (update) selectedColour = ColourUtil.hsvToInt(hue, saturation, value);
        else {
            int[] hsv = ColourUtil.rgbToHsv(selectedColour);
            hue = hsv[0];
            saturation = hsv[1];
            value = hsv[2];
        }
        MCChameleonClient.selectedColour = selectedColour;

        wheel.setColour(selectedColour, true);
        satSlider.setYSelected(1f - (saturation / 100f));
        valSlider.setYSelected(1f - (value / 100f));

        redSlider.setXSelected((rgb[2]/255f));
        greenSlider.setXSelected((rgb[1]/255f));
        blueSlider.setXSelected((rgb[0]/255f));

        float shiftedHue = hue - 240f;
        float normalizedShift = shiftedHue - (float) Mth.floor(shiftedHue / 360f) * 360f;
        float hueRatio = normalizedShift / 360f;
        hueSlider.setXSelected(1f - hueRatio);
        satHSlider.setXSelected(1f - (saturation / 100f));
        valHSlider.setXSelected(1f - (value / 100f));

        int pureHue = ColourUtil.hsvToInt(hue, 100f, 100f);
        satSlider.setCol1(pureHue);
        valSlider.setCol1(ColourUtil.hsvToInt(hue, saturation, 100f));
        satHSlider.setCol1(pureHue);
        valHSlider.setCol1(ColourUtil.hsvToInt(hue, saturation, 100f));
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        float interpolate = 0;
        int openAnimationOffset = -150;
        if (timeOpen<10) {
            float f = timeOpen/10f;
            interpolate = f*f-2*f+1;
        }
        graphics.pose().pushMatrix();
        graphics.pose().translate(openAnimationOffset*interpolate,0);

        int panelSize = width/4;
        graphics.fill(5, 5, panelSize, panelSize, 0x66000000);

        super.extractRenderState(graphics, mouseX, mouseY, a);

        int x = mouseX * minecraft.getWindow().getGuiScale();
        int y = mouseY * minecraft.getWindow().getGuiScale();

        MCChameleonClient.mouseX = x;
        MCChameleonClient.mouseY = y;

        if ((MCChameleonClient.uvCol != 0 && MCChameleonClient.uvCol != -1) || (mouseRightDown && !altDown)) {
            double basePixelSize = (44f*MCChameleonClient.brushSize-16f)/7f;
            float cameraDist = ChameleonOrbitCamera.getInstance().getDistance();
            if (cameraDist < 0.1f) cameraDist = 0.1f;

            int brushSize = (int) Math.round(basePixelSize * (2.0f / cameraDist));
            graphics.pose().translate(-openAnimationOffset*interpolate, 0);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, MCChameleon.id("paint/circle"), mouseX-brushSize/2, mouseY-brushSize/2, brushSize, brushSize, 0xFFFFFFFF);
            graphics.pose().translate(openAnimationOffset*interpolate, 0);
        }

        if (spaceDown) {
            graphics.pose().translate(-openAnimationOffset*interpolate, 0);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, MCChameleon.id("paint/cross"), mouseX-12, mouseY-14, 24, 24, 0xFFFFFFFF);
            graphics.pose().translate(openAnimationOffset*interpolate, 0);
        }

        graphics.fill(panelSize/2+46, 10, panelSize-10, panelSize/5, selectedColour);
        graphics.pose().popMatrix();

        graphics.pose().pushMatrix();
        graphics.pose().translate(openAnimationOffset*interpolate,0);
        graphics.pose().scale(0.5f, 0.5f);
        graphics.text(minecraft.font, "R", (redSlider.getX()-5) * 2, (redSlider.getY()+3) * 2, 0xFFFFFFFF, false);
        graphics.text(minecraft.font, "G", (greenSlider.getX()-5) * 2, (greenSlider.getY()+3) * 2, 0xFFFFFFFF, false);
        graphics.text(minecraft.font, "B", (blueSlider.getX()-5) * 2, (blueSlider.getY()+3) * 2, 0xFFFFFFFF, false);
        graphics.text(minecraft.font, "H", (hueSlider.getX()-5) * 2, (hueSlider.getY()+3) * 2, 0xFFFFFFFF, false);
        graphics.text(minecraft.font, "S", (satHSlider.getX()-5) * 2, (satHSlider.getY()+3) * 2, 0xFFFFFFFF, false);
        graphics.text(minecraft.font, "V", (valHSlider.getX()-5) * 2, (valHSlider.getY()+3) * 2, 0xFFFFFFFF, false);
        graphics.pose().popMatrix();

        age += a;
        timeOpen += a;
        if (age > 300) {
            age = 0;
            ClientPlayNetworking.send(new Payloads.ServerBoundUpdatePixelsPayload(MCChameleonClient.localSkinCache));
            pendingPixels.clear();
        }

        graphics.pose().pushMatrix();
        graphics.pose().translate(openAnimationOffset*interpolate,0);
        ChameleonHud.paintScreenControls(graphics, Minecraft.getInstance().options, spaceDown, altDown, mouseLeftDown, mouseRightDown, mouseMiddleDown);
        graphics.pose().popMatrix();

        if (!TeamControl.isChameleonStrict(Minecraft.getInstance().player.getTeam())) Minecraft.getInstance().gui.setScreen(null);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.input() == GLFW.GLFW_KEY_SPACE) {
            spaceDown = true;
            pickColour();
        }
        if (event.input() == GLFW.GLFW_KEY_LEFT_ALT) {
            altDown = true;
        }
        if (event.input() == GLFW.GLFW_KEY_F) onClose();

        return super.keyPressed(event);
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        if (event.input() == GLFW.GLFW_KEY_SPACE) spaceDown = false;
        if (event.input() == GLFW.GLFW_KEY_LEFT_ALT) altDown = false;

        return super.keyReleased(event);
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {}

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        mousePrevX = event.x();
        mousePrevY = event.y();
        if (event.input() == 0) {
            mouseLeftDown = true;
            paintAtCursor();
        } else if (event.input() == 1) {
            mouseRightDown = true;
        } else if (event.input() == 2) {
            mouseMiddleDown = true;
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        ChameleonOrbitCamera.getInstance().setDistance((float) (ChameleonOrbitCamera.getInstance().getDistance() - 0.25 * scrollY));
        return super.mouseScrolled(x, y, scrollX, scrollY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.input() == 0 && mouseLeftDown) {
            mouseLeftDown = false;
            flushPending();
        } else if (event.input() == 1) {
            mouseRightDown = false;
        } else if (event.input() == 2) {
            mouseMiddleDown = false;
        }

        return super.mouseReleased(event);
    }

    @Override
    public void afterMouseMove() {
        super.afterMouseMove();
    }

    @Override
    public void mouseMoved(double x, double y) {
        super.mouseMoved(x, y);

        double dx = x - mousePrevX;
        double dy = y - mousePrevY;
        mousePrevX = x;
        mousePrevY = y;

        if (mouseLeftDown && !altDown) paintAtCursor();
        if (spaceDown) pickColour();

        if (mouseRightDown && !altDown) {
            float direction = (float) Math.signum(dx);
            if (direction != 0) {
                MCChameleonClient.brushSize += (float) (Math.abs(dx) * direction * 0.15f);
                MCChameleonClient.brushSize = Math.clamp(MCChameleonClient.brushSize, 1.0f, 8.0f);
            }
        }

        if (mouseMiddleDown || (altDown && mouseLeftDown)) {
            ChameleonOrbitCamera.getInstance().rotate((float) dx, (float) dy);
        }
        if (mouseRightDown && altDown) {
            float direction = (float) Math.signum(dx);
            ChameleonOrbitCamera.getInstance().setDistance((float) (ChameleonOrbitCamera.getInstance().getDistance() - 0.25f * (float)(Math.abs(dx) * direction * 0.15f)));
        }
    }

    private void paintAtCursor() {
        if (MCChameleonClient.uvCol == 0 || MCChameleonClient.uvCol == -1) return;
        if (minecraft.player == null || !ChameleonTexture.skins.containsKey(minecraft.player.getUUID())) return;

        int centerU = UvPicker.decodeU(MCChameleonClient.uvCol);
        int centerV = UvPicker.decodeV(MCChameleonClient.uvCol);
        int centerLocation = ChameleonTexture.reversePixelIndex(centerU, centerV);
        if (centerLocation == -1) return;

        float radius = MCChameleonClient.brushSize / 2.0f;

        PoseTracker tracker = MCChameleonClient.POSES.get(minecraft.player.getUUID());
        Poses currentPose = tracker != null ? tracker.getPose() : null;

        int part = ChameleonTexture.getPart(centerLocation);
        int face = ChameleonTexture.getFace(part, ChameleonTexture.getLocalIndex(centerLocation, part));
        Pixel faceOffset = ChameleonTexture.getFaceOffset(part, face);



        int radiusInt = (int) Math.ceil(radius) + 1;
        for (int u = faceOffset.x; u < faceOffset.x+ChameleonTexture.getFaceDimension(part, face).x; u++) {
            for (int v = faceOffset.y; v < faceOffset.y+ChameleonTexture.getFaceDimension(part, face).y; v++) {
                int location = ChameleonTexture.reversePixelIndex(u, v);
                if (location == -1) continue;

                /*float dist = currentPose != null
                        ? BrushGeometry.posedDistance(centerLocation, location, currentPose)
                        : BrushGeometry.distance(centerLocation, location);*/
                float dist = BrushGeometry.distance(centerLocation, location);

                //if (dist <= radius) {
                float distance = new Vec2(u-centerU, v-centerV).length();
                if (distance<=radius) {
                    ColourLocation.ColLoc pixelUpdate = new ColourLocation.ColLoc(selectedColour, location);
                    texture.updatePixel(pixelUpdate);
                    pendingPixels.add(pixelUpdate);
                }
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
                minecraft.gameRenderer.mainRenderTarget(),
                (int) minecraft.mouseHandler.xpos(),
                (int) minecraft.mouseHandler.ypos(),
                col -> {
                    int r = (col >> 16) & 0xFF;
                    int g = (col >> 8) & 0xFF;
                    int b = col & 0xFF;

                    int[] hsv = ColourUtil.rgbToHsv(r, g, b);

                    selectedColour = 0xFF000000 | (b << 16) | (g << 8) | r;
                    MCChameleonClient.selectedColour = selectedColour;

                    this.hue = hsv[0];
                    this.saturation = hsv[1];
                    this.value = hsv[2];

                    recombineColour(true);
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
        MCChameleonClient.wasOpenPaintScreenDown = true;
        ClientPlayNetworking.send(new Payloads.ServerBoundUpdatePixelsPayload(MCChameleonClient.localSkinCache));
    }
}