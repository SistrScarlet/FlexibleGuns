package net.sistr.flexibleguns.wip.ecs.component.item.gun

data class Zoomable(val zoomInAccuracy: Float, val zoomSpeed: Float, var zoom: Boolean = false)
