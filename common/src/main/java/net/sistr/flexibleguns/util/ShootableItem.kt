package net.sistr.flexibleguns.util

import net.minecraft.entity.LivingEntity

interface ShootableItem {
    fun canShoot(): Boolean
    fun getInAccuracy(holder: LivingEntity): Float
}