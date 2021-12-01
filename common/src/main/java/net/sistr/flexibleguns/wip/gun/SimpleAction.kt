package net.sistr.flexibleguns.wip.gun

class SimpleAction(private val shotAction: IShotAction, private val interactEvent: IInteractEvent) {
    private var shooting = false

    fun tick() {
        if (shooting && interactEvent.interact().shouldRun()) {
            shot()
        }
    }

    private fun shot() {
        shotAction.shot()
    }

    interface IInteractResult {
        fun shouldRun(): Boolean
    }

    interface IInteractEvent {
        fun interact(): IInteractResult
    }

    interface IShotAction {
        fun shot()
    }

}