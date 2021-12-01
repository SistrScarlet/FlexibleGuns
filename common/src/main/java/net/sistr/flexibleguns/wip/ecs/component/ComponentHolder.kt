package net.sistr.flexibleguns.wip.ecs.component

import net.minecraft.nbt.NbtCompound
import java.util.*

class ComponentHolder<T>(
    private val write: (T, NbtCompound) -> Unit,
    private val read: (NbtCompound) -> Optional<T>
) : ComponentHolderBase<T>() {

    constructor(): this({ _, _ -> }, { Optional.empty() })

    override fun write(data: T): NbtCompound {
        val nbt = NbtCompound()
        write.invoke(data, nbt)
        return nbt
    }

    override fun read(nbt: NbtCompound): Optional<T> {
        return read.invoke(nbt)
    }
}