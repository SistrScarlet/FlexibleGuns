package net.sistr.flexibleguns.client.overlay

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.util.math.MatrixStack
import net.sistr.flexibleguns.client.FovCapture
import net.sistr.flexibleguns.util.CustomItemStack
import net.sistr.flexibleguns.util.ShootableItem

class CrossHairOverlay : HudOverlayRenderer.Overlay {
    override fun render(mc: MinecraftClient, matrices: MatrixStack, tickDelta: Float) {
        val player = mc.player ?: return
        val stack = player.mainHandStack
        val itemInstance = ((stack as Any) as CustomItemStack).getItemInstanceFG()
        if (itemInstance == null || itemInstance !is ShootableItem) {
            return
        }

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)

        //10m先、1.8高さ、角度は10.20度、pは28(実測だとそうだが、計算式は正しくない)
        //GUIサイズでscaledは動く
        //…正確ではないが、概ね正しい

        val fov = FovCapture.fov.toFloat()
        val accuracy = itemInstance.getInAccuracy(player)//射撃角
        val window = mc.window

        //視野角に対する射撃角の割合 * 縦画面の量
        val p = Math.max(window.scaledHeight / 25, ((accuracy / (fov / 2f)) * (window.scaledHeight / 2f)).toInt())

        val centerX = window.scaledWidth / 2
        val centerY = window.scaledHeight / 2

        DrawableHelper.fill(
            matrices,
            centerX - 1, centerY - p - 10,
            centerX + 1, centerY - p,
            (196 shl 24) or (255 shl 16) or (255 shl 8) or 255//Kotlinの悪いとこ出てる
        )
        DrawableHelper.fill(
            matrices,
            centerX - 1, centerY + p,
            centerX + 1, centerY + p + 10,
            (196 shl 24) or (255 shl 16) or (255 shl 8) or 255
        )
        DrawableHelper.fill(
            matrices,
            centerX - p - 10, centerY - 1,
            centerX - p, centerY + 1,
            (196 shl 24) or (255 shl 16) or (255 shl 8) or 255
        )
        DrawableHelper.fill(
            matrices,
            centerX + p + 10, centerY - 1,
            centerX + p, centerY + 1,
            (196 shl 24) or (255 shl 16) or (255 shl 8) or 255
        )
    }

    override fun tick(mc: MinecraftClient) {

    }
}