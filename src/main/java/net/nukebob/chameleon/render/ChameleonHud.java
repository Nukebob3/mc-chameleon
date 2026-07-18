package net.nukebob.chameleon.render;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.nukebob.chameleon.MCChameleon;
import net.nukebob.chameleon.MCChameleonClient;
import net.nukebob.chameleon.camera.ChameleonOrbitCamera;
import net.nukebob.chameleon.font.ChameleonFonts;
import net.nukebob.chameleon.gameplay.TeamControl;
import net.nukebob.chameleon.keybind.Keybinds;
import net.nukebob.chameleon.screen.PaintScreen;
import net.nukebob.chameleon.voicechat.VoiceChatAccess;
import org.lwjgl.glfw.GLFW;

public class ChameleonHud {
    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Options options = Minecraft.getInstance().options;
        Player player = Minecraft.getInstance().player;
        if (player==null) return;

        if (TeamControl.isChameleon(player.getTeam())) bottomKeyBindsChameleon(graphics, deltaTracker, options);
        else if (TeamControl.isHunter(player.getTeam())) bottomKeyBindsHunter(graphics, deltaTracker, options);
        if (ChameleonOrbitCamera.getInstance().isActive()&&!(Minecraft.getInstance().gui.screen()instanceof PaintScreen)) freeCamPanel(graphics, deltaTracker, options);
        if (TeamControl.isChameleon(player.getTeam())) rightSideKeyBinds(graphics, deltaTracker, options);

