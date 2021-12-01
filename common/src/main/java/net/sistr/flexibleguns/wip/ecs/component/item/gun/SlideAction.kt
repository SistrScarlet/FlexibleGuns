package net.sistr.flexibleguns.wip.ecs.component.item.gun

data class SlideAction(val closeLength: Int, var chamberOpen: Boolean = false, var closeTime: Int = 0)