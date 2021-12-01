package net.sistr.flexibleguns.wip.ecs.system.tick.item

import net.sistr.flexibleguns.wip.ecs.component.IComponentHolder
import net.sistr.flexibleguns.wip.ecs.component.item.ItemComponent
import net.sistr.flexibleguns.wip.ecs.event.EventContext
import net.sistr.flexibleguns.wip.ecs.event.Events
import net.sistr.flexibleguns.wip.ecs.system.ISystem

class ItemTickSystem(private val itemComponents: IComponentHolder<ItemComponent>) : ISystem {
    override fun run() {
        iterate(itemComponents) { id, _ ->
            Events.ITEM_TICK_EVENT.runEvent(EventContext(id))
        }
    }
}