package net.sistr.flexibleguns.wip.gun

import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.minecraft.item.ItemStack
import net.sistr.flexibleguns.entity.FGShooter

class FGun(shooter: FGShooter, gunStack: ItemStack) : IFGun {
    private val gunParts: List<IFGunParts> = ObjectArrayList(FGunPartsRegistry.loadGunParts(gunStack))

    fun getToolTip() {

    }


    fun tick() {

    }

    override fun shoot(shooter: FGShooter) {

    }


}