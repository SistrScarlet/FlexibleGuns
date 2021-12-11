package net.sistr.flexibleguns.item

import net.minecraft.entity.LivingEntity
import net.minecraft.entity.projectile.ProjectileEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.packet.s2c.play.PlaySoundIdS2CPacket
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Arm
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.sistr.flexibleguns.entity.FGBulletEntity
import net.sistr.flexibleguns.item.util.SoundData
import net.sistr.flexibleguns.item.util.SoundDataHolder
import net.sistr.flexibleguns.network.AmmoPacket
import net.sistr.flexibleguns.resource.GunSetting
import net.sistr.flexibleguns.util.*
import java.util.function.Consumer
import java.util.function.Predicate

//todo ejectAmmoに排出速度
//サーバーサイド専用
class GunInstance(
    //銃本体
    val fireInterval: Float,
    val shotsAmount: Int,
    val holdSpeed: Float,
    //弾
    private val inAccuracy: Float,
    val velocity: Float,
    val damage: Float,
    val headshotAmplifier: Float,
    val gravity: Float,
    val decay: Int,
    val inertia: Float,
    val shootSounds: SoundDataHolder,
    //個別
    val reload: ReloadInstance?,
    val zoom: ZoomInstance?,
    val burst: BurstInstance?,
    //可変
    var delay: Float,
    var prevHold: Boolean
) : ItemInstance, ShootableItem, HasAmmoItem, ZoomableItem, SpeedChangeableItem {

    constructor(setting: GunSetting, nbt: NbtCompound) : this(
        setting.fireInterval,
        setting.shotsAmount,
        setting.holdSpeed,

        setting.inAccuracy,
        setting.velocity,
        setting.damage,
        setting.headshotAmplifier,
        setting.gravity,
        setting.decay,
        setting.inertia,
        setting.shootSounds,

        if (setting.reload != null) ReloadInstance(setting.reload, nbt) else null,
        if (setting.zoom != null) ZoomInstance(setting.zoom, nbt) else null,
        if (setting.burst != null) BurstInstance(setting.burst, nbt) else null,

        nbt.getFloat("delay"),
        nbt.getBoolean("prevHold")
    )


    //外から触る

    override fun canShoot(): Boolean {
        if (reload == null) return true
        return hasAmmo(reload)
    }

    override fun getInAccuracy(holder: LivingEntity): Float {
        if (zoom != null && (holder as ZoomableEntity).isZoom_FG()) {
            return zoom.zoomInAccuracy
        }
        return inAccuracy
    }

    override fun getAmmoAmount(): Int {
        if (reload != null) {
            return reload.ammo
        }
        return 0
    }

    override fun setAmmoAmount(amount: Int) {
        if (reload != null) {
            reload.ammo = amount
        }
    }

    override fun canZoom(): Boolean {
        return zoom != null
    }

    override fun zoom(holder: LivingEntity) {
        toggleZoom(holder)
    }

    override fun unZoom(holder: LivingEntity) {
        toggleZoom(holder)
    }

    fun toggleZoom(holder: LivingEntity) {
        holder.world.playSound(
            null, holder.x, holder.y, holder.z,
            SoundEvents.ENTITY_ENDER_DRAGON_FLAP, SoundCategory.PLAYERS, 0.5f, 2f
        )
    }

    override fun getDisplayZoomAmp(holder: LivingEntity): Float {
        if (zoom != null && (holder as ZoomableEntity).isZoom_FG()) {
            return zoom.zoomAmount
        }
        return 1f
    }

    override fun getSpeedAmp(holder: LivingEntity): Float {
        return if (zoom != null && (holder as ZoomableEntity).isZoom_FG()) {
            zoom.zoomSpeed
        } else {
            holdSpeed
        }
    }

    //メイン処理

    override fun save(stack: ItemStack) {
        val stackNbt = stack.orCreateTag
        val gunDate = stackNbt.getCompound("GunDate")
        gunDate.putFloat("delay", delay)
        gunDate.putBoolean("prevHold", prevHold)
        reload?.save(gunDate)
        zoom?.save(gunDate)
        burst?.save(gunDate)
        stackNbt.put("GunDate", gunDate)
    }

    override fun copy(stack: ItemStack): ItemInstance {
        return GunInstance(
            fireInterval,
            shotsAmount,
            holdSpeed,
            inAccuracy,
            velocity,
            damage,
            headshotAmplifier,
            gravity,
            decay,
            inertia,
            shootSounds,
            reload?.copy(),
            zoom?.copy(),
            burst?.copy(),
            delay,
            prevHold
        )
    }

    override fun startTick(stack: ItemStack, holder: LivingEntity, heldHand: Hand?) {
    }

    override fun tick(stack: ItemStack, holder: LivingEntity, heldHand: Hand?) {
        if (holder.world.isClient) {
            return
        }
        if (0 < delay) {
            delay--
        }
        if (burst != null) {
            if (0 < burst.burstDelay) {
                burst.burstDelay--
            }
        }
        val hold = heldHand == Hand.MAIN_HAND//heldHand != null
        if (hold) {
            if (!prevHold) {
                onHold(holder, heldHand!!)
            }
            tickHold(holder, heldHand!!)
        } else if (prevHold) {
            unHold(holder)
        }
        prevHold = hold
    }

    override fun endTick(stack: ItemStack, holder: LivingEntity, heldHand: Hand?) {

    }

    fun onHold(holder: LivingEntity, heldHand: Hand) {
        if (reload != null) reload.prevAmmo = -1
    }

    //インスタンスが破棄された時も実行されるため、確実性薄し
    fun unHold(holder: LivingEntity) {
        if (reload != null) {
            cancelReload(reload)
            val action = reload.action
            if (action != null) {
                cancelCocking(reload, action)
            }
        }
        if (burst != null) {
            cancelBurst(burst)
        }
    }

    private fun tickHold(holder: LivingEntity, heldHand: Hand) {
        if (reload != null) {
            if (isAmmoFull(reload)) {
                val action = reload.action
                if (action != null) {
                    if (shouldCockingToShoot(reload, action)) {
                        startCocking(reload, action)
                    }
                }
            } else {
                val reloadTrigger = (holder as Inputable).getInputKeyFG(Input.RELOAD)
                if (reloadTrigger) {
                    if (!isReloading(reload) && reload.ejectAmmo) {
                        reload.ammo = 0
                    }
                    startReload(reload)
                }
            }
        }
        val fireTrigger = (holder as Inputable).getInputKeyFG(Input.FIRE)
        if (fireTrigger) {
            shootSequence(
                holder,
                heldHand,
                { it.delay <= 0 },
                {
                    it.delay += it.fireInterval
                    if (burst != null) it.startBurst(burst)
                }
            )
        }
        if (burst != null) {
            if (0 < burst.burstCount) {
                shootSequence(
                    holder,
                    heldHand,
                    { 0 < burst.burstCount && burst.burstDelay <= 0 },
                    {
                        burst.burstCount--
                        burst.burstDelay += burst.maxBurstDelay
                    }
                )
            }
        }

        if (reload != null) {
            val action = reload.action
            if (isReloading(reload)) {
                if (isAmmoFull(reload)) {
                    cancelReload(reload)
                }
                //ActionTypeによってはリロードするために開く必要がある
                else if (action != null && shouldCockingToReload(reload, action)
                ) {
                    tickCocking(holder, heldHand, reload, action)
                } else {
                    tickReload(holder, heldHand, reload)
                }
            } else if (action != null && 0 < action.cockingTime && shouldCockingToShoot(reload, action)) {
                tickCocking(holder, heldHand, reload, action)
            }
            if (reload.ammo != reload.prevAmmo) {
                if (holder is ServerPlayerEntity) {
                    AmmoPacket.sendS2C(holder, reload.ammo)
                }
            }
            reload.prevAmmo = reload.ammo
        }
    }

    //todo 良くない
    private fun shootSequence(
        holder: LivingEntity,
        heldHand: Hand,
        shootPredicate: Predicate<GunInstance>,
        shootSequence: Consumer<GunInstance>
    ) {
        if (reload != null) {
            if (!hasAmmo(reload)) {
                onGunOut(reload)
                return
            }
            cancelReload(reload)
            val action = reload.action
            if (action != null) {
                if (shouldCockingToShoot(reload, action)) {
                    startCocking(reload, action)
                    return
                }
            }
        }
        while (shootPredicate.test(this)) {
            shoot(holder, heldHand)

            shootSequence.accept(this)

            if (reload != null) {
                reload.ammo--
                if (!hasAmmo(reload)) {
                    onGunOut(reload)
                    return
                }
                val action = reload.action
                if (action != null) {
                    if (action.type == GunSetting.ActionType.BOLT
                        || action.type == GunSetting.ActionType.PUMP
                    ) {
                        clearChamber(reload, action)
                    }
                    if (shouldCockingToShoot(reload, action)) {
                        startCocking(reload, action)
                    }
                }
            }
        }
    }

    private fun shoot(holder: LivingEntity, hand: Hand) {
        shootSounds.getSound(0)
            .forEach {
                playCustomSound(holder, hand, it.sound, holder.soundCategory, it.volume, it.pitch)
            }

        for (i in 0 until shotsAmount) {
            val bullet = getBullet(holder)
            val inAccuracy =
                if (zoom != null && (holder as ZoomableEntity).isZoom_FG()) zoom.zoomInAccuracy else this.inAccuracy
            val halfPi = (Math.PI / 2).toFloat()
            //ちょっと中心に寄せる
            bullet.setProperties(
                holder,
                holder.pitch + inAccuracy * ((1 - MathHelper.sin(holder.random.nextFloat() * halfPi)) * 2 - 1),
                holder.yaw + inAccuracy * ((1 - MathHelper.sin(holder.random.nextFloat() * halfPi)) * 2 - 1),
                0.0f,
                velocity,
                0f
            )
            holder.world.spawnEntity(bullet)
        }

    }

    private fun getBullet(holder: LivingEntity): ProjectileEntity {
        val bullet = FGBulletEntity(holder)
        bullet.damage = damage
        bullet.headshotAmplifier = headshotAmplifier
        bullet.gravity = gravity
        bullet.decay = decay
        bullet.inertia = inertia
        return bullet
    }

    //Reload

    private fun onGunOut(reload: ReloadInstance) {
        startReload(reload)
        if (reload.action != null) {
            clearChamber(reload, reload.action)
            if (reload.action.type == GunSetting.ActionType.SLIDE) {
                openChamber(reload, reload.action)
            }
        }
        if (burst != null) {
            cancelBurst(burst)
        }
    }

    private fun isAmmoFull(reload: ReloadInstance): Boolean {
        return reload.maxAmmo <= reload.ammo
    }

    private fun hasAmmo(reload: ReloadInstance): Boolean {
        return 0 < reload.ammo
    }

    private fun isReloading(reload: ReloadInstance): Boolean {
        return 0 < reload.reloadTime
    }

    private fun startReload(reload: ReloadInstance) {
        if (!isReloading(reload)) {
            reload.reloadTime = 1
        }
    }

    private fun tickReload(holder: LivingEntity, heldHand: Hand, reload: ReloadInstance) {
        reload.reloadSounds.getSound(reload.reloadTime - 1)
            .forEach {
                playCustomSound(holder, heldHand, it.sound, holder.soundCategory, it.volume, it.pitch)
            }
        if (reload.reloadLength < reload.reloadTime++) {
            endReload(reload)
        }
    }

    private fun endReload(reload: ReloadInstance) {
        reload.ammo = Math.min(reload.ammo + reload.reloadAmount, reload.maxAmmo)
        reload.reloadTime = 0
        if (!isAmmoFull(reload)) {
            startReload(reload)
        } else if (reload.action != null && shouldCockingToShoot(reload, reload.action)) {
            startCocking(reload, reload.action)
        }
    }

    private fun cancelReload(reload: ReloadInstance) {
        reload.reloadTime = 0
    }

    //Cocking

    private fun shouldCockingToReload(reload: ReloadInstance, action: ActionInstance): Boolean {
        if (action.type == GunSetting.ActionType.BOLT || action.type == GunSetting.ActionType.BREAK) {
            return !action.chamberOpen
        }
        return false
    }

    private fun shouldCockingToShoot(reload: ReloadInstance, action: ActionInstance): Boolean {
        return action.chamberOpen || !action.chamberLoaded
    }

    private fun startCocking(reload: ReloadInstance, action: ActionInstance) {
        if (action.cockingTime == 0) {
            action.cockingTime = 1
        }
    }

    private fun tickCocking(holder: LivingEntity, heldHand: Hand, reload: ReloadInstance, action: ActionInstance) {
        if (action.chamberOpen) {
            action.closeSounds.getSound(action.cockingTime - 1)
                .forEach {
                    playCustomSound(holder, heldHand, it.sound, holder.soundCategory, it.volume, it.pitch)
                }
            if (action.closeLength < action.cockingTime++) {
                endCocking(reload, action)
            }
        } else {
            action.openSounds.getSound(action.cockingTime - 1)
                .forEach {
                    playCustomSound(holder, heldHand, it.sound, holder.soundCategory, it.volume, it.pitch)
                }
            if (action.openLength < action.cockingTime++) {
                endCocking(reload, action)
            }
        }
    }

    private fun endCocking(reload: ReloadInstance, action: ActionInstance) {
        action.cockingTime = 0
        if (action.chamberOpen) {
            closeAndLoadChamber(reload, action)
        } else {
            openChamber(reload, action)
        }
        if (!isReloading(reload) && shouldCockingToShoot(reload, action)) {
            startCocking(reload, action)
        }
    }

    private fun cancelCocking(reload: ReloadInstance, action: ActionInstance) {
        action.cockingTime = 0
    }

    private fun openChamber(reload: ReloadInstance, action: ActionInstance) {
        action.chamberOpen = true
    }

    private fun closeAndLoadChamber(reload: ReloadInstance, action: ActionInstance) {
        action.chamberOpen = false
        action.chamberLoaded = true
    }

    private fun clearChamber(reload: ReloadInstance, action: ActionInstance) {
        action.chamberLoaded = false
    }

    //Burst

    private fun startBurst(burst: BurstInstance) {
        burst.burstCount = burst.maxBurstCount
        burst.burstDelay = burst.maxBurstDelay
    }

    private fun cancelBurst(burst: BurstInstance) {
        burst.burstCount = 0
    }

    //その他

    private fun playCustomSound(
        holder: LivingEntity, heldHand: Hand?,
        soundId: Identifier, category: SoundCategory, volume: Float, pitch: Float
    ) {
        val server = holder.server ?: return
        val pos = getHeldPos(holder, heldHand ?: Hand.MAIN_HAND)
        server.playerManager.sendToAround(
            null, pos.x, pos.y, pos.z,
            if (volume > 1.0f) 16.0 * volume else 16.0,
            holder.world.registryKey,
            PlaySoundIdS2CPacket(soundId, category, pos, volume, pitch)
        )
    }

    private fun getHeldPos(holder: LivingEntity, hand: Hand): Vec3d {
        return holder.getCameraPosVec(1f)
            .add(
                getRotationVector(
                    holder.getPitch(1f),
                    holder.getYaw(1f)
                            + (if (hand == Hand.MAIN_HAND) 20f else -20f)
                            * (if (holder.mainArm == Arm.RIGHT) 1 else -1)
                ).multiply(0.5)
            )
    }

    private fun getRotationVector(pitch: Float, yaw: Float): Vec3d {
        val p = pitch * 0.017453292f
        val y = -yaw * 0.017453292f
        val h = MathHelper.cos(y)
        val i = MathHelper.sin(y)
        val j = MathHelper.cos(p)
        val k = MathHelper.sin(p)
        return Vec3d((i * j).toDouble(), (-k).toDouble(), (h * j).toDouble())
    }

    //使わない場合があるフィールドをまとめている
    //またこれにアクセスするメソッドは、これを引数に取ることで、不注意によるぬるぽリスクを減らしている
    //これをもっと推し進めたらECSにたどり着くんやろな
    data class ReloadInstance(
        val maxAmmo: Int,
        val reloadLength: Int,
        val reloadAmount: Int,
        val ejectAmmo: Boolean,
        val action: ActionInstance?,
        val reloadSounds: SoundDataHolder,
        var ammo: Int,
        var reloadTime: Int
    ) {
        //tmp
        constructor(param: GunSetting.ReloadParam, nbt: NbtCompound) : this(
            param.maxAmmo,
            param.reloadLength,
            param.reloadAmount,
            param.ejectAmmo,
            if (param.action != null) ActionInstance(param.action, nbt) else null,
            if (param.action == null || param.action.type == GunSetting.ActionType.SLIDE) SoundDataHolder.getBuilder()
                .setLength(param.reloadLength)
                .addSound(0, SoundData(Identifier("item.flintandsteel.use"), 1f, 1f))
                .addSound(2, SoundData(Identifier("block.wooden_door.open"), 2f, 1f))
                .addSound(
                    (param.reloadLength * 0.8f - 2).toInt(),
                    SoundData(Identifier("item.flintandsteel.use"), 1f, 1f)
                )
                .addSound(
                    (param.reloadLength * 0.8f - 1).toInt(),
                    SoundData(Identifier("entity.generic.hurt"), 0f, 1f)
                )
                .addSound(
                    (param.reloadLength * 0.8f).toInt(),
                    SoundData(Identifier("block.wooden_door.close"), 2f, 1f)
                )
                .build()
            else SoundDataHolder.getBuilder()
                .setLength(param.reloadLength)
                .addSound(param.reloadLength - 1, SoundData(Identifier("block.note_block.hat"), 1f, 1f))
                .build(),
            nbt.getInt("ammo"),
            nbt.getInt("reloadTime")
        )

        var prevAmmo = 0

        fun save(nbt: NbtCompound) {
            nbt.putInt("ammo", ammo)
            nbt.putInt("reloadTime", reloadTime)
            action?.save(nbt)
        }

        fun copy(): ReloadInstance {
            return ReloadInstance(
                maxAmmo,
                reloadLength,
                reloadAmount,
                ejectAmmo,
                action?.copy(),
                reloadSounds,
                ammo,
                reloadTime
            )
        }
    }

    data class ActionInstance(
        val type: GunSetting.ActionType,
        val openLength: Int,
        val closeLength: Int,
        val openSounds: SoundDataHolder,
        val closeSounds: SoundDataHolder,
        var chamberOpen: Boolean,
        var chamberLoaded: Boolean,
        var cockingTime: Int
    ) {
        constructor(param: GunSetting.ActionParam, nbt: NbtCompound) : this(
            param.type, param.openLength, param.closeLength,
            SoundDataHolder.getBuilder()
                .setLength(param.openLength)
                .addSound(param.openLength - 1, SoundData(Identifier("block.piston.extend"), 2f, 1f))
                .build(),
            SoundDataHolder.getBuilder()
                .setLength(param.closeLength)
                .addSound(param.closeLength - 1, SoundData(Identifier("block.piston.contract"), 2f, 1f))
                .build(),
            if (nbt.contains("chamberOpen")) nbt.getBoolean("chamberOpen") else true,
            nbt.getBoolean("chamberLoaded"),
            nbt.getInt("cockingTime")
        )

        fun save(nbt: NbtCompound) {
            nbt.putBoolean("chamberOpen", chamberOpen)
            nbt.putBoolean("chamberLoaded", chamberLoaded)
            nbt.putInt("cockingTime", cockingTime)
        }

        fun copy(): ActionInstance {
            return ActionInstance(
                type,
                openLength,
                closeLength,
                openSounds,
                closeSounds,
                chamberOpen,
                chamberLoaded,
                cockingTime
            )
        }
    }

    data class ZoomInstance(
        val zoomInAccuracy: Float,
        val zoomSpeed: Float,
        val zoomAmount: Float
    ) {
        constructor(param: GunSetting.ZoomParam, nbt: NbtCompound) : this(
            param.zoomInAccuracy,
            param.zoomSpeed,
            param.zoomAmount
        )

        fun save(nbt: NbtCompound) {

        }

        fun copy(): ZoomInstance {
            return ZoomInstance(zoomInAccuracy, zoomSpeed, zoomAmount)
        }
    }

    data class BurstInstance(
        val maxBurstCount: Int,
        val maxBurstDelay: Float,
        var burstCount: Int,
        var burstDelay: Float
    ) {
        constructor(param: GunSetting.BurstParam, nbt: NbtCompound) : this(
            param.maxBurstCount,
            param.maxBurstDelay,
            nbt.getInt("burstCount"),
            nbt.getFloat("burstDelay")
        )

        fun save(nbt: NbtCompound) {
            nbt.putInt("burstCount", burstCount)
            nbt.putFloat("burstDelay", burstDelay)
        }

        fun copy(): BurstInstance {
            return BurstInstance(maxBurstCount, maxBurstDelay, burstCount, burstDelay)
        }
    }

}