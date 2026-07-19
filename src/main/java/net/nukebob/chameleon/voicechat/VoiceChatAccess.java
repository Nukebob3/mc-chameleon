package net.nukebob.chameleon.voicechat;

import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.server.level.ServerPlayer;
import net.nukebob.chameleon.MCChameleon;
import net.nukebob.chameleon.render.ChameleonHud;

public class VoiceChatAccess {
    public static boolean INSTALLED = false;

    public static void addPlayerToGroup(ServerPlayer player) {
        if (INSTALLED) VoiceChat.addPlayerToGroup(player);
    }
    public static void removePlayerFromGroup(ServerPlayer player) {
        if (INSTALLED) VoiceChat.removePlayerFromGroup(player);
    }

    public static void renderGui(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        if (INSTALLED&&!VoiceChat.isGuiHidden()&&!VoiceChat.isDisconnected()) {
            if (!VoiceChat.muteKey().isUnbound()) {
                graphics.fill(graphics.guiWidth() - 30, graphics.guiHeight() / 2 + 64 + 30, graphics.guiWidth() - 7, graphics.guiHeight() / 2 + 78 + 30, 0x55000000);
                ChameleonHud.renderKey(graphics, VoiceChat.muteKey(), graphics.guiWidth() - 30 + 2, graphics.guiHeight() / 2 + 64 + 2 + 30, 10, VoiceChat.muteKey().isDown() ? 0xFFAAAAAA : 0xFFFFFFFF, 0xFF000000);
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, MCChameleon.id(VoiceChat.isMuted()?"vc/mic_off":"vc/mic"), graphics.guiWidth() - 30 + 2+11, graphics.guiHeight() / 2 + 64 + 2 + 30, 9, 9);
            }
            if (!VoiceChat.deafenKey().isUnbound()) {
                graphics.fill(graphics.guiWidth() - 30, graphics.guiHeight() / 2 + 64 + 30+20, graphics.guiWidth() - 7, graphics.guiHeight() / 2 + 78 + 30+20, 0x55000000);
                ChameleonHud.renderKey(graphics, VoiceChat.deafenKey(), graphics.guiWidth() - 30 + 2, graphics.guiHeight() / 2 + 64 + 2 + 30+20, 10, VoiceChat.deafenKey().isDown() ? 0xFFAAAAAA : 0xFFFFFFFF, 0xFF000000);
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, MCChameleon.id(VoiceChat.isDeafened()?"vc/headphones_off":"vc/headphones"), graphics.guiWidth() - 30 + 2+11, graphics.guiHeight() / 2 + 64 + 2 + 30+20, 9, 9);
            }
        }
    }

    public static void mute() {
        VoiceChat.mute();
    }

    public static void deafen() {
        VoiceChat.deafen();
    }

    public static void paintScreenVoiceChatKeybinds(int key) {
        if (key == KeyMappingHelper.getBoundKeyOf(VoiceChat.muteKey()).getValue()) {
            mute();
        } else if (key == KeyMappingHelper.getBoundKeyOf(VoiceChat.deafenKey()).getValue()) {
            deafen();
        }
    }
}
