package net.sistr.flexibleguns.wip.ecs.system

class SimpleSystem(private val system: Runnable) : ISystem {
    override fun run() {
        system.run()
    }
}