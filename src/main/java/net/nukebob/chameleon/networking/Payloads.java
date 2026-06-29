package net.nukebob.chameleon.networking;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.nukebob.chameleon.MCChameleon;
import net.nukebob.chameleon.texture.ColourLocation;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
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

    public static void register() {
        PayloadTypeRegistry.clientboundPlay().register(ClientBoundUpdatePixelsPayload.TYPE, ClientBoundUpdatePixelsPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ClientBoundUpdateSpecificPixelsPayload.TYPE, ClientBoundUpdateSpecificPixelsPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ClientBoundClearPixelsPayload.TYPE, ClientBoundClearPixelsPayload.CODEC);

        PayloadTypeRegistry.serverboundPlay().register(ServerBoundUpdatePixelsPayload.TYPE, ServerBoundUpdatePixelsPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ServerBoundUpdateSpecificPixelsPayload.TYPE, ServerBoundUpdateSpecificPixelsPayload.CODEC);
    }
}
