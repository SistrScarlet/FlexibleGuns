package net.sistr.flexibleguns.wip.ecs.component.item

import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack

data class ItemComponent(val holder: LivingEntity, val stack: ItemStack)