package net.sistr.flexibleguns.wip.ecs.system

import com.google.common.collect.ImmutableList
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.minecraft.util.Identifier

class SystemManager : ISystemManager {
    private val idMap = Object2ObjectOpenHashMap<Identifier, ISystem>()
    private val preDepMap = Object2ObjectOpenHashMap<Identifier, ImmutableList<Identifier>>()
    private val postDepMap = Object2ObjectOpenHashMap<Identifier, ImmutableList<Identifier>>()
    private val systems = ObjectArrayList<ISystem>()

    fun registerAndSetDep(
        id: Identifier,
        system: ISystem,
        preDep: Collection<Identifier>,
        postDep: Collection<Identifier>
    ) {
        setPreDep(id, preDep)
        setPostDep(id, postDep)
        register(id, system)
    }

    fun registerAndSetPreDep(id: Identifier, system: ISystem, preDep: Collection<Identifier>) {
        this.registerAndSetDep(id, system, preDep, ImmutableList.of())
    }

    fun registerAndSetPostDep(id: Identifier, system: ISystem, postDep: Collection<Identifier>) {
        this.registerAndSetDep(id, system, ImmutableList.of(), postDep)
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

    fun register(id: Identifier, system: ISystem) {
        idMap[id] = system
        assembleSystemList()
    }

    private fun assembleSystemList() {
        systems.clear()
        for (entry in idMap.entries) {
            assembleSystemList(entry.key)
        }
    }

    private fun assembleSystemList(id: Identifier) {
        val system = idMap[id] ?: return
        if (systems.contains(system)) {
            return
        }
        //このシステムより先に処理したいやつがあるなら先にそちらを登録する
        postDepMap[id]?.forEach { assembleSystemList(it) }
        preDepMap[id]?.forEach { assembleSystemList(it) }
        systems.add(system)
    }

    override fun run() {
        systems.forEach { it.run() }
    }

}