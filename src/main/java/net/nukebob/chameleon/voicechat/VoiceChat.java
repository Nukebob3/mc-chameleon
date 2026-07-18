package net.nukebob.chameleon.voicechat;

import de.maxhenkel.voicechat.api.*;
import de.maxhenkel.voicechat.api.events.ClientVoicechatConnectionEvent;
import de.maxhenkel.voicechat.api.events.ClientVoicechatInitializationEvent;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;
import de.maxhenkel.voicechat.voice.client.KeyEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.server.level.ServerPlayer;
import net.nukebob.chameleon.MCChameleon;
import org.jspecify.annotations.Nullable;

public class VoiceChat implements VoicechatPlugin {
    @Nullable
    public static VoicechatServerApi SERVER_API;

    @Nullable
    public static VoicechatClientApi CLIENT_API;

    @Nullable
    public static Group deadPpl;

    @Override
    public void initialize(VoicechatApi api) {
        VoiceChatAccess.INSTALLED = true;
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(VoicechatServerStartedEvent.class, this::serverStarted);
        registration.registerEvent(ClientVoicechatInitializationEvent.class, this::clientStart);
        //registration.registerEvent();
    }

    private void clientStart(ClientVoicechatInitializationEvent event) {
        CLIENT_API = event.getVoicechat();
    }

    private void serverStarted(VoicechatServerStartedEvent event) {
        SERVER_API = event.getVoicechat();

        deadPpl = SERVER_API.groupBuilder()
                .setPersistent(true)
                .setHidden(true)
                .setName("Dead")
                .setType(Group.Type.NORMAL)
                .setPassword("hehe i knew the password and broke in")
                .build();
    }

    protected static void addPlayerToGroup(ServerPlayer player) {
        if (VoiceChat.SERVER_API!=null) {
            VoicechatConnection connection = VoiceChat.SERVER_API.getConnectionOf(player.getUUID());
            if (connection!=null)
                connection.setGroup(VoiceChat.deadPpl);
        }
    }

    protected static boolean isMuted() {
        return CLIENT_API.isMuted();
    }

    protected static boolean isDeafened() {
        return CLIENT_API.isDisabled();
    }

    protected static KeyMapping muteKey() {
        return KeyEvents.KEY_MUTE;
    }

    protected static KeyMapping deafenKey() {
        return KeyEvents.KEY_DISABLE;
    }

    protected static boolean isDisconnected() {
        return CLIENT_API.isDisconnected();
    }

    protected static boolean isGuiHidden() {
        return CLIENT_API.getClientConfig().getBoolean("hide_icons", true);
    }

    @Override
    public String getPluginId() {
        return MCChameleon.MOD_ID;
    }
}
