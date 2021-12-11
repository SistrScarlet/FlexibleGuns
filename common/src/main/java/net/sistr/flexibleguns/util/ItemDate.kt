package net.sistr.flexibleguns.util

import net.minecraft.item.ItemStack
import net.minecraft.util.Hand

data class ItemDate(val stack: ItemStack, var heldHand: Hand?, var isAlive: Boolean)
