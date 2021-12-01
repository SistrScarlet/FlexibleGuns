package net.sistr.flexibleguns.wip.gun

import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.minecraft.nbt.NbtCompound

class TriggerParts(type: IFGunPartType<FGunParts>) : FGunParts(type) {
    private val gunParts: MutableList<IFGunParts> = ObjectArrayList()

    constructor(type: IFGunPartType<FGunParts>, nbt: NbtCompound) : this(type) {
        gunParts.addAll(FGunPartsRegistry.loadGunParts(nbt))
    }

    override fun writeNBT(): NbtCompound {
        return TODO()
    }

    fun trigger() {

    }


}