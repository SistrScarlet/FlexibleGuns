package net.sistr.flexibleguns.wip.ecs.component.item.gun

data class Reloadable(val maxReloadTime: Int, var reloadTime: Int = 0)
