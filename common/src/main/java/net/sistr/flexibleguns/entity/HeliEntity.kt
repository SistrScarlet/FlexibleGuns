package net.sistr.flexibleguns.entity

import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.Packet
import net.minecraft.network.PacketByteBuf
import net.minecraft.world.World
import net.sistr.flexibleguns.network.CustomEntitySpawnPacket
import net.sistr.flexibleguns.util.FGCustomPacketEntity

class HeliEntity(type: EntityType<*>?, world: World?) : Entity(type, world), FGCustomPacketEntity {

    override fun initDataTracker() {
    }

    override fun readCustomDataFromNbt(tag: NbtCompound?) {
    }

    override fun writeCustomDataToNbt(tag: NbtCompound?) {
    }

    override fun writeCustomPacket(packet: PacketByteBuf?) {

    }

    override fun readCustomPacket(packet: PacketByteBuf?) {

    }

    override fun createSpawnPacket(): Packet<*> {
        return CustomEntitySpawnPacket.createPacket(this)
    }

}