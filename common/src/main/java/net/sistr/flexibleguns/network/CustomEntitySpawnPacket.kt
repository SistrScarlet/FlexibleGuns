package net.sistr.flexibleguns.network

import io.netty.buffer.Unpooled
import me.shedaniel.architectury.networking.NetworkManager
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.network.Packet
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import net.sistr.flexibleguns.FlexibleGunsMod
import net.sistr.flexibleguns.util.FGCustomPacketEntity
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.*

object CustomEntitySpawnPacket {
    val ID = Identifier(FlexibleGunsMod.MODID, "entity_spawn")
    private val LOGGER: Logger = LogManager.getLogger()

    fun <T> createPacket(entity: T): Packet<*> where T : Entity, T : FGCustomPacketEntity {
        val buf = PacketByteBuf(Unpooled.buffer())
        buf.writeVarInt(entity.entityId)
        buf.writeUuid(entity.uuid)
        buf.writeVarInt(Registry.ENTITY_TYPE.getRawId(entity.type))
        buf.writeDouble(entity.x)
        buf.writeDouble(entity.y)
        buf.writeDouble(entity.z)
        buf.writeFloat(entity.yaw)
        buf.writeFloat(entity.pitch)
        buf.writeFloat(entity.headYaw)
        val velocity = entity.velocity
        buf.writeDouble(velocity.x)
        buf.writeDouble(velocity.y)
        buf.writeDouble(velocity.z)
        entity.writeCustomPacket(buf)
        return NetworkManager.toPacket(NetworkManager.Side.S2C, ID, buf)
    }

    @Environment(EnvType.CLIENT)
    fun receiveS2CPacket(buf: PacketByteBuf, context: NetworkManager.PacketContext) {
        val id = buf.readVarInt()
        val uuid = buf.readUuid()
        val entityTypeId = buf.readVarInt()
        val x = buf.readDouble()
        val y = buf.readDouble()
        val z = buf.readDouble()
        val yaw = buf.readFloat()
        val pitch = buf.readFloat()
        val headYaw = buf.readFloat()
        val velocityX = buf.readDouble()
        val velocityY = buf.readDouble()
        val velocityZ = buf.readDouble()
        val nBuf = PacketByteBuf(buf.copy())
        context.queue {
            spawn(
                id, uuid, entityTypeId,
                x, y, z, yaw, pitch, headYaw,
                velocityX, velocityY, velocityZ,
                nBuf
            )
        }
    }

    @Environment(EnvType.CLIENT)
    private fun spawn(
        id: Int, uuid: UUID, entityTypeId: Int, x: Double, y: Double, z: Double,
        yaw: Float, pitch: Float, headYaw: Float,
        velocityX: Double, velocityY: Double, velocityZ: Double, buf: PacketByteBuf
    ) {
        val client = MinecraftClient.getInstance()
        val world = client.world ?: return
        val entity = EntityType.createInstanceFromId(entityTypeId, world)
        if (entity is FGCustomPacketEntity) {
            entity.updateTrackedPosition(x, y, z)
            entity.headYaw = headYaw
            entity.entityId = id
            entity.uuid = uuid
            entity.updatePositionAndAngles(x, y, z, yaw, pitch)
            entity.setVelocity(velocityX, velocityY, velocityZ)
            entity.readCustomPacket(buf)
            world.addEntity(id, entity)
        } else {
            LOGGER.warn("Skipping Entity with id {}", entityTypeId)
        }
    }
}