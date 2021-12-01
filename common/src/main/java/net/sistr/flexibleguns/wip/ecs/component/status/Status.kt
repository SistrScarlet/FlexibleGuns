package net.sistr.flexibleguns.wip.ecs.component.status

import com.google.common.collect.Lists

class Status(val id: String, var baseValue: Float) {
    private val modifiers: MutableList<IStatusModifier> = Lists.newArrayList()

    fun add(modifier: IStatusModifier) {
        this.modifiers.add(modifier)
    }

    fun remove(modifier: IStatusModifier) {
        this.modifiers.remove(modifier)
    }

    fun clear() {
        this.modifiers.clear()
    }

    fun getValue(): Float {
        var value = baseValue
        modifiers.forEach { value = it.getValue(value) }
        return value
    }

}