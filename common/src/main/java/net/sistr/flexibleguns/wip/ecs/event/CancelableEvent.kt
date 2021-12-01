package net.sistr.flexibleguns.wip.ecs.event

class CancelableEvent<T : CancelableEvent.CancelableContext> : Event<T>() {

    override fun runEvent(ctx: T) {
        listeners.forEach {
            it.runEvent(ctx)
            if (ctx.cancel) {
                return
            }
        }
    }

    class CancelableContext(id: Int, var cancel: Boolean) : EventContext(id)

}