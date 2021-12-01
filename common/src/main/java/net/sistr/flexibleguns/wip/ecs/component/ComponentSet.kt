package net.sistr.flexibleguns.wip.ecs.component

data class ComponentSet<T>(val holder: IComponentHolder<T>, val data: T)