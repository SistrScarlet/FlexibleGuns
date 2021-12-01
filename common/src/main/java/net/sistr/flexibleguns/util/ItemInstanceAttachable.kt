package net.sistr.flexibleguns.util

import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.world.World

interface ItemInstanceAttachable {

    fun createItemInstanceFG(world: World, holder: LivingEntity, stack: ItemStack): ItemInstance

}