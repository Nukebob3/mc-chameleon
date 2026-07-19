package net.nukebob.chameleon.networking;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.nukebob.chameleon.MCChameleon;
import net.nukebob.chameleon.gameplay.MissedSpot;
import net.nukebob.chameleon.gameplay.Poses;
import net.nukebob.chameleon.texture.ColourLocation;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Payloads {
    public record ClientBoundUpdatePixelsPayload(UUID uuid, int[] pixels) implements CustomPacketPayload {
        public static final Identifier UPDATE_PIXELS_PAYLOAD_ID = MCChameleon.id("update_pixels");

        public static final CustomPacketPayload.Type<ClientBoundUpdatePixelsPayload> TYPE = new CustomPacketPayload.Type<>(UPDATE_PIXELS_PAYLOAD_ID);

        public static final StreamCodec<RegistryFriendlyByteBuf, ClientBoundUpdatePixelsPayload> CODEC = StreamCodec.composite(
                UUIDUtil.STREAM_CODEC,
                ClientBoundUpdatePixelsPayload::uuid,
                ByteBufCodecs.INT.apply(ByteBufCodecs.list(1504)).map(
                        list -> list.stream().mapToInt(Integer::intValue).toArray(),
                        array -> Arrays.stream(array).boxed().toList()
                ),
                ClientBoundUpdatePixelsPayload::pixels,
                ClientBoundUpdatePixelsPayload::new
        );


        @Override
        public @NonNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

    }

    public record ClientBoundUpdateSpecificPixelsPayload(UUID uuid, ColourLocation.ColLoc[] pixels) implements CustomPacketPayload {
        public static final Identifier UPDATE_SPECIFIC_PIXELS_PAYLOAD_ID = MCChameleon.id("update_specific_pixels");

        public static final CustomPacketPayload.Type<ClientBoundUpdateSpecificPixelsPayload> TYPE = new CustomPacketPayload.Type<>(UPDATE_SPECIFIC_PIXELS_PAYLOAD_ID);

        public static final StreamCodec<RegistryFriendlyByteBuf, ClientBoundUpdateSpecificPixelsPayload> CODEC = StreamCodec.composite(
                UUIDUtil.STREAM_CODEC,
                ClientBoundUpdateSpecificPixelsPayload::uuid,

                ColourLocation.ColLoc.STREAM_CODEC.apply(ByteBufCodecs.list()).map(
                        list -> list.toArray(new ColourLocation.ColLoc[0]),
                        Arrays::asList
                ),
                ClientBoundUpdateSpecificPixelsPayload::pixels,
                ClientBoundUpdateSpecificPixelsPayload::new
        );


        @Override
        public @NonNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ClientBoundClearPixelsPayload(UUID uuid) implements CustomPacketPayload {
        public static final Identifier CLEAR_PIXELS_PAYLOAD_ID = MCChameleon.id("clear_pixels");

        public static final CustomPacketPayload.Type<ClientBoundClearPixelsPayload> TYPE = new CustomPacketPayload.Type<>(CLEAR_PIXELS_PAYLOAD_ID);

        public static final StreamCodec<RegistryFriendlyByteBuf, ClientBoundClearPixelsPayload> CODEC = StreamCodec.composite(
                UUIDUtil.STREAM_CODEC,
                ClientBoundClearPixelsPayload::uuid,
                ClientBoundClearPixelsPayload::new
        );


        @Override
        public @NonNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ClientBoundPosePayload(UUID uuid, int pose) implements CustomPacketPayload {
        public static final Identifier POSE_PAYLOAD_ID = MCChameleon.id("pose");

        public static final CustomPacketPayload.Type<ClientBoundPosePayload> TYPE = new CustomPacketPayload.Type<>(POSE_PAYLOAD_ID);

        public static final StreamCodec<RegistryFriendlyByteBuf, ClientBoundPosePayload> CODEC = StreamCodec.composite(
                UUIDUtil.STREAM_CODEC,
                ClientBoundPosePayload::uuid,
                ByteBufCodecs.INT,
                ClientBoundPosePayload::pose,
                ClientBoundPosePayload::new
        );

        @Override
        public @NonNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ClientBoundClearPosesPayload() implements CustomPacketPayload {
        public static final Identifier CLEAR_POSES_PAYLOAD_ID = MCChameleon.id("clear_poses");

        public static final CustomPacketPayload.Type<ClientBoundClearPosesPayload> TYPE = new CustomPacketPayload.Type<>(CLEAR_POSES_PAYLOAD_ID);

        public static final StreamCodec<RegistryFriendlyByteBuf, ClientBoundClearPosesPayload> CODEC = StreamCodec.unit(new ClientBoundClearPosesPayload());

        @Override
        public @NonNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ClientBoundShotPayload(Vec3 start, Vec3 end) implements CustomPacketPayload {
        public static final Identifier SHOT_PAYLOAD_ID = MCChameleon.id("shot");

        public static final CustomPacketPayload.Type<ClientBoundShotPayload> TYPE = new CustomPacketPayload.Type<>(SHOT_PAYLOAD_ID);

        public static final StreamCodec<RegistryFriendlyByteBuf, ClientBoundShotPayload> CODEC = StreamCodec.composite(
                Vec3.STREAM_CODEC,
                ClientBoundShotPayload::start,
                Vec3.STREAM_CODEC,
                ClientBoundShotPayload::end,
                ClientBoundShotPayload::new
        );

        @Override
        public @NonNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ClientBoundGameHudUpdatePayload(int time, int maxTime, int whistle, String subtitle) implements CustomPacketPayload {
        public static final Identifier GAME_HUD_UPDATE_ID = MCChameleon.id("game_hud_update");

        public static final CustomPacketPayload.Type<ClientBoundGameHudUpdatePayload> TYPE = new CustomPacketPayload.Type<>(GAME_HUD_UPDATE_ID);

        public static final StreamCodec<RegistryFriendlyByteBuf, ClientBoundGameHudUpdatePayload> CODEC = StreamCodec.composite(
                ByteBufCodecs.INT,
                ClientBoundGameHudUpdatePayload::time,
                ByteBufCodecs.INT,
                ClientBoundGameHudUpdatePayload::maxTime,
                ByteBufCodecs.INT,
                ClientBoundGameHudUpdatePayload::whistle,
                ByteBufCodecs.STRING_UTF8,
                ClientBoundGameHudUpdatePayload::subtitle,
                ClientBoundGameHudUpdatePayload::new
        );

        @Override
        public @NonNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ClientBoundGameHudPlayersPayload(int hiders, int seekers) implements CustomPacketPayload {
        public static final Identifier GAME_HUD_PLAYERS_ID = MCChameleon.id("game_hud_players");

        public static final CustomPacketPayload.Type<ClientBoundGameHudPlayersPayload> TYPE = new CustomPacketPayload.Type<>(GAME_HUD_PLAYERS_ID);

        public static final StreamCodec<RegistryFriendlyByteBuf, ClientBoundGameHudPlayersPayload> CODEC = StreamCodec.composite(
                ByteBufCodecs.INT,
                ClientBoundGameHudPlayersPayload::hiders,
                ByteBufCodecs.INT,
                ClientBoundGameHudPlayersPayload::seekers,
                ClientBoundGameHudPlayersPayload::new
        );

        @Override
        public @NonNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ClientBoundMissedSpotPayload(int timer, List<MissedSpot.MissedSpotEntry> rankings) implements CustomPacketPayload {
        public static final Identifier MISSED_SPOT = MCChameleon.id("missed_spot");

        public static final CustomPacketPayload.Type<ClientBoundMissedSpotPayload> TYPE = new CustomPacketPayload.Type<>(MISSED_SPOT);

        public static final StreamCodec<RegistryFriendlyByteBuf, ClientBoundMissedSpotPayload> CODEC = StreamCodec.composite(
                ByteBufCodecs.INT,
                ClientBoundMissedSpotPayload::timer,
                MissedSpot.MissedSpotEntry.STREAM_CODEC.apply(ByteBufCodecs.list()),
                ClientBoundMissedSpotPayload::rankings,
                ClientBoundMissedSpotPayload::new
        );

        @Override
        public @NonNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ClientBoundMissedSpotEnableHunterPayload(boolean enabled) implements CustomPacketPayload {
        public static final Identifier MISSED_SPOT = MCChameleon.id("missed_spot_enabled_hunter");

        public static final CustomPacketPayload.Type<ClientBoundMissedSpotEnableHunterPayload> TYPE = new CustomPacketPayload.Type<>(MISSED_SPOT);

        public static final StreamCodec<RegistryFriendlyByteBuf, ClientBoundMissedSpotEnableHunterPayload> CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL,
                ClientBoundMissedSpotEnableHunterPayload::enabled,
                ClientBoundMissedSpotEnableHunterPayload::new
        );

        @Override
        public @NonNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    //

    public record ServerBoundUpdatePixelsPayload(int[] pixels) implements CustomPacketPayload {
        public static final Identifier UPDATE_PIXELS_PAYLOAD = MCChameleon.id("update_pixels_client");

        public static final CustomPacketPayload.Type<ServerBoundUpdatePixelsPayload> TYPE = new CustomPacketPayload.Type<>(UPDATE_PIXELS_PAYLOAD);

        public static final StreamCodec<RegistryFriendlyByteBuf, ServerBoundUpdatePixelsPayload> CODEC = StreamCodec.composite(
                ByteBufCodecs.INT.apply(ByteBufCodecs.list(1504)).map(
                        list -> list.stream().mapToInt(Integer::intValue).toArray(),
                        array -> Arrays.stream(array).boxed().toList()
                ),
                ServerBoundUpdatePixelsPayload::pixels,
                ServerBoundUpdatePixelsPayload::new
        );


        @Override
        public @NonNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ServerBoundUpdateSpecificPixelsPayload(ColourLocation.ColLoc[] pixels) implements CustomPacketPayload {
        public static final Identifier UPDATE_SPECIFIC_PIXELS_PAYLOAD_ID = MCChameleon.id("update_specific_pixels_client");

        public static final CustomPacketPayload.Type<ServerBoundUpdateSpecificPixelsPayload> TYPE = new CustomPacketPayload.Type<>(UPDATE_SPECIFIC_PIXELS_PAYLOAD_ID);

        public static final StreamCodec<RegistryFriendlyByteBuf, ServerBoundUpdateSpecificPixelsPayload> CODEC = StreamCodec.composite(
                ColourLocation.ColLoc.STREAM_CODEC.apply(ByteBufCodecs.list()).map(
                        list -> list.toArray(new ColourLocation.ColLoc[0]),
                        Arrays::asList
                ),
                ServerBoundUpdateSpecificPixelsPayload::pixels,
                ServerBoundUpdateSpecificPixelsPayload::new
        );


        @Override
        public @NonNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ServerBoundPosePayload(Poses pose) implements CustomPacketPayload {
        public static final Identifier POSE_PAYLOAD_ID = MCChameleon.id("pose_client");

        public static final CustomPacketPayload.Type<ServerBoundPosePayload> TYPE = new CustomPacketPayload.Type<>(POSE_PAYLOAD_ID);

        public static final StreamCodec<RegistryFriendlyByteBuf, ServerBoundPosePayload> CODEC = StreamCodec.composite(
                ByteBufCodecs.INT,
                payload -> payload.pose() == null ? -1 : payload.pose().ordinal(),
                id -> new ServerBoundPosePayload(id == -1 ? null : Poses.values()[id])
        );

        @Override
        public @NonNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ServerBoundWhistle() implements CustomPacketPayload {
        public static final Identifier WHISTLE_PAYLOAD_ID = MCChameleon.id("whistle");

        public static final CustomPacketPayload.Type<ServerBoundWhistle> TYPE = new CustomPacketPayload.Type<>(WHISTLE_PAYLOAD_ID);

        public static  final  StreamCodec<RegistryFriendlyByteBuf, ServerBoundWhistle> CODEC = StreamCodec.unit(new ServerBoundWhistle());

        @Override
        public @NonNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ServerBoundShotPayload(Vec3 target, UUID hit) implements CustomPacketPayload {
        public static final Identifier SHOT_PAYLOAD_ID = MCChameleon.id("shot_client");

        public static final CustomPacketPayload.Type<ServerBoundShotPayload> TYPE = new CustomPacketPayload.Type<>(SHOT_PAYLOAD_ID);

        public static final StreamCodec<RegistryFriendlyByteBuf, ServerBoundShotPayload> CODEC = StreamCodec.composite(
                Vec3.STREAM_CODEC,
                ServerBoundShotPayload::target,
                UUIDUtil.STREAM_CODEC,
                ServerBoundShotPayload::hit,
                ServerBoundShotPayload::new
        );

        @Override
        public @NonNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public static void register() {
        //s2c
        PayloadTypeRegistry.clientboundPlay().register(ClientBoundUpdatePixelsPayload.TYPE, ClientBoundUpdatePixelsPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ClientBoundUpdateSpecificPixelsPayload.TYPE, ClientBoundUpdateSpecificPixelsPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ClientBoundClearPixelsPayload.TYPE, ClientBoundClearPixelsPayload.CODEC);

        PayloadTypeRegistry.clientboundPlay().register(ClientBoundPosePayload.TYPE, ClientBoundPosePayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ClientBoundClearPosesPayload.TYPE, ClientBoundClearPosesPayload.CODEC);

        PayloadTypeRegistry.clientboundPlay().register(ClientBoundShotPayload.TYPE, ClientBoundShotPayload.CODEC);

        PayloadTypeRegistry.clientboundPlay().register(ClientBoundGameHudUpdatePayload.TYPE, ClientBoundGameHudUpdatePayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ClientBoundGameHudPlayersPayload.TYPE, ClientBoundGameHudPlayersPayload.CODEC);

        PayloadTypeRegistry.clientboundPlay().register(ClientBoundMissedSpotPayload.TYPE, ClientBoundMissedSpotPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ClientBoundMissedSpotEnableHunterPayload.TYPE, ClientBoundMissedSpotEnableHunterPayload.CODEC);

        //c2s
        PayloadTypeRegistry.serverboundPlay().register(ServerBoundUpdatePixelsPayload.TYPE, ServerBoundUpdatePixelsPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ServerBoundUpdateSpecificPixelsPayload.TYPE, ServerBoundUpdateSpecificPixelsPayload.CODEC);

        PayloadTypeRegistry.serverboundPlay().register(ServerBoundPosePayload.TYPE, ServerBoundPosePayload.CODEC);

        PayloadTypeRegistry.serverboundPlay().register(ServerBoundWhistle.TYPE, ServerBoundWhistle.CODEC);

        PayloadTypeRegistry.serverboundPlay().register(ServerBoundShotPayload.TYPE, ServerBoundShotPayload.CODEC);
    }
}
