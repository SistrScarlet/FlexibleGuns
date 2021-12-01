package net.sistr.flexibleguns.wip.ecs.event.item.gun

import net.sistr.flexibleguns.wip.ecs.event.Event
import net.sistr.flexibleguns.wip.ecs.event.EventContext

class GunCanShootEvent : Event<GunCanShootEvent.Context>() {

    class Context(id: Int, var cancel: Boolean) : EventContext(id)

}