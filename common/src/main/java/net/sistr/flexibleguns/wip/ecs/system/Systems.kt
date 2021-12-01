package net.sistr.flexibleguns.wip.ecs.system

import net.minecraft.util.Identifier
import net.sistr.flexibleguns.FlexibleGunsMod
import net.sistr.flexibleguns.wip.ecs.component.ComponentHolders
import net.sistr.flexibleguns.wip.ecs.system.tick.item.ItemTickSystem

object Systems {
    val ITEM_TICK_ID = Identifier(FlexibleGunsMod.MODID, "tick")

    fun init() {
        SystemManagers.TICK.register(ITEM_TICK_ID, ItemTickSystem(ComponentHolders.ITEM_COMPONENT_HOLDER))
    }

}