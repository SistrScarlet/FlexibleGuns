package net.sistr.flexibleguns.wip.gun

import net.minecraft.nbt.NbtCompound

abstract class FGunParts(val type: IFGunPartType<FGunParts>) : IFGunParts {

    constructor(type: IFGunPartType<FGunParts>, nbt: NbtCompound) : this(type)

}