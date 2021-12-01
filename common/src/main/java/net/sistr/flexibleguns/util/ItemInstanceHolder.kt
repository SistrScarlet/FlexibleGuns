package net.sistr.flexibleguns.util

import net.minecraft.item.ItemStack
import java.util.*

interface ItemInstanceHolder {

    fun tickItemInstanceFG()

    fun getItemInstanceFG(stack: ItemStack): Optional<ItemInstance>

}