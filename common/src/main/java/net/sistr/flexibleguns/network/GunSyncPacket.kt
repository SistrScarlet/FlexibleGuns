package net.sistr.flexibleguns.network

import dev.architectury.networking.NetworkManager
import io.netty.buffer.Unpooled
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.sistr.flexibleguns.FlexibleGunsMod
import net.sistr.flexibleguns.resource.GunManager
import net.sistr.flexibleguns.resource.GunRecipeManager

object GunSyncPacket {
    val ID = Identifier(FlexibleGunsMod.MODID, "gun_sync")

    fun sendS2C(playerEntity: ServerPlayerEntity) {
        val buf = createS2CPacket()
        NetworkManager.sendToPlayer(playerEntity, ID, buf)
    }

    fun createS2CPacket(): PacketByteBuf {
        val buf = PacketByteBuf(Unpooled.buffer())
        GunManager.INSTANCE.write(buf)
        GunRecipeManager.INSTANCE.write(buf)
        return buf
    }

    @Environment(EnvType.CLIENT)
    fun receiveS2CPacket(buf: PacketByteBuf, ctx: NetworkManager.PacketContext) {
        val bufC = PacketByteBuf(buf.copy())
        ctx.queue {
            GunManager.INSTANCE.clear()
            GunManager.INSTANCE.read(bufC)
            GunRecipeManager.INSTANCE.clear()
            GunRecipeManager.INSTANCE.read(bufC)
            bufC.release()
        }
    }

}