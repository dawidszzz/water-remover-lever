package com.example.waterremover.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Pusty pakiet client -> server. Samo jego wysłanie oznacza
 * "przełącz stan włączenia moda". Nie niesie żadnych danych.
 */
public record ToggleModPayload() implements CustomPayload {

    public static final CustomPayload.Id<ToggleModPayload> ID =
            new CustomPayload.Id<>(Identifier.of("waterremover", "toggle"));

    public static final PacketCodec<RegistryByteBuf, ToggleModPayload> CODEC =
            PacketCodec.unit(new ToggleModPayload());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
