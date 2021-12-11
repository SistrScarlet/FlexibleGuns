package net.sistr.flexibleguns.entity.goal

import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.goal.Goal
import net.minecraft.entity.mob.MobEntity
import net.sistr.flexibleguns.entity.util.HasSenseMemory
import net.sistr.flexibleguns.util.*
import java.util.*

//todo 4tick前の情報を元に狙いを定めるよう変更
class ShootTargetGoal(val owner: MobEntity) : Goal() {
    var target: LivingEntity? = null
    var gun: ShootableItem? = null

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
        val itemIns = ((stack as Any) as CustomItemStack).getItemInstanceFG()
        if (itemIns != null && itemIns is ShootableItem) {
            gun = itemIns
            target = owner.target
            if (target != null && target!!.isAlive && owner.visibilityCache.canSee(target)) {
                return true
            }
        }
        return false
    }

    override fun shouldContinue(): Boolean {
        if (!this.owner.isAlive || target == null || !target!!.isAlive) {
            return false
        }

        val stack = owner.mainHandStack
        val itemIns = ((stack as Any) as CustomItemStack).getItemInstanceFG()
        if (itemIns != null && itemIns is ShootableItem) {
            gun = itemIns
            target = owner.target
        }
        return false
    }

    override fun start() {

    }

    override fun tick() {
        (owner as Inputable).inputKeyFG(Input.FIRE, true)
        this.owner.lookAtEntity(target, 30f, 30f)
        this.owner.lookControl.lookAt(target, 30f, 30f)
        if (!(owner as ZoomableEntity).isZoom_FG() && gun is ZoomableItem && (gun as ZoomableItem).canZoom() && gun!!.canShoot()) {
            (owner as Inputable).inputKeyFG(Input.ZOOM, true)
        } else {
            (owner as Inputable).inputKeyFG(Input.ZOOM, false)
        }
    }

    override fun stop() {
        (owner as Inputable).inputKeyFG(Input.FIRE, false)
    }
}