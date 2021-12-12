package net.sistr.flexibleguns.client.overlay

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import net.sistr.flexibleguns.FlexibleGunsMod
import net.sistr.flexibleguns.client.renderer.util.NumberRenderer
import net.sistr.flexibleguns.client.screen.GunTableScreen
import net.sistr.flexibleguns.util.CustomItemStack
import net.sistr.flexibleguns.util.HasAmmoItem

class AmmoOverlay : HudOverlayRenderer.Overlay {

    companion object {
        private val NUMBER_TEXTURE = Identifier(FlexibleGunsMod.MODID, "textures/hud/number.png")
        private val GUI_TEXTURE = Identifier(FlexibleGunsMod.MODID, "textures/hud/ammo_frame.png")
    }

    override fun render(mc: MinecraftClient, matrices: MatrixStack, tickDelta: Float) {
        val player = mc.player ?: return
        val stack = player.mainHandStack
        val itemInstance = ((stack as Any) as CustomItemStack).getItemInstanceFG()
        if (itemInstance == null || itemInstance !is HasAmmoItem) {
            return
        }

        val window = mc.window

        val baseX = (window.scaledWidth * 0.9f).toInt()
        val baseY = (window.scaledHeight * 0.7f).toInt()

        RenderSystem.setShader { GameRenderer.getPositionTexShader() }
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
        RenderSystem.setShaderTexture(0, GUI_TEXTURE)
        DrawableHelper.drawTexture(
            matrices,
            baseX - 4 - 16 * 3, baseY - 4, -97,
            0f, 0f,
            64, 32,
            64, 32,
        )

        RenderSystem.setShader { GameRenderer.getPositionTexShader() }
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
        RenderSystem.setShaderTexture(0, NUMBER_TEXTURE)
        val ammo = itemInstance.getAmmoAmount()
        if (ammo == 0) {
            RenderSystem.setShaderColor(1.0f, 0.0f, 0.0f, 1.0f)
        }

        NumberRenderer.renderNumber(matrices, Math.min(ammo, 999), baseX, baseY, -95, 2, false)
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
    }

    override fun tick(mc: MinecraftClient) {

    }
}