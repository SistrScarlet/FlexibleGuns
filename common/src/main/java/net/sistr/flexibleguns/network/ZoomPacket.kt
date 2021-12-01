package net.sistr.flexibleguns.network

import io.netty.buffer.Unpooled
import me.shedaniel.architectury.networking.NetworkManager
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.sistr.flexibleguns.FlexibleGunsMod
import net.sistr.flexibleguns.item.ClientGunInstance
import net.sistr.flexibleguns.util.ItemInstanceHolder

object ZoomPacket {
    val ID = Identifier(FlexibleGunsMod.MODID, "zoom")

    fun sendS2C(player: ServerPlayerEntity, zoom: Boolean) {
        val buf = createS2CPacket(zoom)
        NetworkManager.sendToPlayer(player, ID, buf)
    }

    fun createS2CPacket(zoom: Boolean): PacketByteBuf {
        val buf = PacketByteBuf(Unpooled.buffer())
        buf.writeBoolean(zoom)
        return buf
    }

    @Environment(EnvType.CLIENT)
    fun receiveS2CPacket(buf: PacketByteBuf, ctx: NetworkManager.PacketContext) {
        val zoom = buf.readBoolean()
        ctx.queue {
            val player = ctx.player
            (player as ItemInstanceHolder).getItemInstanceFG(player.mainHandStack)
                .filter{it is ClientGunInstance }
                .map { it as ClientGunInstance }
                .ifPresent {  }
        }
    }

}