package net.sistr.flexibleguns.item.util

import net.minecraft.client.util.ModelIdentifier
import net.minecraft.item.ItemStack

interface CustomTextureItem {
    fun getTextureId(stack: ItemStack): ModelIdentifier
}