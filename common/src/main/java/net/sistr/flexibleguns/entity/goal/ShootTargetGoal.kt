package net.sistr.flexibleguns.entity.goal

import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.goal.Goal
import net.minecraft.entity.mob.MobEntity
import net.sistr.flexibleguns.entity.util.HasSenseMemory
import net.sistr.flexibleguns.item.GunInstance
import net.sistr.flexibleguns.util.ItemInstanceHolder
import net.sistr.flexibleguns.util.Input
import net.sistr.flexibleguns.util.Inputable
import java.util.*

//todo 4tick前の情報を元に狙いを定めるよう変更
class ShootTargetGoal(val owner: MobEntity) : Goal() {
    var target: LivingEntity? = null
    var gun: GunInstance? = null

    init {
        controls = EnumSet.of(Control.LOOK, Control.MOVE)
    }

    override fun canStart(): Boolean {
        if (this.owner.isDead || !this.owner.isAlive) {
            return false
        }
        if (owner is HasSenseMemory) {
            owner.getSenseMemory().sense()
        }
        val stack = owner.mainHandStack
        return (owner as ItemInstanceHolder).getItemInstanceFG(stack)
            .filter { it is GunInstance }
            .map { it as GunInstance }
            .map {
                gun = it
                target = owner.target
                target != null && target!!.isAlive && owner.visibilityCache.canSee(target)
            }.orElse(false)
    }

    override fun shouldContinue(): Boolean {
        return !this.owner.isDead && this.owner.isAlive
                && target != null && target!!.isAlive && owner.visibilityCache.canSee(target)
    }

    override fun start() {

    }

    override fun tick() {
        (owner as Inputable).inputKeyFG(Input.FIRE, true)
        this.owner.lookAtEntity(target, 30f, 30f)
        this.owner.lookControl.lookAt(target, 30f, 30f)
        if (!gun!!.isZoom() && gun!!.canShoot()) {
            (owner as Inputable).inputKeyFG(Input.ZOOM, true)
        } else {
            (owner as Inputable).inputKeyFG(Input.ZOOM, false)
        }
    }

    override fun stop() {
        (owner as Inputable).inputKeyFG(Input.FIRE, false)
    }
}