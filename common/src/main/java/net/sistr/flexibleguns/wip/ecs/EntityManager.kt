package net.sistr.flexibleguns.wip.ecs

import com.google.common.collect.Lists
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Identifier
import net.sistr.flexibleguns.wip.ecs.component.ComponentManager
import net.sistr.flexibleguns.wip.ecs.component.ComponentSet
import net.sistr.flexibleguns.wip.ecs.component.IComponentHolder

class EntityManager(private val componentManager: ComponentManager) {
    private val entitySet = IntOpenHashSet()
    private val entityComponents = Int2ObjectOpenHashMap<ObjectOpenHashSet<Identifier>>()
    private var incrementalEntityId = 0

    companion object {
        val INSTANCE = EntityManager(ComponentManager.INSTANCE)
    }

    fun addEntity(components: Collection<ComponentSet<*>>): Int {
        val id = addEntity()
        addAllComponent(id, components)
        return id
    }

    fun addEntity(): Int {
        val id = getNewId()
        entitySet.add(id)
        return id
    }

    fun addAllComponent(entityId: Int, components: Collection<ComponentSet<*>>) {
        components.forEach { addComponent(entityId, it) }
    }

    fun <T> addComponent(entityId: Int, set: ComponentSet<T>) {
        set.holder.add(entityId, set.data)
    }

    fun getAllComponent(entityId: Int): MutableList<IComponentHolder<*>> {
        val set = entityComponents[entityId] ?: return Lists.newArrayList()
        val list = Lists.newArrayList<IComponentHolder<*>>()
        set.forEach {
            val holder = this.componentManager.get(it)
            if (holder != null) {
                list.add(holder)
            }
        }
        return list
    }

    fun hasComponent(entityId: Int, id: Identifier): Boolean {
        val set = entityComponents[entityId] ?: return false
        return set.contains(id)
    }

    fun remove(entityId: Int) {
        entitySet.remove(entityId)
        componentManager.remove(entityId)
    }

    fun isAliveEntity(entityId: Int): Boolean {
        return entitySet.contains(entityId)
    }

    fun write(entityId: Int, nbt: NbtCompound) {
        if (isAliveEntity(entityId)) componentManager.write(entityId, nbt)
    }

    fun read(nbt: NbtCompound): List<ComponentSet<*>> {
        return componentManager.read(nbt)
    }

    private fun getNewId(): Int {
        return incrementalEntityId++
    }

}