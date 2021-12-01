package net.sistr.flexibleguns.wip.ecs.component

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.minecraft.nbt.NbtCompound
import java.util.*

abstract class ComponentHolderBase<T>: IComponentHolder<T> {
    private val components: Int2ObjectOpenHashMap<Component<T>> = Int2ObjectOpenHashMap()

    override fun add(id: Int, component: T) {
        components[id] = Component(id, component)
    }

    override fun get(id: Int): Optional<Component<T>> {
        return Optional.ofNullable(components[id])
    }

    override fun remove(id: Int) {
        components.remove(id)
    }

    override fun contains(id: Int): Boolean {
        return components.containsKey(id)
    }

    override fun getComponents(): Collection<Component<T>> {
        return components.values
    }

    override fun write(id: Int): Optional<NbtCompound> {
        return get(id).map { c -> write(c.data) }
    }

    abstract override fun write(data: T): NbtCompound

    abstract override fun read(nbt: NbtCompound): Optional<T>

}