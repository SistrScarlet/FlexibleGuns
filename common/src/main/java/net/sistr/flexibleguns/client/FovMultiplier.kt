package net.sistr.flexibleguns.client

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.util.math.MathHelper

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
        prevValue = value
        value += (fov - value) * 0.5f
    }

}