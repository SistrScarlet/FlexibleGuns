package net.sistr.flexibleguns.wip.util

import net.minecraft.entity.LivingEntity
import net.minecraft.util.math.Vec3d

class LagChecker {
    private var clientPos = Vec3d.ZERO
    private var serverPos = Vec3d.ZERO
    private var clientVec = Vec3d.ZERO
    private var serverVec = Vec3d.ZERO

    @Synchronized
    fun get(isClient: Boolean, type: Type): Vec3d {
        return if (type == Type.POS) {
            if (isClient) {
                clientPos
            } else {
                serverPos
            }
        } else {
            if (isClient) {
                clientVec
            } else {
                serverVec
            }
        }
    }

    @Synchronized
    fun set(vec: Vec3d, isClient: Boolean, type: Type) {
        if (type == Type.POS) {
            if (isClient) {
                clientPos = vec
            } else {
                serverPos = vec
            }
        } else {
            if (isClient) {
                clientVec = vec
            } else {
                serverVec = vec
            }
        }
    }

    @Synchronized
    fun tick(entity: LivingEntity) {
        this.set(entity.pos, entity.world.isClient, Type.POS)
        this.set(entity.velocity, entity.world.isClient, Type.VEC)
    }

    @Synchronized
    fun tick(isClient: Boolean, lagChecker: LagChecker) {
        this.set(if (isClient) lagChecker.clientPos else lagChecker.serverPos, isClient, Type.POS)
        this.set(if (isClient) lagChecker.clientVec else lagChecker.serverVec, isClient, Type.VEC)
    }

    @Synchronized
    fun print() {
        val pos = this.get(true, Type.POS).subtract(this.get(false, Type.POS)).length()
        val vec = this.get(true, Type.VEC).subtract(this.get(false, Type.VEC)).length()
        if (0.1 < pos && vec != 0.0) {
            println("pos : $pos + $clientPos + $serverPos")
            println("vec : $vec + $clientVec + $serverVec")
            println(pos / vec)
        }
    }

    enum class Type {
        POS,
        VEC
    }

}