package net.sistr.flexibleguns.util

import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.Hand

interface ItemInstance {
    companion object {
        val EMPTY = object : ItemInstance {
            override fun startTick(stack: ItemStack, holder: LivingEntity, heldHand: Hand?) {}
            override fun tick(stack: ItemStack, holder: LivingEntity, heldHand: Hand?) {}
            override fun endTick(stack: ItemStack, holder: LivingEntity, heldHand: Hand?) {}
            override fun save(stack: ItemStack) {}
            override fun copy(stack: ItemStack): ItemInstance {
                return this
            }
        }
    }

    fun startTick(stack: ItemStack, holder: LivingEntity, heldHand: Hand?)

    fun tick(stack: ItemStack, holder: LivingEntity, heldHand: Hand?)

    fun endTick(stack: ItemStack, holder: LivingEntity, heldHand: Hand?)

    fun save(stack: ItemStack)

    fun copy(stack: ItemStack): ItemInstance

}