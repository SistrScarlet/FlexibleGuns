package net.sistr.flexibleguns.wip.ecs.component.item.gun

data class Shootable(
    val inAccuracy: Float,
    val velocity: Float,
    val damage: Float,
    val headshotDamage: Float,
    val gravity: Float,
    val shootAmount: Int
)
