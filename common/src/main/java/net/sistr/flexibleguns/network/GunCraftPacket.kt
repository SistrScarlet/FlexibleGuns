package net.sistr.flexibleguns.network

import io.netty.buffer.Unpooled
import me.shedaniel.architectury.networking.NetworkManager
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier
import net.sistr.flexibleguns.FlexibleGunsMod
import net.sistr.flexibleguns.block.GunTableScreenHandler
import net.sistr.flexibleguns.resource.GunRecipeManager

object GunCraftPacket {
    val ID = Identifier(FlexibleGunsMod.MODID, "gun_craft")

    @Environment(EnvType.CLIENT)
    fun sendC2S(recipeId: Identifier) {
        val buf = createC2SPacket(recipeId)
        NetworkManager.sendToServer(ID, buf)
    }

    @Environment(EnvType.CLIENT)
    fun createC2SPacket(recipeId: Identifier): PacketByteBuf {
        val buf = PacketByteBuf(Unpooled.buffer())
        buf.writeIdentifier(recipeId)
        return buf
    }

    fun receiveC2SPacket(buf: PacketByteBuf, ctx: NetworkManager.PacketContext) {
        val recipeId = buf.readIdentifier()
        ctx.queue {
            val player = ctx.player
            if (player.currentScreenHandler is GunTableScreenHandler) {
                val recipe = GunRecipeManager.INSTANCE.getRecipe(recipeId)
                if (recipe != null) {
                    val playerInventory = player.inventory
                    if (recipe.match(playerInventory) || player.isCreative) {
                        val result = recipe.craft(playerInventory)
                        if (!playerInventory.insertStack(result)) {
                            val dropItemEntity = playerInventory.player.dropItem(result, true)
                            if (dropItemEntity != null) {
                                playerInventory.player.world.spawnEntity(dropItemEntity)
                            }
                        }
                    }
                }
            }
        }
    }

}