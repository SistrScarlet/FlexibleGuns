package net.sistr.flexibleguns.wip.gun

import net.minecraft.nbt.NbtCompound

interface IFGunPartType<out T : IFGunParts> {
    fun create(): T
    fun create(nbt: NbtCompound): T
}