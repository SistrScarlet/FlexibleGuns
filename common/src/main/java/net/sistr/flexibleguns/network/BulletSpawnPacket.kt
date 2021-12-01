package net.sistr.flexibleguns.network

import io.netty.buffer.Unpooled
import me.shedaniel.architectury.networking.NetworkManager
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.EntityType
import net.minecraft.network.Packet
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import net.sistr.flexibleguns.FlexibleGunsMod
import net.sistr.flexibleguns.entity.FGBulletEntity
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.*

object BulletSpawnPacket {
    val ID = Identifier(FlexibleGunsMod.MODID, "bullet_spawn")
    private val LOGGER: Logger = LogManager.getLogger()

    fun createPacket(bullet: FGBulletEntity): Packet<*> {
        val buf = PacketByteBuf(Unpooled.buffer())
        buf.writeVarInt(bullet.entityId)
        buf.writeUuid(bullet.uuid)
        buf.writeVarInt(Registry.ENTITY_TYPE.getRawId(bullet.type))
        buf.writeDouble(bullet.x)
        buf.writeDouble(bullet.y)
        buf.writeDouble(bullet.z)
        buf.writeFloat(bullet.yaw)
        buf.writeFloat(bullet.pitch)
        buf.writeFloat(bullet.headYaw)
        val velocity = bullet.velocity
        buf.writeDouble(velocity.x)
        buf.writeDouble(velocity.y)
        buf.writeDouble(velocity.z)
        buf.writeInt(bullet.owner?.entityId ?: 0)
        bullet.write(buf)
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
        val ownerEntityId = buf.readInt()
        val nBuf = PacketByteBuf(buf.copy())
        context.queue {
            spawn(
                id, uuid, entityTypeId,
                x, y, z, yaw, pitch, headYaw,
                velocityX, velocityY, velocityZ,
                ownerEntityId,
                nBuf
            )
        }
    }

    @Environment(EnvType.CLIENT)
    private fun spawn(
        id: Int, uuid: UUID, entityTypeId: Int, x: Double, y: Double, z: Double,
        yaw: Float, pitch: Float, headYaw: Float,
        velocityX: Double, velocityY: Double, velocityZ: Double, ownerEntityId: Int,
        buf: PacketByteBuf
    ) {
        val client = MinecraftClient.getInstance()
        val world = client.world ?: return
        val bullet = EntityType.createInstanceFromId(entityTypeId, world)
        if (bullet is FGBulletEntity) {
            bullet.updateTrackedPosition(x, y, z)
            bullet.headYaw = headYaw
            bullet.entityId = id
            bullet.uuid = uuid
            bullet.updatePositionAndAngles(x, y, z, yaw, pitch)
            bullet.setVelocity(velocityX, velocityY, velocityZ)
            val owner = world.getEntityById(ownerEntityId)
            if (owner != null) {
                bullet.owner = owner
            }
            bullet.read(buf)
            world.addEntity(id, bullet)
        } else {
            LOGGER.warn("Skipping Entity with id {}", entityTypeId)
        }
    }

}