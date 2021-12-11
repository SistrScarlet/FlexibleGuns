package net.sistr.flexibleguns.util

import net.minecraft.entity.LivingEntity

interface SpeedChangeableItem {

    fun getSpeedAmp(holder: LivingEntity): Float

}