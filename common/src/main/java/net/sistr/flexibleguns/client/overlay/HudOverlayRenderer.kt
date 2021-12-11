package net.sistr.flexibleguns.client.overlay

import com.google.common.collect.Lists
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.math.MatrixStack
import net.sistr.flexibleguns.util.HudRenderable
import net.sistr.flexibleguns.util.ItemInstanceHolder

@Environment(EnvType.CLIENT)
class HudOverlayRenderer {
    val overlays = Lists.newArrayList<Overlay>()

    companion object {
        val INSTANCE = HudOverlayRenderer()
    }

    fun render(mc: MinecraftClient, matrices: MatrixStack, tickDelta: Float) {
        overlays.forEach { it.render(mc, matrices, tickDelta) }
    }

    fun tick(mc: MinecraftClient) {

    }

    fun register(overlay: Overlay) {
        overlays.add(overlay)
    }

    interface Overlay {
        fun render(mc: MinecraftClient, matrices: MatrixStack, tickDelta: Float)
        fun tick(mc: MinecraftClient)
    }

}