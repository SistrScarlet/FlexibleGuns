package net.sistr.flexibleguns.network

import io.netty.buffer.Unpooled
import me.shedaniel.architectury.networking.NetworkManager
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.network.Packet
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier
import net.minecraft.util.math.MathHelper
import net.minecraft.util.registry.Registry
import net.sistr.flexibleguns.FlexibleGunsMod.MODID
import net.sistr.flexibleguns.util.FGCustomPacketEntity
import org.apache.logging.log4j.LogManager
import java.util.*

object CustomMobSpawnPacket {
    val ID = Identifier(MODID, "custom_mob_spawn")
    val LOGGER = LogManager.getLogger()

    fun createPacket(entity: LivingEntity): Packet<*> {
        check(entity is FGCustomPacketEntity) { "CustomPacketEntityを実装していません。" }
        val buf = PacketByteBuf(Unpooled.buffer())
        buf.writeVarInt(entity.entityId)
        buf.writeUuid(entity.uuid)
        buf.writeVarInt(Registry.ENTITY_TYPE.getRawId(entity.type))
        buf.writeDouble(entity.x)
        buf.writeDouble(entity.y)
        buf.writeDouble(entity.z)
        buf.writeByte((entity.yaw * 256.0f / 360.0f).toInt())
        buf.writeByte((entity.pitch * 256.0f / 360.0f).toInt())
        buf.writeByte((entity.headYaw * 256.0f / 360.0f).toInt())
        val vec3d = entity.velocity
        val velocityX = (MathHelper.clamp(vec3d.x, -3.9, 3.9) * 8000.0).toInt()
        val velocityY = (MathHelper.clamp(vec3d.y, -3.9, 3.9) * 8000.0).toInt()
        val velocityZ = (MathHelper.clamp(vec3d.z, -3.9, 3.9) * 8000.0).toInt()
        buf.writeShort(velocityX)
        buf.writeShort(velocityY)
        buf.writeShort(velocityZ)
        (entity as FGCustomPacketEntity).writeCustomPacket(buf)
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
        val yaw = buf.readByte() * 360f / 256f
        val pitch = buf.readByte() * 360f / 256f
        val headYaw = buf.readByte() * 360f / 256f
        val velocityX = buf.readShort() / 8000f
        val velocityY = buf.readShort() / 8000f
        val velocityZ = buf.readShort() / 8000f
        //そのまんまbuf渡すと、spawnが実行されるまでの間に読み込めなくなるため、コピーする
        val additional = PacketByteBuf(buf.copy())
        context.queue {
            spawn(
                id, uuid, entityTypeId, x, y, z, yaw, pitch, headYaw,
                velocityX, velocityY, velocityZ, additional
            )
        }
    }

    @Environment(EnvType.CLIENT)
    private fun spawn(
        id: Int, uuid: UUID, entityTypeId: Int, x: Double, y: Double, z: Double,
        yaw: Float, pitch: Float, headYaw: Float,
        velocityX: Float, velocityY: Float, velocityZ: Float, additional: PacketByteBuf
    ) {
        val client = MinecraftClient.getInstance()
        val world = client.world ?: return
        val livingEntity = EntityType.createInstanceFromId(entityTypeId, world) as LivingEntity?
        if (livingEntity is FGCustomPacketEntity) {
            livingEntity.updateTrackedPosition(x, y, z)
            livingEntity.bodyYaw = headYaw
            livingEntity.headYaw = headYaw
            livingEntity.entityId = id
            livingEntity.uuid = uuid
            livingEntity.updatePositionAndAngles(x, y, z, yaw, pitch)
            livingEntity.setVelocity(velocityX.toDouble(), velocityY.toDouble(), velocityZ.toDouble())
            (livingEntity as FGCustomPacketEntity).readCustomPacket(additional)
            world.addEntity(id, livingEntity)
        } else {
            LOGGER.warn("Skipping Entity with id {}", entityTypeId)
        }
        if (additional.refCnt() > 0) {
            additional.release()
        }
    }
}