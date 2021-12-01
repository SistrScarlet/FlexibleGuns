package net.sistr.flexibleguns.util

import net.minecraft.entity.Entity
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import java.util.*
import java.util.function.Predicate

object CollisionDetection {

    fun getEntityHitResult(
        world: World,
        projectile: Entity,
        hitBox: Box,
        start: Vec3d,
        end: Vec3d,
        aroundBox: Box,
        targetPredicate: Predicate<Entity>
    ): EntityHitResult? {
        return getEntityHitResult(world, projectile, hitBox, start, end, aroundBox, 0, targetPredicate)
    }

    fun getEntityHitResult(
        world: World,
        projectile: Entity,
        hitBox: Box,
        start: Vec3d,
        end: Vec3d,
        aroundBox: Box,
        prev: Int,
        targetPredicate: Predicate<Entity>
    ): EntityHitResult? {
        var nearDist = Double.MAX_VALUE
        var nearTarget: Entity? = null
        var nearPos: Vec3d? = null
        val bulletVelocity = end.subtract(start)
        world.getOtherEntities(projectile, aroundBox, targetPredicate).forEach { aroundEntity ->
            //val box2 = aroundEntity.boundingBox.expand(0.30000001192092896)
            val prevEntity = (aroundEntity as PrevEntityGetter).getPrevEntity(prev)
            val hitPos = getHitPos(hitBox, bulletVelocity, prevEntity.box, prevEntity.velocity)
            //box2.raycast(vec3d, vec3d2)
            if (hitPos.isPresent) {
                val entityDist = start.squaredDistanceTo(hitPos.get())
                if (entityDist < nearDist) {
                    nearTarget = aroundEntity
                    nearDist = entityDist
                    nearPos = hitPos.get()
                }
            }
        }
        return if (nearTarget == null) {
            null
        } else {
            EntityHitResult(nearTarget, nearPos)
        }
    }

    private fun getHitPos(aBox: Box, aVec: Vec3d, bBox: Box, bVec: Vec3d): Optional<Vec3d> {
        val dist = getHitTime(aBox, bBox, aVec, bVec)
        if (!dist.isPresent) {
            return Optional.empty()
        }
        return Optional.of(aBox.center.add(aVec.multiply(dist.get().min)))
    }

    fun getHitTime(aBox: Box, bBox: Box, aVelocity: Vec3d, bVelocity: Vec3d): Optional<TimeRange> {
        val x = getHitRange(aBox.minX, aBox.maxX, bBox.minX, bBox.maxX, aVelocity.x, bVelocity.x)
        val y = getHitRange(aBox.minY, aBox.maxY, bBox.minY, bBox.maxY, aVelocity.y, bVelocity.y)
        val z = getHitRange(aBox.minZ, aBox.maxZ, bBox.minZ, bBox.maxZ, aVelocity.z, bVelocity.z)
        if (!x.isPresent || !y.isPresent || !z.isPresent) {
            return Optional.empty()
        }
        val xTR = x.get()
        val yTR = y.get()
        val zTR = z.get()
        return getNewRange(xTR, yTR).map { getNewRange(it, zTR).orElse(null) }
    }

    private fun getHitRange(
        aMin: Double,
        aMax: Double,
        bMin: Double,
        bMax: Double,
        aV: Double,
        bV: Double
    ): Optional<TimeRange> {

        //当たり判定の大きさRとその中心位置C
        val aR = (aMax - aMin) / 2.0
        val aC = aMin + aR
        val bR = (bMax - bMin) / 2.0
        val bC = bMin + bR

        //aが右にある場合は左右入れ替え
        if (bC < aC) {
            return getHitRange(bMin, bMax, aMin, aMax, bV, aV)
        }

        //当たったと判定する距離
        val r = aR + bR
        //aとbの距離
        val d = bC - aC
        //速度
        //この値が正の値の場合、距離が近づく
        //この値が負の値の場合、距離が離れる
        val v = aV - bV

        //速度がゼロの場合
        if (v == 0.0) {
            //範囲内の場合
            return if (d <= r) {
                Optional.of(TimeRange(0.0, 1.0))
            } else {
                Optional.empty()
            }
        }

        //ab間を移動するまでの時間
        //速度が負の場合はこちらも負になる
        val moveABTime = d / v
        //範囲Rを移動するまでの時間
        //速度が負の場合はこちらも負になる
        val moveRTime = r / v

        //範囲内の場合
        return if (d <= r) {
            //近づく場合
            if (0 < v) {
                Optional.of(TimeRange(0.0, Math.min(moveABTime + moveRTime, 1.0)))
            } else {//離れる場合 (time反転注意)
                Optional.of(TimeRange(0.0, Math.min(-moveRTime - -moveABTime, 1.0)))
            }
        } else {//範囲外の場合
            //近づく場合
            if (0 < v) {
                //Rに到達するまでの時間
                val moveToRTime = moveABTime - moveRTime
                //1以内にたどり着かない場合
                if (1.0 < moveToRTime) {
                    Optional.empty()
                } else {//1以内にたどり着く場合
                    Optional.of(TimeRange(moveToRTime, Math.min(moveToRTime + moveRTime * 2.0, 1.0)))
                }
            } else {//離れる場合
                Optional.empty()
            }
        }

    }

    data class TimeRange(val min: Double, val max: Double) {
        init {
            if (min < 0 || 1 < max) {
                println("$min : $max")
            }
        }
    }

    private fun getNewRange(a: TimeRange, b: TimeRange): Optional<TimeRange> {
        if (a.min < b.max && b.min < a.max) {
            return Optional.of(
                TimeRange(
                    Math.max(a.min, b.min),
                    Math.min(a.max, b.max)
                )
            )
        }
        return Optional.empty()
    }

}