package net.sistr.flexibleguns.client

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import net.minecraft.util.math.MathHelper
import net.sistr.flexibleguns.util.CustomItemStack
import net.sistr.flexibleguns.util.ZoomableEntity
import net.sistr.flexibleguns.util.ZoomableItem

@Environment(EnvType.CLIENT)
object FovMultiplier {
    private var fov = 1f
    private var value = 1f
    private var prevValue = 1f
    private var reset = true

    fun setFov(fov: Float) {
        FovMultiplier.fov = fov
        reset = false
    }

    fun getFov(tickDelta: Float): Float {
        return MathHelper.lerp(tickDelta, prevValue, value)
    }

    fun tick() {
        val player = MinecraftClient.getInstance().player
        if (player != null) {
            if ((player as ZoomableEntity).isZoom_FG()) {
                val stack = player.mainHandStack
                val instance = ((stack as Any) as CustomItemStack).getItemInstanceFG()
                if (instance != null && instance is ZoomableItem) {
                    fov = instance.getDisplayZoomAmp(player)
                }
            } else {
                fov = 1f
            }
        }

        prevValue = value
        value += (fov - value) * 0.5f
    }

}