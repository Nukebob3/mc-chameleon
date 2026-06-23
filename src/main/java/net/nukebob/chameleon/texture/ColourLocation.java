package net.nukebob.chameleon.texture;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class ColourLocation {
    public record ColLoc(int colour, int location) {
        public static final StreamCodec<ByteBuf, ColLoc> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.INT, ColLoc::colour,
                ByteBufCodecs.INT, ColLoc::location,
                ColLoc::new
        );
    }
}
