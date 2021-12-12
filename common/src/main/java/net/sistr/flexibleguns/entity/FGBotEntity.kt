package net.sistr.flexibleguns.entity

import net.minecraft.entity.EntityData
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnReason
import net.minecraft.entity.ai.goal.*
import net.minecraft.entity.attribute.DefaultAttributeContainer
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.mob.HostileEntity
import net.minecraft.entity.mob.Monster
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Hand
import net.minecraft.world.LocalDifficulty
import net.minecraft.world.ServerWorldAccess
import net.minecraft.world.World
import net.sistr.flexibleguns.entity.goal.ShootTargetGoal
import net.sistr.flexibleguns.entity.util.HasSenseMemory
import net.sistr.flexibleguns.entity.util.SenseMemory
import net.sistr.flexibleguns.resource.GunManager
import net.sistr.flexibleguns.setup.Registration

//todo 視界と記憶
class FGBotEntity(entityType: EntityType<out FGBotEntity>, world: World?) :
    HostileEntity(entityType, world),
    Monster,
    HasSenseMemory {
    val senseMem = SenseMemory(this, SenseMemory.EyeSense(this, 32f, 70f))

    companion object {
        /*val lagChecker = LagChecker()
        val lagChecker1 = LagChecker()
        val lagChecker2 = LagChecker()
        val lagChecker3 = LagChecker()*/

        fun createBotAttributes(): DefaultAttributeContainer.Builder {
            return createHostileAttributes()
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 35.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3.0)
                .add(EntityAttributes.GENERIC_ARMOR, 0.0)
        }
    }

    override fun getSenseMemory(): SenseMemory {
        return senseMem
    }

    override fun initialize(
        world: ServerWorldAccess?,
        difficulty: LocalDifficulty?,
        spawnReason: SpawnReason?,
        entityData: EntityData?,
        entityTag: NbtCompound?
    ): EntityData? {
        initEquipment(difficulty)
        return super.initialize(world, difficulty, spawnReason, entityData, entityTag)
    }

    override fun initEquipment(difficulty: LocalDifficulty?) {
        GunManager.INSTANCE.getRandomGunId()
            .ifPresent {
                val gun = Registration.GUN_ITEM.get().createGun(it)
                this.setStackInHand(Hand.MAIN_HAND, gun)
            }
    }

    override fun initGoals() {
        goalSelector.add(0, SwimGoal(this))
        goalSelector.add(1, ShootTargetGoal(this))
        //todo 視界と記憶による攻撃/移動AI
        goalSelector.add(3, WanderAroundFarGoal(this, 1.0))
        goalSelector.add(8, LookAroundGoal(this))

        //todo 視界と記憶によるターゲットAI
        targetSelector.add(
            1, ActiveTargetGoal(
                this,
                PlayerEntity::class.java, true
            )
        )
        targetSelector.add(2, RevengeGoal(this))
    }

    override fun mobTick() {
        senseMem.tick()
    }

}