package net.sistr.flexibleguns.item

import com.google.common.collect.ImmutableMap
import com.google.common.collect.Lists
import java.util.function.BooleanSupplier

class CustomEventHolder(
    private val eventMap: ImmutableMap<String, Collection<Runnable>>,
    private val cancelableEventMap2: ImmutableMap<String, Collection<BooleanSupplier>>
) {

    fun event(id: String) {
        eventMap[id]?.forEach { it.run() }
    }

    fun cancelableEvent(id: String) {

    }

    class Builder {
        private val eventMap = HashMap<String, MutableList<Runnable>>()
        private val cancelableEventMap2 = HashMap<String, MutableList<BooleanSupplier>>()

        fun register(id: String, runnable: Runnable) {
            eventMap.computeIfAbsent(id) { Lists.newArrayList() }.add(runnable)
        }

        fun register(id: String, supplier: BooleanSupplier) {
            cancelableEventMap2.computeIfAbsent(id) { Lists.newArrayList() }.add(supplier)
        }

    }

}