package net.sistr.flexibleguns.wip.ecs.event.item.gun

import net.sistr.flexibleguns.wip.ecs.event.Event
import net.sistr.flexibleguns.wip.ecs.event.EventContext

class GunShootEvent : Event<GunShootEvent.Context>() {

    class Context(id: Int) : EventContext(id)

}