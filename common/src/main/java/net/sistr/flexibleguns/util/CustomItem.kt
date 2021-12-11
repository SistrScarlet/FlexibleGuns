package net.sistr.flexibleguns.util

import net.minecraft.item.ItemStack

interface CustomItem {

    fun createItemInstanceFG(stack: ItemStack): ItemInstance

}