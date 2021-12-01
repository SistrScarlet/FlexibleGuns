package net.sistr.flexibleguns.util

import net.minecraft.network.PacketByteBuf

interface FGCustomPacketEntity {
    fun writeCustomPacket(packet: PacketByteBuf?)
    fun readCustomPacket(packet: PacketByteBuf?)
}