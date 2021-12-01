package net.sistr.flexibleguns.wip.ecs.component.status

interface IStatusModifier {

    fun getValue(value: Float): Float

    fun getId(): String

}