package net.sistr.flexibleguns.network

import io.netty.buffer.Unpooled
import me.shedaniel.architectury.networking.NetworkManager
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.sistr.flexibleguns.FlexibleGunsMod
import net.sistr.flexibleguns.util.CustomItemStack
import net.sistr.flexibleguns.util.HasAmmoItem

object AmmoPacket {
    val ID = Identifier(FlexibleGunsMod.MODID, "ammo")

    fun sendS2C(playerEntity: ServerPlayerEntity, ammo: Int) {
        val buf = createS2CPacket(ammo)
        NetworkManager.sendToPlayer(playerEntity, ID, buf)
    }

    fun createS2CPacket(ammo: Int): PacketByteBuf {
        val buf = PacketByteBuf(Unpooled.buffer())
        buf.writeVarInt(ammo)
        return buf
    }

    @Environment(EnvType.CLIENT)
    fun receiveS2CPacket(buf: PacketByteBuf, ctx: NetworkManager.PacketContext) {
        val ammo = buf.readVarInt()
        ctx.queue {
            val player = ctx.player
            val mainStack = player.mainHandStack
            val itemInstance = ((mainStack as Any) as CustomItemStack).getItemInstanceFG()
            if (itemInstance != null && itemInstance is HasAmmoItem) {
                itemInstance.setAmmoAmount(ammo)
            }
        }
    }

}