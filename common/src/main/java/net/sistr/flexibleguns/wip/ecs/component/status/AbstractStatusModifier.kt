package net.sistr.flexibleguns.wip.ecs.component.status

import java.lang.RuntimeException

abstract class AbstractStatusModifier(private val id: String): IStatusModifier {

    override fun getId(): String {
        return id
    }

    class Base(id: String, private val num: Float): AbstractStatusModifier(id) {
        override fun getValue(value: Float): Float {
            return this.num
        }
    }

    class Plus(id: String, private val num: Float): AbstractStatusModifier(id) {
        override fun getValue(value: Float): Float {
            return  value + this.num
        }
    }

    class Minus(id: String, private val num: Float): AbstractStatusModifier(id) {
        override fun getValue(value: Float): Float {
            return value - this.num
        }
    }

    class Mul(id: String, private val num: Float): AbstractStatusModifier(id) {
        override fun getValue(value: Float): Float {
            return value * this.num
        }
    }

    class Div(id: String, private val num: Float): AbstractStatusModifier(id) {
        init {
            if (this.num == 0f) {
                throw RuntimeException("div 0")
            }
        }

        override fun getValue(value: Float): Float {
            return value / this.num
        }
    }

}