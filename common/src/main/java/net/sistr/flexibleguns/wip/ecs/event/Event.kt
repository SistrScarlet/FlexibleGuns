package net.sistr.flexibleguns.wip.ecs.event

import com.google.common.collect.ImmutableList
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.minecraft.util.Identifier

open class Event<T: IEventContext> : IEvent<T> {
    private val idMap = Object2ObjectOpenHashMap<Identifier, IEvent<T>>()
    private val preDepMap = Object2ObjectOpenHashMap<Identifier, ImmutableList<Identifier>>()
    private val postDepMap = Object2ObjectOpenHashMap<Identifier, ImmutableList<Identifier>>()
    protected val listeners = ObjectArrayList<IEvent<T>>()

    override fun runEvent(ctx: T) {
        listeners.forEach { it.runEvent(ctx) }
    }

    fun registerAndSetDep(
        id: Identifier,
        event: IEvent<T>,
        preDep: Collection<Identifier>,
        postDep: Collection<Identifier>
    ) {
        setPreDep(id, preDep)
        setPostDep(id, postDep)
        register(id, event)
    }

    fun registerAndSetPreDep(id: Identifier, event: IEvent<T>, preDep: Collection<Identifier>) {
        this.registerAndSetDep(id, event, preDep, ImmutableList.of())
    }

    fun registerAndSetPostDep(id: Identifier, event: IEvent<T>, postDep: Collection<Identifier>) {
        this.registerAndSetDep(id, event, ImmutableList.of(), postDep)
    }

    fun setPreDep(id: Identifier, preDep: Collection<Identifier>) {
        preDepMap[id] = ImmutableList.copyOf(preDep)
    }

    fun setPostDep(id: Identifier, postDep: Collection<Identifier>) {
        postDep.forEach {
            val list = postDepMap.computeIfAbsent(it) { ImmutableList.of() }
            postDepMap[it] = ImmutableList.builder<Identifier>().addAll(list).add(id).build()
        }
    }

    fun register(id: Identifier, event: IEvent<T>) {
        idMap[id] = event
        assembleEventList()
    }

    private fun assembleEventList() {
        listeners.clear()
        for (entry in idMap.entries) {
            assembleEventList(entry.key)
        }
    }

    private fun assembleEventList(id: Identifier) {
        val event = idMap[id] ?: return
        if (listeners.contains(event)) {
            return
        }
        //このシステムより先に処理したいやつがあるなら先にそちらを登録する
        postDepMap[id]?.forEach { assembleEventList(it) }
        preDepMap[id]?.forEach { assembleEventList(it) }
        listeners.add(event)
    }

}