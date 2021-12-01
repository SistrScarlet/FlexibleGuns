package net.sistr.flexibleguns.util

import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.util.math.MatrixStack

interface HudRenderable {

    fun renderHud_FG(mc: MinecraftClient, player: ClientPlayerEntity, matrices: MatrixStack, tickDelta: Float)

    fun tickHud_FG(mc: MinecraftClient, player: ClientPlayerEntity)

}