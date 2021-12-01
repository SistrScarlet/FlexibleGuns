package net.sistr.flexibleguns.wip.gun

import net.minecraft.nbt.NbtCompound

interface IFGunParts {

    fun writeNBT(): NbtCompound

}