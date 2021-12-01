package net.sistr.flexibleguns.client.overlay

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.math.MatrixStack
import net.sistr.flexibleguns.util.HudRenderable
import net.sistr.flexibleguns.util.ItemInstanceHolder

@Environment(EnvType.CLIENT)
class AmmoOverlay {

    companion object {
        val INSTANCE = AmmoOverlay()
    }

    fun render(mc: MinecraftClient, matrices: MatrixStack, tickDelta: Float) {
        val player = mc.player ?: return
        (player as ItemInstanceHolder).getItemInstanceFG(player.mainHandStack)
            .filter { it is HudRenderable }
            .map { it as HudRenderable }
            .ifPresent { it.renderHud_FG( mc, player, matrices, tickDelta) }
    }

    fun tick(mc: MinecraftClient) {
        val player = mc.player ?: return
        (player as ItemInstanceHolder).getItemInstanceFG(player.mainHandStack)
            .filter { it is HudRenderable }
            .map { it as HudRenderable }
            .ifPresent { it.tickHud_FG(mc, player) }
    }

}