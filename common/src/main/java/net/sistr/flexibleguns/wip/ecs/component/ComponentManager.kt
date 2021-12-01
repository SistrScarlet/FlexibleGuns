package net.sistr.flexibleguns.wip.ecs.component

import com.google.common.collect.Lists
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.util.Identifier
import java.util.*

class ComponentManager {
    private val componentHolderMap = Object2ObjectOpenHashMap<Identifier, IComponentHolder<*>>()

    companion object {
        val INSTANCE = ComponentManager()
    }

    fun register(id: Identifier, component: IComponentHolder<*>) {
        componentHolderMap[id] = component
    }

    fun remove(id: Int) {
        componentHolderMap.forEach { (_, h) -> h.remove(id) }
    }

    fun get(id: Identifier): IComponentHolder<*>? {
        return componentHolderMap[id]
    }

    fun contains(id: Identifier): Boolean {
        return componentHolderMap.containsKey(id)
    }

    fun write(id: Int, nbt: NbtCompound) {
        val list = NbtList()
        componentHolderMap.forEach { (i, h) ->
            h.write(id).ifPresent { t ->
                t.putString("ComponentId", i.toString())
                list.add(t)
            }
        }
        nbt.put("ComponentData", list)
    }

    fun read(nbt: NbtCompound): List<ComponentSet<*>> {
        val listNbt = nbt.getList("ComponentData", 10)
        val list = Lists.newArrayList<ComponentSet<*>>()
        listNbt
            .map { it as NbtCompound }
            .forEach {
                val componentId = Identifier(it.getString("ComponentId"))
                val holder = componentHolderMap[componentId]
                if (holder != null) {
                    add(holder, it).ifPresent { set -> list.add(set) }
                }
            }
        return list
    }

    private fun <T> add(holder: IComponentHolder<T>, nbt: NbtCompound): Optional<ComponentSet<T>> {
        return holder.read(nbt).map { ComponentSet(holder, it) }
    }
}