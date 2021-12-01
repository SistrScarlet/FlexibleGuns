package net.sistr.flexibleguns.item

import com.mojang.blaze3d.systems.RenderSystem
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.sistr.flexibleguns.FlexibleGunsMod
import net.sistr.flexibleguns.client.FovCapture
import net.sistr.flexibleguns.client.FovMultiplier
import net.sistr.flexibleguns.client.renderer.util.NumberRenderer
import net.sistr.flexibleguns.resource.GunManager
import net.sistr.flexibleguns.resource.GunSetting
import net.sistr.flexibleguns.util.HudRenderable
import net.sistr.flexibleguns.util.ItemInstance
import net.sistr.flexibleguns.util.Zoomable

@Environment(EnvType.CLIENT)
class ClientGunInstance(
    private val holder: LivingEntity,
    private val stack: ItemStack,
    setting: GunSetting,
    nbt: NbtCompound
) : ItemInstance,
    HudRenderable {

    companion object {
        private val NUMBER_TEXTURE = Identifier(FlexibleGunsMod.MODID, "textures/hud/number.png")
        private val GUI_TEXTURE = Identifier(FlexibleGunsMod.MODID, "textures/hud/ammo_frame.png")
    }

    constructor(holder: LivingEntity, stack: ItemStack) : this(
        holder,
        stack,
        GunManager.INSTANCE.getGunSetting(Identifier(stack.orCreateTag.getString("GunSettingId")))!!,
        stack.orCreateTag.getCompound("GunDate")
    )

    //基本
    private var heldHand: Hand? = null
    private var hold = false
    private var prevHold = false

    //弾丸
    private val inAccuracy = setting.inAccuracy

    //弾数
    var ammo = nbt.getInt("ammo")

    //ズーム
    private val canZoom = setting.zoom != null
    private val zoomInAccuracy = setting.zoom?.zoomInAccuracy ?: 0f
    private val zoomAmount = setting.zoom?.zoomAmount ?: 0f

    override fun tick() {
        this.heldHand = getHand()
        this.hold = heldHand != null
        if (hold) {
            if (!prevHold) {
                onHold()
            }
        } else if (prevHold) {
            unHold()
        }
        if (hold) {
            tickHold(heldHand!!)
        }
        prevHold = hold
    }

    private fun onHold() {

    }

    private fun unHold() {
        if (canZoom) {
            FovMultiplier.setFov(1f)
        }
    }

    private fun tickHold(heldHand: Hand) {
        if (canZoom && holder is Zoomable) {
            if ((holder as Zoomable).isZoom_FG()) {
                FovMultiplier.setFov(zoomAmount)
            } else {
                FovMultiplier.setFov(1f)
            }
        }
    }

    private fun getHand(): Hand? {
        if (holder.mainHandStack === stack) {
            return Hand.MAIN_HAND
        } else if (holder.offHandStack === stack) {
            return Hand.OFF_HAND
        }
        return null
    }

    override fun remove() {

    }

    override fun renderHud_FG(
        mc: MinecraftClient,
        player: ClientPlayerEntity,
        matrices: MatrixStack,
        tickDelta: Float
    ) {
        val window = mc.window

        val baseX = (window.scaledWidth * 0.9f).toInt()
        val baseY = (window.scaledHeight * 0.7f).toInt()

        mc.textureManager.bindTexture(GUI_TEXTURE)
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f)
        DrawableHelper.drawTexture(
            matrices,
            baseX - 4 - 16 * 3, baseY - 4, -97,
            0f, 0f,
            64, 32,
            32, 64,
        )

        mc.textureManager.bindTexture(NUMBER_TEXTURE)
        if (ammo == 0) {
            RenderSystem.color4f(1.0f, 0.0f, 0.0f, 1.0f)
        }

        NumberRenderer.renderNumber(matrices, Math.min(ammo, 999), baseX, baseY, -95, 2, false)
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f)

        //10m先、1.8高さ、角度は10.20度、pは28(実測だとそうだが、計算式は正しくない)
        //GUIサイズでscaledは動く
        //…正確ではないが、概ね正しい

        val fov = FovCapture.fov.toFloat()
        val accuracy = if ((holder as Zoomable).isZoom_FG()) zoomInAccuracy else inAccuracy//射撃角
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

    override fun tickHud_FG(mc: MinecraftClient, player: ClientPlayerEntity) {

    }
}