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
import net.sistr.flexibleguns.entity.util.SpeedChangeable
import net.sistr.flexibleguns.item.util.SoundData
import net.sistr.flexibleguns.item.util.SoundDataHolder
import net.sistr.flexibleguns.network.AmmoPacket
import net.sistr.flexibleguns.resource.GunManager
import net.sistr.flexibleguns.resource.GunSetting
import net.sistr.flexibleguns.util.Input
import net.sistr.flexibleguns.util.Inputable
import net.sistr.flexibleguns.util.ItemInstance
import net.sistr.flexibleguns.util.Zoomable
import java.util.function.Consumer
import java.util.function.Predicate

//todo 武器持ち変え時に移動速度が変わらないバグ/ejectAmmoに排出速度
//サーバーサイド専用
class GunInstance(
    private val holder: LivingEntity,
    private val stack: ItemStack,
    setting: GunSetting,
    nbt: NbtCompound
) : ItemInstance {

    constructor(holder: LivingEntity, stack: ItemStack) : this(
        holder,
        stack,
        GunManager.INSTANCE.getGunSetting(Identifier(stack.orCreateTag.getString("GunSettingId")))!!,
        stack.orCreateTag.getCompound("GunDate")
    )

    //基本
    private var heldHand: Hand? = null
    private var hold = false
    private var prevHold = false

    //性能
    val fireInterval = setting.fireInterval
    val shotsAmount = setting.shotsAmount
    val holdSpeed = setting.holdSpeed
    private var delay = 0f

    //弾丸
    val inAccuracy = setting.inAccuracy
    val velocity = setting.velocity
    val damage = setting.damage
    val headshotAmplifier = setting.headshotAmplifier
    val gravity = setting.gravity
    val decay = setting.decay
    val inertia = setting.inertia

    //リロード
    val reload = if (setting.reload != null) ReloadInstance(setting.reload, nbt) else null

    //todo instance化
    //ズーム
    val canZoom = setting.zoom != null
    val zoomInAccuracy = setting.zoom?.zoomInAccuracy ?: 0f
    val zoomSpeed = setting.zoom?.zoomSpeed ?: 0f
    private var zoom = false
    private var prevPressZoomKey = false

    //バースト
    val maxBurstCount = setting.burst?.maxBurstCount ?: 0
    val maxBurstDelay = setting.burst?.maxBurstDelay ?: 0f
    private var burstCount = 0
    private var burstDelay = 0f

    //音
    val shootSounds = setting.shootSounds

    //外から触る

    fun canShoot(): Boolean {
        if (reload == null) return true
        return hasAmmo(reload)
    }

    fun isZoom(): Boolean {
        return zoom
    }

    //メイン処理

    override fun tick() {
        if (0 < delay) {
            delay--
        }
        if (0 < burstDelay) {
            burstDelay--
        }
        this.heldHand = getHand()
        this.hold = heldHand != null
        if (hold) {
            if (!prevHold) {
                onHold()
            }
        } else if (prevHold) {
            unHold()
        }
        if (hold) {
            tickHold(heldHand!!)
        }
        prevHold = hold
    }

    private fun onHold() {
        updateMoveSpeed()
        if (reload != null) reload.prevAmmo = -1
    }

    private fun unHold() {
        if (reload != null) {
            cancelReload(reload)
            val action = reload.action
            if (action != null) {
                cancelCocking(reload, action)
            }
        }
        cancelBurst()
        if (zoom) {
            toggleZoom()
        }
        updateMoveSpeed()
    }

    private fun tickHold(heldHand: Hand) {
        val zoomTrigger = (holder as Inputable).getInputKeyFG(Input.ZOOM)
        if (zoomTrigger) {
            if (!prevPressZoomKey) {
                prevPressZoomKey = true
                if (canZoom /*&& reloadTime <= 0*/) {
                    toggleZoom()
                }
            }
        } else {
            prevPressZoomKey = false
        }
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
            shootSequence({
                it.delay <= 0
            }, {
                it.delay += it.fireInterval
                it.startBurst()
            })
        }
        if (0 < burstCount) {
            shootSequence({
                0 < it.burstCount && it.burstDelay <= 0
            }, {
                it.burstCount--
                it.burstDelay += it.maxBurstDelay
            })
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
                    tickCocking(reload, action)
                } else {
                    tickReload(reload)
                }
            } else if (action != null && 0 < action.cockingTime && shouldCockingToShoot(reload, action)) {
                tickCocking(reload, action)
            }
            if (reload.ammo != reload.prevAmmo) {
                writeNbtAmmo(reload)
                if (holder is ServerPlayerEntity) {
                    AmmoPacket.sendS2C(holder, reload.ammo)
                }
            }
            reload.prevAmmo = reload.ammo
        }
    }

    //todo 良くない
    private fun shootSequence(shootPredicate: Predicate<GunInstance>, shootSequence: Consumer<GunInstance>) {
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
            shoot(heldHand!!)

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

    private fun shoot(hand: Hand) {
        shootSounds.getSound(0)
            .forEach {
                playCustomSound(
                    it.sound, holder.soundCategory, it.volume, it.pitch
                )
            }

        for (i in 0 until shotsAmount) {
            val bullet = getBullet()
            val inAccuracy = if (zoom) zoomInAccuracy else this.inAccuracy
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

    private fun getBullet(): ProjectileEntity {
        val bullet = FGBulletEntity(holder)
        bullet.damage = damage
        bullet.headshotAmplifier = headshotAmplifier
        bullet.gravity = gravity
        bullet.decay = decay
        bullet.inertia = inertia
        return bullet
    }

    override fun remove() {
        //removeが実行されるときtickは実行されないため、holdは1tick前に持っていたかどうかになる
        if (hold) {
            unHold()
        }
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
        cancelBurst()
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
            /*if (zoom) {
                toggleZoom()
            }*/
        }
    }

    private fun tickReload(reload: ReloadInstance) {
        reload.reloadSounds.getSound(reload.reloadTime - 1)
            .forEach {
                playCustomSound(
                    it.sound, holder.soundCategory, it.volume, it.pitch
                )
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

    fun writeNbtAmmo(reload: ReloadInstance) {
        val nbt = this.stack.orCreateTag.getCompound("GunDate")
        nbt.putInt("ammo", reload.ammo)
        stack.orCreateTag.put("GunDate", nbt)
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

    private fun tickCocking(reload: ReloadInstance, action: ActionInstance) {
        if (action.chamberOpen) {
            action.closeSounds.getSound(action.cockingTime - 1)
                .forEach {
                    playCustomSound(
                        it.sound, holder.soundCategory, it.volume, it.pitch
                    )
                }
            if (action.closeLength < action.cockingTime++) {
                endCocking(reload, action)
            }
        } else {
            action.openSounds.getSound(action.cockingTime - 1)
                .forEach {
                    playCustomSound(
                        it.sound, holder.soundCategory, it.volume, it.pitch
                    )
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
        writeNbtCocking(action)
    }

    private fun closeAndLoadChamber(reload: ReloadInstance, action: ActionInstance) {
        action.chamberOpen = false
        action.chamberLoaded = true
        writeNbtCocking(action)
    }

    private fun clearChamber(reload: ReloadInstance, action: ActionInstance) {
        action.chamberLoaded = false
        writeNbtCocking(action)
    }

    fun writeNbtCocking(action: ActionInstance) {
        val nbt = this.stack.orCreateTag.getCompound("GunDate")
        nbt.putBoolean("chamberOpen", action.chamberOpen)
        nbt.putBoolean("chamberLoaded", action.chamberLoaded)
        stack.orCreateTag.put("GunDate", nbt)
    }

    //Zoom

    private fun toggleZoom() {
        zoom = !zoom
        holder.world.playSound(
            null, holder.x, holder.y, holder.z,
            SoundEvents.ENTITY_ENDER_DRAGON_FLAP, SoundCategory.PLAYERS, 0.5f, 2f
        )
        updateMoveSpeed()
        updateZoomState()
    }

    private fun updateZoomState() {
        if (holder is Zoomable) {
            (holder as Zoomable).setZoom_FG(zoom)
        }
    }

    private fun updateMoveSpeed() {
        val speedChanger = (holder as SpeedChangeable)
        val amp = getSpeedAmp()
        speedChanger.setSpeedAmp_FG(amp)
    }

    private fun getSpeedAmp(): Float {
        return if (hold) {
            if (zoom) {
                holdSpeed * zoomSpeed
            } else {
                holdSpeed
            }
        } else {
            1f
        }
    }

    //Burst

    private fun startBurst() {
        burstCount = maxBurstCount
        burstDelay = maxBurstDelay
    }

    private fun cancelBurst() {
        burstCount = 0
    }

    //その他

    private fun getHand(): Hand? {
        if (holder.mainHandStack === stack) {
            return Hand.MAIN_HAND
        } else if (holder.offHandStack === stack) {
            return Hand.OFF_HAND
        }
        return null
    }

    private fun playCustomSound(
        soundId: Identifier, category: SoundCategory, volume: Float, pitch: Float
    ) {
        val server = this.holder.server ?: return
        val pos = getHeldPos(heldHand ?: Hand.MAIN_HAND)
        server.playerManager.sendToAround(
            null, pos.x, pos.y, pos.z,
            if (volume > 1.0f) 16.0 * volume else 16.0,
            this.holder.world.registryKey,
            PlaySoundIdS2CPacket(soundId, category, pos, volume, pitch)
        )
    }

    private fun getHeldPos(hand: Hand): Vec3d {
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

    //todo data class化？

    //使わない場合があるフィールドをまとめている
    //またこれにアクセスするメソッドは、これを引数に取ることで、不注意によるぬるぽリスクを減らしている
    //これをもっと推し進めたらECSにたどり着くんやろな
    class ReloadInstance(param: GunSetting.ReloadParam, nbt: NbtCompound) {
        val maxAmmo: Int = param.maxAmmo
        val reloadLength: Int = param.reloadLength
        val reloadAmount: Int = param.reloadAmount
        val ejectAmmo: Boolean = param.ejectAmmo
        val action = if (param.action != null) ActionInstance(param.action, nbt) else null
        val reloadSounds =
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
                .build()
        var ammo = nbt.getInt("ammo")
        var prevAmmo = 0
        var reloadTime = 0
    }

    class ActionInstance(param: GunSetting.ActionParam, nbt: NbtCompound) {
        val type: GunSetting.ActionType = param.type
        val openLength: Int = param.openLength
        val closeLength: Int = param.closeLength
        val openSounds = SoundDataHolder.getBuilder()
            .setLength(param.openLength)
            .addSound(param.openLength - 1, SoundData(Identifier("block.piston.extend"), 2f, 1f))
            .build()
        val closeSounds = SoundDataHolder.getBuilder()
            .setLength(param.closeLength)
            .addSound(param.closeLength - 1, SoundData(Identifier("block.piston.contract"), 2f, 1f))
            .build()
        var chamberOpen = if (nbt.contains("chamberOpen")) nbt.getBoolean("chamberOpen") else true
        var chamberLoaded = nbt.getBoolean("chamberLoaded")
        var cockingTime = 0
    }

}