        GameHud.render(graphics, deltaTracker);
    }

    private static void bottomKeyBindsChameleon(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Options options) {
        if (MCChameleonClient.climbing) {
            graphics.fill(graphics.guiWidth() / 2 - 73, graphics.guiHeight() - 50, graphics.guiWidth() / 2 - 20, graphics.guiHeight() - 23, 0x55000000);
            if (!options.keyJump.isUnbound()) {
                if (options.keyJump.isDown())
                    graphics.fill(graphics.guiWidth() / 2 - 40, graphics.guiHeight() - 50, graphics.guiWidth() / 2 - 20, graphics.guiHeight() - 23, 0x66000000);
                renderLabel(graphics, "Move Up", graphics.guiWidth() / 2 - 42, graphics.guiHeight() - 30, 24, 7, 0.5f, true);
                renderKey(graphics, options.keyJump, graphics.guiWidth() / 2 - 40 + 3, graphics.guiHeight() - 50 + 3, 14, 0xFFFFFFFF, 0xFF000000);
            }

            if (!options.keyShift.isUnbound()) {
                if (options.keyShift.isDown()) {
                    graphics.fill(graphics.guiWidth() / 2 - 73, graphics.guiHeight() - 50, graphics.guiWidth() / 2 - 47, graphics.guiHeight() - 23, 0x66000000);
                }
                renderKey(graphics, options.keyShift, graphics.guiWidth() / 2 - 70 + 3, graphics.guiHeight() - 50 + 3, 14, 0xFFFFFFFF, 0xFF000000);
                renderLabel(graphics, "Move Down", graphics.guiWidth() / 2 - 75, graphics.guiHeight() - 30, 30, 7, 0.5f, true);
            }

            if (!options.keySprint.isUnbound()) {
                graphics.fill(graphics.guiWidth() / 2 - 99, graphics.guiHeight() - 50, graphics.guiWidth() / 2 - 77, graphics.guiHeight() - 23, 0x55000000);
                renderKey(graphics, options.keySprint, graphics.guiWidth() / 2 - 98 + 3, graphics.guiHeight() - 50 + 3, 14, 0xFF00FF39, 0xFF000000);
                renderLabel(graphics, "Detach", graphics.guiWidth() / 2 - 99, graphics.guiHeight() - 30, 22, 7, 0.5f, true);
            }
        } else {
            if (!options.keyJump.isUnbound()) {
                graphics.fill(graphics.guiWidth() / 2 - 40, graphics.guiHeight() - 50, graphics.guiWidth() / 2 - 20, graphics.guiHeight() - 23, 0x55000000); // width = 20
                renderKey(graphics, options.keyJump, graphics.guiWidth() / 2 - 40 + 3, graphics.guiHeight() - 50 + 3, 14, 0xFF00FBFF, 0xFF000000);
                renderLabel(graphics, "Climb", graphics.guiWidth() / 2 - 40, graphics.guiHeight() - 30, 20, 7, 0.5f, true);
            }
        }
        if (!Keybinds.cameraLock.isUnbound()) {
            graphics.fill(graphics.guiWidth() / 2 - 16, graphics.guiHeight() - 50, graphics.guiWidth() / 2 + 40, graphics.guiHeight() - 23, Keybinds.cameraLock.isDown() ? 0xAA000000 : 0x55000000); // width = 56
            renderKey(graphics, Keybinds.cameraLock, graphics.guiWidth()/2-16+21, graphics.guiHeight()-50+3, 14, 0xFFFF79E3, 0xFF000000);
            renderLabel(graphics, "Switch to Free Cam", graphics.guiWidth()/2-16, graphics.guiHeight()-30, 56, 7, 0.5f, true);
        }
    }
    private static void bottomKeyBindsHunter(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Options options) {
        if (MCChameleonClient.climbing) {
            graphics.fill(graphics.guiWidth() / 2 - 73 +18, graphics.guiHeight() - 50, graphics.guiWidth() / 2 - 20 +18, graphics.guiHeight() - 23, 0x55000000);
            if (!options.keyJump.isUnbound()) {
                if (options.keyJump.isDown())
                    graphics.fill(graphics.guiWidth() / 2 - 40 + 18, graphics.guiHeight() - 50, graphics.guiWidth() / 2 - 20 + 18, graphics.guiHeight() - 23, 0x66000000);
                renderKey(graphics, options.keyJump, graphics.guiWidth() / 2 - 40 + 3 + 18, graphics.guiHeight() - 50 + 3, 14, 0xFFFFFFFF, 0xFF000000);
                renderLabel(graphics, "Move Up", graphics.guiWidth() / 2 - 42 + 18, graphics.guiHeight() - 30, 24, 7, 0.5f, true);
            }

            if (!options.keyShift.isUnbound()) {
                if (options.keyShift.isDown())
                    graphics.fill(graphics.guiWidth() / 2 - 73 + 18, graphics.guiHeight() - 50, graphics.guiWidth() / 2 - 47 + 18, graphics.guiHeight() - 23, 0x66000000);
                renderKey(graphics, options.keyShift, graphics.guiWidth() / 2 - 70 + 3 + 18, graphics.guiHeight() - 50 + 3, 14, 0xFFFFFFFF, 0xFF000000);
                renderLabel(graphics, "Move Down", graphics.guiWidth() / 2 - 75 + 18, graphics.guiHeight() - 30, 30, 7, 0.5f, true);
            }

            if (!options.keySprint.isUnbound()) {
                graphics.fill(graphics.guiWidth() / 2 - 99 + 18, graphics.guiHeight() - 50, graphics.guiWidth() / 2 - 77 + 18, graphics.guiHeight() - 23, 0x55000000);
                renderKey(graphics, options.keySprint, graphics.guiWidth() / 2 - 98 + 3 + 18, graphics.guiHeight() - 50 + 3, 14, 0xFF00FF39, 0xFF000000);
                renderLabel(graphics, "Detach", graphics.guiWidth() / 2 - 99 + 18, graphics.guiHeight() - 30, 22, 7, 0.5f, true);
            }
        } else {
            if (!options.keyJump.isUnbound()) {
                graphics.fill(graphics.guiWidth() / 2 - 20 - 2, graphics.guiHeight() - 50, graphics.guiWidth() / 2 - 2, graphics.guiHeight() - 23, 0x55000000); // width = 20
                renderKey(graphics, options.keyJump, graphics.guiWidth() / 2 - 20 + 3 - 2, graphics.guiHeight() - 50 + 3, 14, 0xFF00FBFF, 0xFF000000);
                renderLabel(graphics, "Climb", graphics.guiWidth() / 2 - 20 - 2, graphics.guiHeight() - 30, 20, 7, 0.5f, true);
            }
        }
        if (!options.keyShift.isUnbound()) {
            graphics.fill(graphics.guiWidth() / 2 + 2, graphics.guiHeight() - 50, graphics.guiWidth() / 2 + 20 + 2, graphics.guiHeight() - 23, options.keyShift.isDown() ? 0xAA000000 : 0x55000000); // width = 56
            renderKey(graphics, options.keyShift, graphics.guiWidth() / 2 + 3 + 2, graphics.guiHeight() - 50 + 3, 14, 0xFFFF79E3, 0xFF000000);
            renderLabel(graphics, "Crouch", graphics.guiWidth() / 2 + 2, graphics.guiHeight() - 30, 20, 7, 0.5f, true);
        }
    }
    private static void freeCamPanel(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Options options) {
        graphics.fill(graphics.guiWidth()/2-graphics.guiWidth()/5, graphics.guiHeight()-20, graphics.guiWidth()/2+graphics.guiWidth()/5, graphics.guiHeight(), 0xBB000000);
        graphics.fill(graphics.guiWidth()/2-graphics.guiWidth()/5, graphics.guiHeight() - 50, graphics.guiWidth()/2-graphics.guiWidth()/5+46, graphics.guiHeight() - 23, 0xBB000000);

        if (!Keybinds.freeCam.isUnbound()) {
            renderLabel(graphics, "Free Movement", graphics.guiWidth() / 2 - graphics.guiWidth() / 5, graphics.guiHeight() - 30, 46, 7, 0.5f, false);
            graphics.fill(graphics.guiWidth() / 2 - graphics.guiWidth() / 5 + 3, graphics.guiHeight() - 50 + 3, graphics.guiWidth() / 2 - graphics.guiWidth() / 5 + 26, graphics.guiHeight() - 33, ChameleonOrbitCamera.getInstance().isInFreeCam() ? 0xFF05c900 : 0xFFAF0B0A);
            renderKey(graphics, Keybinds.freeCam, graphics.guiWidth() / 2 - graphics.guiWidth() / 5 + 3 + 1 + (ChameleonOrbitCamera.getInstance().isInFreeCam() ? 9 : 0), graphics.guiHeight() - 50 + 3 + 1, 12, Keybinds.freeCam.isDown() ? 0xFFAAAAAA : 0xFFFFFFFF, ChameleonOrbitCamera.getInstance().isInFreeCam() ? 0xFF05A501 : 0xFFAF0B0A);
        }

        String spectatingWho = ChameleonOrbitCamera.getInstance().spectateWho==null?(Minecraft.getInstance().player==null?"Self":Minecraft.getInstance().player.getName().getString()):ChameleonOrbitCamera.getInstance().spectateWho.getName().getString();
        renderLabel(graphics, spectatingWho, graphics.guiWidth()/2-graphics.guiWidth()/5, graphics.guiHeight()-20, graphics.guiWidth()*2/5, 20, 1.6f, false);

        if (!options.keyAttack.isUnbound())
            renderKey(graphics, options.keyAttack, graphics.guiWidth()/2-graphics.guiWidth()/5+3, graphics.guiHeight()-20+2, 16, options.keyAttack.isDown()?0xFF888888:0xFFFFFFFF, 0xFF000000);
        if (!options.keyUse.isUnbound())
        renderKey(graphics, options.keyUse, graphics.guiWidth()/2+graphics.guiWidth()/5-3-16, graphics.guiHeight()-20+2, 16, options.keyUse.isDown()?0xFF888888:0xFFFFFFFF, 0xFF000000);

        String freeCamLabel = "Free Camera";
        int freeCamWidth = graphics.guiWidth()/2 - graphics.guiWidth()/5 - 5;
        float freeCamScale = Math.min(2f, freeCamWidth / (float) Minecraft.getInstance().font.width(freeCamLabel));
        renderLabel(graphics, freeCamLabel, 5, graphics.guiHeight()-50, freeCamWidth, 50, freeCamScale, false);
    }
    private static void rightSideKeyBinds(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Options options) {
        if (!Keybinds.whistle.isUnbound()) {
            graphics.fill(graphics.guiWidth() - 45, graphics.guiHeight() / 2 - 57, graphics.guiWidth() - 5, graphics.guiHeight() / 2 - 22, 0x99a35c1a);
            renderKey(graphics, Keybinds.whistle, graphics.guiWidth() - 45 + 4, graphics.guiHeight() / 2 - 57 + 4, 14, Keybinds.whistle.isDown() ? 0xFFc28708 : 0xFFFFBC00, 0xFF8a4e16);
            graphics.fill(graphics.guiWidth() - 45 + 4 + 17, graphics.guiHeight() / 2 - 57 + 4 - 1, graphics.guiWidth() - 45 + 4 + 17 + 17, graphics.guiHeight() / 2 - 57 + 4 + 16, 0x77000000);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, MCChameleon.id("actions/whistle"), graphics.guiWidth() - 45 + 4 + 17 + 1, graphics.guiHeight() / 2 - 57 + 4, 15, 15);
            renderLabel(graphics, "Taunt", graphics.guiWidth() - 45 + 3, graphics.guiHeight() / 2 - 22 - 13, 35, 10, 0.8f, true);
        }

        if (!Keybinds.openPoseScreen.isUnbound()) {
            graphics.fill(graphics.guiWidth() - 45, graphics.guiHeight() / 2 - 20, graphics.guiWidth() - 5, graphics.guiHeight() / 2 + 15, 0x99196609);
            renderKey(graphics, Keybinds.openPoseScreen, graphics.guiWidth() - 45 + 4, graphics.guiHeight() / 2 - 20 + 4, 14, Keybinds.openPoseScreen.isDown() ? 0xFFAAAAAA : 0xFFFFFFFF, 0xFF134f06);
            graphics.fill(graphics.guiWidth() - 45 + 4 + 17, graphics.guiHeight() / 2 - 20 + 4 - 1, graphics.guiWidth() - 45 + 4 + 17 + 17, graphics.guiHeight() / 2 - 20 + 4 + 16, 0x77000000);
            graphics.outline(graphics.guiWidth() - 45 + 4 + 17, graphics.guiHeight() / 2 - 20 + 4 - 1, 17, 17, 0xFFFFFFFF);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, MCChameleon.id("actions/pose"), graphics.guiWidth() - 45 + 4 + 17 + 1, graphics.guiHeight() / 2 - 20 + 4, 15, 15);
            renderLabel(graphics, "Pose", graphics.guiWidth() - 45 + 3, graphics.guiHeight() / 2 + 15 - 13, 35, 10, 0.8f, true);
        }

        if (!Keybinds.openPaintScreen.isUnbound()) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, MCChameleon.id("backgrounds/paint"), graphics.guiWidth() - 45, graphics.guiHeight() / 2 + 17, 40, 35, 0x99888888);
            renderKey(graphics, Keybinds.openPaintScreen, graphics.guiWidth() - 45 + 4, graphics.guiHeight() / 2 + 17 + 4, 14, Keybinds.openPaintScreen.isDown() ? 0xFFAAAAAA : 0xFFFFFFFF, 0xFF6a216e);
            graphics.fill(graphics.guiWidth() - 45 + 4 + 17, graphics.guiHeight() / 2 + 17 + 4 - 1, graphics.guiWidth() - 45 + 4 + 17 + 17, graphics.guiHeight() / 2 + 17 + 4 + 16, 0x77000000);
            graphics.outline(graphics.guiWidth() - 45 + 4 + 17, graphics.guiHeight() / 2 + 17 + 4 - 1, 17, 17, 0xFFFFFFFF);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, MCChameleon.id("actions/paint"), graphics.guiWidth() - 45 + 4 + 17 + 1, graphics.guiHeight() / 2 + 17 + 4, 15, 15);
            renderLabel(graphics, "Paint", graphics.guiWidth() - 45 + 3, graphics.guiHeight() / 2 + 52 - 13, 35, 10, 0.8f, true);
        }

        if (!Keybinds.toggleNameplate.isUnbound()) {
            graphics.fill(graphics.guiWidth() - 30, graphics.guiHeight() / 2 + 64, graphics.guiWidth() - 7, graphics.guiHeight() / 2 + 78, MCChameleonClient.namePlatesDisplay ? 0xFF05c900 : 0xFFAF0B0A);
            renderKey(graphics, Keybinds.toggleNameplate, graphics.guiWidth() - 30 + (MCChameleonClient.namePlatesDisplay ? 9 : 0) + 1, graphics.guiHeight() / 2 + 64 + 1, 12, Keybinds.toggleNameplate.isDown() ? 0xFFAAAAAA : 0xFFFFFFFF, MCChameleonClient.namePlatesDisplay ? 0xFF05A501 : 0xFFAF0B0A);
            renderLabel(graphics, "Toggle Nameplate Display", graphics.guiWidth() - 70, graphics.guiHeight() / 2 + 83, 65, 6, 0.5f, false);
        }

        if (!options.keyChat.isUnbound()) {
            graphics.fill(graphics.guiWidth()-20-2, graphics.guiHeight()/2-77-2, graphics.guiWidth()-20+14, graphics.guiHeight()/2-77+14, 0x55000000);
            renderKey(graphics, options.keyChat, graphics.guiWidth()-20, graphics.guiHeight()/2-77, 12, options.keyChat.isDown()?0xFFAAAAAA:0xFFFFFFFF,0xFF000000);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, MCChameleon.id("actions/chat"), graphics.guiWidth()-20-15, graphics.guiHeight()/2-77+1, 11,11);
        }

        VoiceChatAccess.renderGui(graphics, deltaTracker);
    }

    public static void paintScreenControls(GuiGraphicsExtractor graphics, Options options, boolean space, boolean alt, boolean left, boolean right, boolean middle) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, MCChameleon.id("backgrounds/paint"), 5, graphics.guiHeight()/2, 80, 60, 0x77888888);
        graphics.fill(5+4, graphics.guiHeight()/2+4, 5+20+4, graphics.guiHeight()/2+20+4, 0x77000000);
        graphics.fill(5+4+25, graphics.guiHeight()/2+4, 5+4+25+16, graphics.guiHeight()/2+4+16, 0x77000000);
        renderKey(graphics, "space", 5+4+25+1, graphics.guiHeight()/2+4+1, 14, space?0xFFad720c:0xFFFFDC08, 0xFF000000);
        renderLabel(graphics, "Eyedropper (hold)", 5+4+25+4, graphics.guiHeight()/2+4+16, 40, 8, 0.5f, false);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, MCChameleon.id("paint/colour_picker"), 5+4+2, graphics.guiHeight()/2+4+2, 16, 16);

        graphics.fill(5+4, graphics.guiHeight()/2+4+30, 5+20+4, graphics.guiHeight()/2+20+4+30, 0x77000000);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, MCChameleon.id("paint/brush_size"), 5+4+2, graphics.guiHeight()/2+4+2+30, 16, 16);
        graphics.fill(5+4+25, graphics.guiHeight()/2+4+30, 5+4+25+14, graphics.guiHeight()/2+4+14+30, 0x77000000);
        graphics.fill(5+4+25+21, graphics.guiHeight()/2+4+30, 5+4+25+14+21, graphics.guiHeight()/2+4+14+30, 0x77000000);
        renderPlus(graphics, 5+4+25+14+2-2+1, graphics.guiHeight()/2+4+30+3, 5, 0xFFFFFFFF);
        renderKey(graphics, "mouse/right", 5+4+25+2, graphics.guiHeight()/2+4+30+2, 10, right&&!alt?0xFF888888:0xFFFFFFFF, 0xFF000000);
        renderKey(graphics, "mouse/move_horizontal", 5+4+25+21+2, graphics.guiHeight()/2+4+30+2, 10, 0xFFFFFFFF, 0xFF000000);
        renderLabel(graphics, "Brush Size", 5+4+15+4, graphics.guiHeight()/2+4+30+16, 40, 8, 0.5f, false);

        //

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, MCChameleon.id("backgrounds/paint"), 5, graphics.guiHeight()/2+65, 90, 30, 0x77888888);
        graphics.fill(5+2, graphics.guiHeight()/2+4+30+30+3,5+2+12, graphics.guiHeight()/2+4+30+30+3+12, 0x77000000);
        graphics.fill(5+2, graphics.guiHeight()/2+4+30+30+3+14,5+2+12, graphics.guiHeight()/2+4+30+30+3+12+14, 0x77000000);
        renderPlus(graphics, 5+2+12+1, graphics.guiHeight()/2+4+30+30+3+2, 5, 0xFFFFFFFF);
        renderPlus(graphics, 5+2+12+1, graphics.guiHeight()/2+4+30+30+3+2+14, 5, 0xFFFFFFFF);
        graphics.fill(5+2+19, graphics.guiHeight()/2+4+30+30+3,5+2+12+19, graphics.guiHeight()/2+4+30+30+3+12, 0x77000000);
        graphics.fill(5+2+19, graphics.guiHeight()/2+4+30+30+3+14,5+2+12+19, graphics.guiHeight()/2+4+30+30+3+12+14, 0x77000000);
        renderPlus(graphics, 5+2+12+1+19, graphics.guiHeight()/2+4+30+30+3+2+14, 5, 0xFFFFFFFF);
        graphics.fill(5+2+19+19, graphics.guiHeight()/2+4+30+30+3+14,5+2+12+19+19, graphics.guiHeight()/2+4+30+30+3+12+14, 0x77000000);
        renderKey(graphics, "mouse/middle", 5+2+1, graphics.guiHeight()/2+4+30+30+3+1, 10, middle?0xFF888888:0xFFFFFFFF, 0xFF000000);
        renderKey(graphics, "mouse/move", 5+2+1+19, graphics.guiHeight()/2+4+30+30+3+1, 10, 0xFFFFFFFF, 0xFF000000);
        renderKey(graphics, "left alt", 5+2+1, graphics.guiHeight()/2+4+30+30+3+1+14, 10, alt&&left?0xFF888888:0xFFFFFFFF, 0xFF000000);
        renderKey(graphics, "mouse/left", 5+2+1+19, graphics.guiHeight()/2+4+30+30+3+1+14, 10, alt&&left?0xFF888888:0xFFFFFFFF, 0xFF000000);
        renderKey(graphics, "mouse/move", 5+2+1+19+19, graphics.guiHeight()/2+4+30+30+3+1+14, 10, 0xFFFFFFFF, 0xFF000000);
        renderLabel(graphics, "Camera Rotation", 5+2+12+19+19, graphics.guiHeight()/2+65, 38, 30, 0.5f, false);

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, MCChameleon.id("backgrounds/paint"), 5, graphics.guiHeight()/2+65+35, 90, 30, 0x77888888);
        graphics.fill(5+2, graphics.guiHeight()/2+4+30+30+3+35,5+2+12, graphics.guiHeight()/2+4+30+30+3+12+35, 0x77000000);
        graphics.fill(5+2, graphics.guiHeight()/2+4+30+30+3+14+35,5+2+12, graphics.guiHeight()/2+4+30+30+3+12+14+35, 0x77000000);
        renderPlus(graphics, 5+2+12+1, graphics.guiHeight()/2+4+30+30+3+2+14+35, 5, 0xFFFFFFFF);
        graphics.fill(5+2+19, graphics.guiHeight()/2+4+30+30+3+14+35,5+2+12+19, graphics.guiHeight()/2+4+30+30+3+12+14+35, 0x77000000);
        renderPlus(graphics, 5+2+12+1+19, graphics.guiHeight()/2+4+30+30+3+2+14+35, 5, 0xFFFFFFFF);
        graphics.fill(5+2+19+19, graphics.guiHeight()/2+4+30+30+3+14+35,5+2+12+19+19, graphics.guiHeight()/2+4+30+30+3+12+14+35, 0x77000000);
        renderKey(graphics, "mouse/scroll", 5+2+1, graphics.guiHeight()/2+4+30+30+3+1+35, 10, 0xFFFFFFFF, 0xFF000000);
        renderKey(graphics, "left alt", 5+2+1, graphics.guiHeight()/2+4+30+30+3+1+14+35, 10, alt&&right?0xFF888888:0xFFFFFFFF, 0xFF000000);
        renderKey(graphics, "mouse/right", 5+2+1+19, graphics.guiHeight()/2+4+30+30+3+1+14+35, 10, right&&alt?0xFF888888:0xFFFFFFFF, 0xFF000000);
        renderKey(graphics, "mouse/move", 5+2+1+19+19, graphics.guiHeight()/2+4+30+30+3+1+14+35, 10, 0xFFFFFFFF, 0xFF000000);
        renderLabel(graphics, "Zoom", 5+2+12+19+19, graphics.guiHeight()/2+65+35, 38, 30, 0.5f, false);
    }

    public static void renderKey(GuiGraphicsExtractor graphics, KeyMapping key, int x, int y, int width, int colour, int textColour) {
        if (KeyMappingHelper.getBoundKeyOf(key).getType().equals(InputConstants.Type.MOUSE)) {
            Identifier texture = switch (KeyMappingHelper.getBoundKeyOf(key).getValue()) {
                case GLFW.GLFW_MOUSE_BUTTON_LEFT -> MCChameleon.id("mouse/left");
                case GLFW.GLFW_MOUSE_BUTTON_RIGHT -> MCChameleon.id("mouse/right");
                case GLFW.GLFW_MOUSE_BUTTON_MIDDLE -> MCChameleon.id("mouse/middle");
                default -> MCChameleon.id("mouse/none");
            };
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, texture, x, y, width, width, colour);
            return;
        }

        Component keyName = key.getTranslatedKeyMessage();
        String keyString = keyName.getString().toLowerCase().replace("left ", "").replace("right ", "").replace("control","ctrl");
        Component label = Component.literal(keyString).withStyle(keyName.getStyle().withFont(ChameleonFonts.FONT_FIVE));
        int labelWidth = Minecraft.getInstance().font.width(label);
        float textScale = labelWidth>width-2?(float)(width-2)/labelWidth:1;

        //colour
        int height = keyString.length()>1?(int) (Math.round((width*textScale+2)*0.2)/0.2f):(int)(width*textScale);
        int yOffset = (width - height) / 2;
        graphics.fill(x, y + yOffset, x + width, y + height + yOffset, colour);

        float scaledTextWidth = labelWidth * textScale;
        float centreX = x + (width - scaledTextWidth) / 2f;
        float centreY = y + yOffset + (height - Minecraft.getInstance().font.lineHeight * textScale) / 2f;

        graphics.pose().pushMatrix();
        graphics.pose().scale(textScale);
        graphics.pose().translate(centreX / textScale, centreY / textScale);
        graphics.text(Minecraft.getInstance().font, label, 0, 0, textColour, false);
        graphics.pose().popMatrix();
    }

    private static void renderKey(GuiGraphicsExtractor graphics, String key, int x, int y, int width, int colour, int textColour) {
        if (key.contains("mouse/")) {
            Identifier texture = MCChameleon.id(key);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, texture, x, y, width, width, colour);
            return;
        }

        Component keyName = Component.nullToEmpty(key);
        String keyString = keyName.getString().toLowerCase().replace("left ", "").replace("right ", "").replace("control","ctrl");
        Component label = Component.literal(keyString).withStyle(keyName.getStyle().withFont(ChameleonFonts.FONT_FIVE));
        int labelWidth = Minecraft.getInstance().font.width(label);
        float textScale = labelWidth>width-2?(float)(width-2)/labelWidth:1;

        //colour
        int height = keyString.length()>1?(int) (Math.round((width*textScale+2)*0.2)/0.2f):(int)(width*textScale);
        int yOffset = (width - height) / 2;
        graphics.fill(x, y + yOffset, x + width, y + height + yOffset, colour);

        float scaledTextWidth = labelWidth * textScale;
        float centreX = x + (width - scaledTextWidth) / 2f;
        float centreY = y + yOffset + (height - Minecraft.getInstance().font.lineHeight * textScale) / 2f;

        graphics.pose().pushMatrix();
        graphics.pose().scale(textScale);
        graphics.pose().translate(centreX / textScale, centreY / textScale);
        graphics.text(Minecraft.getInstance().font, label, 0, 0, textColour, false);
        graphics.pose().popMatrix();
    }

    private static void renderLabel(GuiGraphicsExtractor graphics, String label, int x, int y, int width, int height, float textScale, boolean backdrop) {
        if (backdrop) graphics.fill(x, y, x+width, y+height, 0x88000000);

        int textWidth = Minecraft.getInstance().font.width(label);
        int textHeight = Minecraft.getInstance().font.lineHeight;

        graphics.pose().pushMatrix();
        graphics.pose().scale(textScale);
        graphics.pose().translate((x+width/2f-textWidth*textScale/2f)/textScale, (y+height/2f-textHeight*textScale/2f)/textScale);
        graphics.text(Minecraft.getInstance().font, label, 0,0, 0xFFFFFFFF);
        graphics.pose().popMatrix();
    }

    private static void renderPlus(GuiGraphicsExtractor graphics, int x, int y, int size, int colour) {
        graphics.verticalLine(x+size/2, y, y+size+1, colour);
        graphics.horizontalLine(x, x+size-1, y+size/2+1, colour);
    }
}
