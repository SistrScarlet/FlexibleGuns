package net.sistr.flexibleguns.network

import dev.architectury.networking.NetworkManager
import io.netty.buffer.Unpooled
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Identifier
import net.sistr.flexibleguns.FlexibleGunsMod
import net.sistr.flexibleguns.client.SoundCapManager
import net.sistr.flexibleguns.setup.ClientSetup

object BulletHitPacket {
    val ID = Identifier(FlexibleGunsMod.MODID, "bullet_hit")

    fun sendS2C(playerEntity: ServerPlayerEntity, type: Type) {
        val buf = createS2CPacket(type)
        NetworkManager.sendToPlayer(playerEntity, ID, buf)
    }

    fun createS2CPacket(type: Type): PacketByteBuf {
        val buf = PacketByteBuf(Unpooled.buffer())
        buf.writeEnumConstant(type)
        return buf
    }

    @Environment(EnvType.CLIENT)
    fun receiveS2CPacket(buf: PacketByteBuf, ctx: NetworkManager.PacketContext) {
        val type = buf.readEnumConstant(Type.HIT.javaClass)
        ctx.queue {
            if (SoundCapManager.INSTANCE.isSoundCapped(ClientSetup.GUN_HIT_SOUND_CAP_ID)) {
                return@queue
            }
            val player = ctx.player
            when (type) {
                Type.KILL -> {
                    player.playSound(SoundEvents.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.5f, 1.0f)
                }
                Type.HEADSHOT -> {
                    player.playSound(SoundEvents.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.5f, 2f)
                }
                else -> {
                    player.playSound(SoundEvents.ENTITY_GENERIC_HURT, 0.5f, 1.8f)
                }
            }
            SoundCapManager.INSTANCE.addSoundCap(ClientSetup.GUN_HIT_SOUND_CAP_ID)
        }
    }


    enum class Type {
        HIT,
        HEADSHOT,
        KILL
    }

}