package com.example.elementalia.network;

import com.example.elementalia.Elementalia;
import com.example.elementalia.element.Element;
import com.mojang.datafixers.util.Function3;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

/**
 * Sent server → clients when an elemental tome cast is resolved.
 * Carries the origin (eye pos), impact point, RNG seed, and the casting
 * {@link Element} so each client can render the beam and impact effect locally.
 *
 * The {@code seed} value lets every watching client seed the same RNG so
 * randomised particle scatter looks identical across all viewers.
 *
 * {@code element} is encoded as a single byte (the ordinal) — see
 * {@link Element#toByte()} / {@link Element#fromByte(byte)}.
 */
public record BookCastPayload(Vec3 origin, Vec3 impact, int seed, Element element)
        implements CustomPacketPayload {

    public static final Type<BookCastPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Elementalia.MODID, "book_cast"));

    /** Encodes/decodes a {@link Vec3} as three consecutive doubles. */
    private static final StreamCodec<ByteBuf, Vec3> VEC3_STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.DOUBLE, Vec3::x,
                    ByteBufCodecs.DOUBLE, Vec3::y,
                    ByteBufCodecs.DOUBLE, Vec3::z,
                    (Function3<Double, Double, Double, Vec3>) Vec3::new);

    /** Encodes/decodes an {@link Element} as a single byte (its ordinal). */
    private static final StreamCodec<ByteBuf, Element> ELEMENT_STREAM_CODEC =
            ByteBufCodecs.BYTE.map(Element::fromByte, Element::toByte);

    public static final StreamCodec<ByteBuf, BookCastPayload> STREAM_CODEC =
            StreamCodec.composite(
                    VEC3_STREAM_CODEC,    BookCastPayload::origin,
                    VEC3_STREAM_CODEC,    BookCastPayload::impact,
                    ByteBufCodecs.INT,    BookCastPayload::seed,
                    ELEMENT_STREAM_CODEC, BookCastPayload::element,
                    BookCastPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
