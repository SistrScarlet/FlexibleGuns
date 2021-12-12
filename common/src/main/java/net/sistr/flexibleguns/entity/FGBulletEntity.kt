package net.sistr.flexibleguns.entity

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.damage.ProjectileDamageSource
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.projectile.ProjectileEntity
import net.minecraft.network.Packet
import net.minecraft.network.PacketByteBuf
import net.minecraft.particle.DustParticleEffect
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.Box
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3f
import net.minecraft.world.RaycastContext
import net.minecraft.world.World
import net.sistr.flexibleguns.entity.util.ShouldAccurateVelocitySync
import net.sistr.flexibleguns.network.BulletHitPacket
import net.sistr.flexibleguns.network.BulletSpawnPacket
import net.sistr.flexibleguns.setup.Registration
import net.sistr.flexibleguns.util.CollisionDetection
import net.sistr.flexibleguns.util.PrevEntityGetter

open class FGBulletEntity(type: EntityType<FGBulletEntity>, world: World) :
    ProjectileEntity(type, world),
    FGBullet, ShouldAccurateVelocitySync {
    var prevVelocity = Vec3d.ZERO
    var damage = 0f
    var headshotAmplifier = 1.0f
    var gravity = 0f

    //慣性率 現実の銃弾はバカ早いため、慣性の影響を大きく受ける前に着弾する
    //一方こちらではゲーム性のため、弾速を現実のソレと比べてバカ遅くしている
    //そのため、慣性の影響が現実のソレに比べると過剰に大きくなり不自然と感じるため、慣性率を設けて慣性の影響を減らす
    var inertia = 1.0f
    var decay = 100

    constructor(owner: LivingEntity) : this(Registration.BULLET_ENTITY.get(), owner.world) {
        updatePosition(owner.x, owner.eyeY - 0.1, owner.z)
        this.owner = owner
    }

    init {
        this.ignoreCameraFrustum = true
    }

    fun write(buf: PacketByteBuf) {
        buf.writeFloat(gravity)
    }

    fun read(buf: PacketByteBuf) {
        gravity = buf.readFloat()
    }

    override fun initDataTracker() {

    }

    override fun setVelocity(
        user: Entity,
        pitch: Float,
        yaw: Float,
        roll: Float,
        modifierZ: Float,
        modifierXYZ: Float
    ) {
        val rad = (Math.PI / 180).toFloat()
        val xV = -MathHelper.sin(yaw * rad) * MathHelper.cos(pitch * rad)
        val yV = -MathHelper.sin((pitch + roll) * rad)
        val zV = MathHelper.cos(yaw * rad) * MathHelper.cos(pitch * rad)
        this.setVelocity(xV.toDouble(), yV.toDouble(), zV.toDouble(), modifierZ, modifierXYZ)
        val vec3d = user.velocity
        velocity = velocity.add(
            vec3d.x * inertia,
            (if (user.isOnGround) 0.0 else vec3d.y) * inertia,
            vec3d.z * inertia
        )
    }

    @Environment(EnvType.CLIENT)
    override fun shouldRender(distance: Double): Boolean {
        var d = this.boundingBox.averageSideLength * 10.0
        if (java.lang.Double.isNaN(d)) {
            d = 1.0
        }
        d *= 64.0 * getRenderDistanceMultiplier()
        return distance < d * d
    }

    override fun tick() {
        if (decay < this.age) {
            this.discard()
            return
        }

        super.tick()
        val velocity = velocity

        val blockPos = blockPos
        val blockState = world.getBlockState(blockPos)
        if (!blockState.isAir) {
            val voxelShape = blockState.getCollisionShape(world, blockPos)
            if (!voxelShape.isEmpty) {
                val pos = pos
                for (box in voxelShape.boundingBoxes) {
                    if (box.offset(blockPos).contains(pos)) {
                        this.discard()
                        return
                    }
                }
            }
        }

        if (this.isTouchingWaterOrRain) {
            extinguish()
        }

        val pos = pos
        val newPos = pos.add(velocity)
        val hitResult = getHitResult(pos, newPos)
        onCollision(hitResult)
        showParticle(pos, hitResult.pos, 0.75f)
        //velocityDirty = true

        val dX: Double = velocity.x
        val dY: Double = velocity.y
        val dZ: Double = velocity.z
        val nX = this.x + dX
        val nY = this.y + dY
        val nZ = this.z + dZ
        val h = velocity.horizontalLength()
        val rad = (180.0 / Math.PI)
        yaw = (MathHelper.atan2(dX, dZ) * rad).toFloat()
        pitch = (MathHelper.atan2(dY, h) * rad).toFloat()
        pitch = updateRotation(prevPitch, pitch)
        yaw = updateRotation(prevYaw, yaw)
        var drag = this.getDragInAir()
        if (this.isTouchingWater) {
            for (o in 0..3) {
                val p = 0.25f
                world.addParticle(ParticleTypes.BUBBLE, nX - dX * p, nY - dY * p, nZ - dZ * p, dX, dY, dZ)
            }
            drag = this.getDragInWater()
        }
        this.velocity = velocity.multiply(drag.toDouble())
        this.prevVelocity = velocity
        if (!hasNoGravity()) {
            val v = this.velocity
            this.setVelocity(v.x, v.y - gravity, v.z)
        }
        updatePosition(nX, nY, nZ)
        checkBlockCollision()
    }

    fun showParticle(start: Vec3d, end: Vec3d, interval: Float) {
        val toVec = end.subtract(start)
        val toVecN = toVec.normalize()
        val length = toVec.length()
        var loop = 0
        while (0 < length - loop * interval) {
            val point = start.add(toVecN.multiply((loop * interval).toDouble()))
            world.addParticle(
                DustParticleEffect(Vec3f(0.9f, 0.9f, 0.9f), 0.5f), point.getX(), point.getY(), point.getZ(),
                0.0, 0.0, 0.0
            )
            loop++
        }
    }

    fun getHitResult(currentPosition: Vec3d, nextPosition: Vec3d): HitResult {
        val blockHitResult: HitResult = world.raycast(
            RaycastContext(
                currentPosition,
                nextPosition,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                this
            )
        )
        val nP = if (blockHitResult.type != HitResult.Type.MISS) blockHitResult.pos else nextPosition
        val entityHitResult: EntityHitResult? = this.getEntityCollision(currentPosition, nP)
        if (entityHitResult != null) {
            val target = entityHitResult.entity
            val owner = this.owner
            if (target !is PlayerEntity || owner !is PlayerEntity || owner.shouldDamagePlayer(target)
            ) {
                return entityHitResult
            }
        }
        return blockHitResult

    }

    fun getEntityCollision(currentPosition: Vec3d, nextPosition: Vec3d): EntityHitResult? {
        return CollisionDetection.getEntityHitResult(
            this.world,
            this,
            this.boundingBox,
            currentPosition,
            nextPosition,
            this.boundingBox.stretch(velocity).expand(1.0),
            4
        ) { entity: Entity? ->
            canHit(
                entity
            )
        }
    }

    override fun onEntityHit(entityHitResult: EntityHitResult) {
        super.onEntityHit(entityHitResult)
        val target = entityHitResult.entity
        val isHeadshot = isHeadshot(target)
        val damage = if (isHeadshot) damage * headshotAmplifier else damage
        val owner = this.owner
        val damageSource = getBulletDamageSource(this, owner ?: this)
        if (owner is LivingEntity) {
            owner.onAttacking(target)
        }
        val targetVelocity = target.velocity
        target.timeUntilRegen = 10
        if (target.damage(damageSource, damage)
            && owner is ServerPlayerEntity
        ) {
            BulletHitPacket.sendS2C(
                owner, when {
                    target is LivingEntity && target.health <= 0 -> BulletHitPacket.Type.KILL
                    isHeadshot -> BulletHitPacket.Type.HEADSHOT
                    else -> BulletHitPacket.Type.HIT
                }
            )
        }
        target.velocity = targetVelocity
        this.discard()
    }

    fun isHeadshot(target: Entity): Boolean {
        val prev = (target as PrevEntityGetter).getPrevEntity(4)
        val targetWidth = prev.box.xLength
        val targetHeight = prev.box.yLength
        val targetEyeHeight = prev.eyeHeight
        val boxSize: Double = Math.min(
            targetWidth / 2.0,
            targetHeight - targetEyeHeight
        )
        val boxPos = prev.pos.add(0.0, prev.eyeHeight.toDouble(), 0.0)
        val targetBox = Box(-boxSize, -boxSize, -boxSize, boxSize, boxSize, boxSize).offset(boxPos)
        val bulletBox = Box(this.x, this.y, this.z, this.x, this.y, this.z)
        val time = CollisionDetection.getHitTime(bulletBox, targetBox, this.velocity, prev.velocity)
        return time.isPresent
    }

    open fun getBulletDamageSource(projectile: FGBulletEntity, attacker: Entity): DamageSource {
        return ProjectileDamageSource("bullet", projectile, attacker).setProjectile()
    }

    override fun onBlockHit(blockHitResult: BlockHitResult) {
        super.onBlockHit(blockHitResult)
        val hitPos = blockHitResult.pos
        world.addParticle(ParticleTypes.CRIT, hitPos.x, hitPos.y, hitPos.z, 0.0, 0.0, 0.0)
    }

    fun getDragInAir(): Float {
        return 0.99f
    }

    fun getDragInWater(): Float {
        return 0.6f
    }

    override fun createSpawnPacket(): Packet<*> {
        return BulletSpawnPacket.createPacket(this)
    }

}