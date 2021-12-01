package net.sistr.flexibleguns.wip.ecs.component.entity.util

import io.netty.buffer.Unpooled
import me.shedaniel.architectury.networking.NetworkManager
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.Packet
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier
import net.minecraft.world.World
import net.sistr.flexibleguns.FlexibleGunsMod
import net.sistr.flexibleguns.wip.ecs.EntityManager
import net.sistr.flexibleguns.wip.ecs.component.ComponentSet

class EntityInstance(
    type: EntityType<EntityInstance>,
    world: World,
    private val entityManager: EntityManager,
    default: Collection<ComponentSet<*>>
) : Entity(type, world) {
    val id: Int = entityManager.addEntity()
    var nbt = NbtCompound()

    init {
        /*entityManager.addComponent(
            id,
            ComponentSet(ComponentHolders.ENTITY_COMPONENTS, EntityComponent(this))
        )*/
        entityManager.addAllComponent(id, default)
        val data = entityManager.read(nbt)
        entityManager.addAllComponent(id, data)
    }

    override fun tick() {
    }

    override fun initDataTracker() {

    }

    override fun readCustomDataFromNbt(tag: NbtCompound) {
        nbt = tag.getCompound("EntityData")
    }

    override fun writeCustomDataToNbt(tag: NbtCompound) {
        entityManager.write(id, nbt)
        tag.put("EntityData", nbt)
    }

    override fun createSpawnPacket(): Packet<*> {
        return NetworkManager.toPacket(
            NetworkManager.Side.S2C,
            Identifier(FlexibleGunsMod.MODID, "entity"),
            PacketByteBuf(Unpooled.buffer())
        )
    }
}