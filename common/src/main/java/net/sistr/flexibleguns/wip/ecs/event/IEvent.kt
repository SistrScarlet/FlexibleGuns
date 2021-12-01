package net.sistr.flexibleguns.wip.ecs.event

interface IEvent<T: IEventContext> {
    fun runEvent(ctx: T)
}