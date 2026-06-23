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

import java.util.Arrays;
import java.util.UUID;

public class Payloads {
    public record ClientboundUpdatePixelsPayload(UUID uuid, int[] pixels) implements CustomPacketPayload {
        public static final Identifier UPDATE_PIXELS_PAYLOAD_ID = MCChameleon.id("update_pixels");

        public static final CustomPacketPayload.Type<ClientboundUpdatePixelsPayload> TYPE = new CustomPacketPayload.Type<>(UPDATE_PIXELS_PAYLOAD_ID);

        public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundUpdatePixelsPayload> CODEC = StreamCodec.composite(
                UUIDUtil.STREAM_CODEC,
                ClientboundUpdatePixelsPayload::uuid,
                ByteBufCodecs.INT.apply(ByteBufCodecs.list(1632)).map(
                        list -> list.stream().mapToInt(Integer::intValue).toArray(),
                        array -> Arrays.stream(array).boxed().toList()
                ),
                ClientboundUpdatePixelsPayload::pixels,
                ClientboundUpdatePixelsPayload::new
        );


        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

    }

    public record ClientboundUpdateSpecificPixelsPayload(UUID uuid, ColourLocation.ColLoc[] pixels) implements CustomPacketPayload {
        public static final Identifier UPDATE_SPECIFIC_PIXELS_PAYLOAD_ID = MCChameleon.id("update_specific_pixels");

        public static final CustomPacketPayload.Type<ClientboundUpdateSpecificPixelsPayload> TYPE = new CustomPacketPayload.Type<>(UPDATE_SPECIFIC_PIXELS_PAYLOAD_ID);

        public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundUpdateSpecificPixelsPayload> CODEC = StreamCodec.composite(
                UUIDUtil.STREAM_CODEC,
                ClientboundUpdateSpecificPixelsPayload::uuid,

                ColourLocation.ColLoc.STREAM_CODEC.apply(ByteBufCodecs.list()).map(
                        list -> list.toArray(new ColourLocation.ColLoc[0]),
                        array -> Arrays.asList(array)
                ),
                ClientboundUpdateSpecificPixelsPayload::pixels,
                ClientboundUpdateSpecificPixelsPayload::new
        );


        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ClientboundClearPixelsPayload(UUID uuid) implements CustomPacketPayload {
        public static final Identifier CLEAR_PIXELS_PAYLOAD_ID = MCChameleon.id("clear_pixels");

        public static final CustomPacketPayload.Type<ClientboundClearPixelsPayload> TYPE = new CustomPacketPayload.Type<>(CLEAR_PIXELS_PAYLOAD_ID);

        public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundClearPixelsPayload> CODEC = StreamCodec.composite(
                UUIDUtil.STREAM_CODEC,
                ClientboundClearPixelsPayload::uuid,
                ClientboundClearPixelsPayload::new
        );


        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public static void register() {
        PayloadTypeRegistry.clientboundPlay().register(Payloads.ClientboundUpdatePixelsPayload.TYPE, Payloads.ClientboundUpdatePixelsPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(Payloads.ClientboundUpdateSpecificPixelsPayload.TYPE, Payloads.ClientboundUpdateSpecificPixelsPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(Payloads.ClientboundClearPixelsPayload.TYPE, Payloads.ClientboundClearPixelsPayload.CODEC);
    }
}
