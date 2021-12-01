package net.sistr.flexibleguns.wip.ecs.event

open class EventContext(private val id: Int): IEventContext {
    override fun getEntityId(): Int {
        return id
    }
}