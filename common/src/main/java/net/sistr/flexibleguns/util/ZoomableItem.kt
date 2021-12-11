package net.sistr.flexibleguns.util

import net.minecraft.entity.LivingEntity

interface ZoomableItem {

    fun canZoom(): Boolean

    fun zoom(holder: LivingEntity)

    fun unZoom(holder: LivingEntity)

    fun getDisplayZoomAmp(holder: LivingEntity): Float

}