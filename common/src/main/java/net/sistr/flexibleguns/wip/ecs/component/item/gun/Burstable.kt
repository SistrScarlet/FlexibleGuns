package net.sistr.flexibleguns.wip.ecs.component.item.gun

data class Burstable(
    val maxBurstCount: Int,
    val maxBurstDelay: Float,
    var burstCount: Int = 0,
    var burstDelay: Float = 0f
)