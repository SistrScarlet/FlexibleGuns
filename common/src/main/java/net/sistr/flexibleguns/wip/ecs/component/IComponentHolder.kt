package net.sistr.flexibleguns.wip.ecs.component

import net.minecraft.nbt.NbtCompound
import java.util.*

interface IComponentHolder<T> {

    fun add(id: Int, component: T)

    fun get(id: Int): Optional<Component<T>>

    fun remove(id: Int)

    fun contains(id: Int): Boolean

    fun getComponents(): Collection<Component<T>>

    fun write(id: Int): Optional<NbtCompound>

    fun write(data: T): NbtCompound

    fun read(nbt: NbtCompound): Optional<T>

}