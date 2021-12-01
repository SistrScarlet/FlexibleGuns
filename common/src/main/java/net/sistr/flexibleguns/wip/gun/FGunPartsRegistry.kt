package net.sistr.flexibleguns.wip.gun

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Identifier
import java.util.*

object FGunPartsRegistry {
    private val PARTS_REGISTRY: BiMap<Identifier, IFGunPartType<IFGunParts>> = HashBiMap.create()

    fun register(id: Identifier, type: IFGunPartType<IFGunParts>) {
        PARTS_REGISTRY[id] = type
    }

    fun createGunPart(id: Identifier): Optional<IFGunParts> {
        return getGunPartType(id).map { type -> type.create() }
    }

    fun getGunPartType(id: Identifier): Optional<IFGunPartType<IFGunParts>> {
        return Optional.ofNullable(PARTS_REGISTRY[id])
    }

    fun loadGunPart(nbt: NbtCompound): Optional<IFGunParts> {
        return Optional.ofNullable(PARTS_REGISTRY[Identifier(nbt.getString("id"))]?.create(nbt))
    }

    fun loadGunParts(gunStack: ItemStack): Collection<IFGunParts> {
        if (gunStack.tag == null) return emptyList()
        return loadGunParts(gunStack.tag!!)
    }

    fun loadGunParts(nbt: NbtCompound): Collection<IFGunParts> {
        return nbt
            .getCompound("FGTagData")
            .getList("Parts", 10)
            .map { tag -> tag as NbtCompound }
            .map { tag -> loadGunPart(tag) }
            .filter { optional -> optional.isPresent }
            .map { optional -> optional.get() }
    }

}