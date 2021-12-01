package net.sistr.flexibleguns.network

import io.netty.buffer.Unpooled
import me.shedaniel.architectury.networking.NetworkManager
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier
import net.sistr.flexibleguns.FlexibleGunsMod
import net.sistr.flexibleguns.util.Input
import net.sistr.flexibleguns.util.Inputable

object InputPacket {
    val ID = Identifier(FlexibleGunsMod.MODID, "click_input")

    @Environment(EnvType.CLIENT)
    fun sendC2S(input: Input, on: Boolean) {
        val buf = createC2SPacket(input, on)
        NetworkManager.sendToServer(ID, buf)
    }

    fun createC2SPacket(input: Input, on: Boolean): PacketByteBuf {
        val buf = PacketByteBuf(Unpooled.buffer())
        buf.writeEnumConstant(input)
        buf.writeBoolean(on)
        return buf
    }

    fun receiveC2SPacket(buf: PacketByteBuf, ctx: NetworkManager.PacketContext) {
        val input: Input = buf.readEnumConstant(Input::class.java)
        val on = buf.readBoolean()
        ctx.queue {
            (ctx.player as Inputable).inputKeyFG(input, on)
        }
    }

}