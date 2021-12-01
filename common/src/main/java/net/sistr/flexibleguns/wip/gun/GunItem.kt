package net.sistr.flexibleguns.wip.gun

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Hand
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import java.util.*
import java.util.function.Consumer

//銃の性能はインスタンスが持ち、銃の状態はNBTで持つ
//処理は鯖側のみ
//todo 射撃の音声の逐次化
//todo 持ち手のスワップとか両手に銃持った時のMoveとかリロード音とかがおかしい
class GunItem(private val settings: GunSettings) {

    @Environment(EnvType.CLIENT)
    fun addInformation(stack: ItemStack, tooltip: MutableList<Text>) {
        tooltip.add(
            TranslatableText("tooltip.endlessrain.gun_item.bullets_amount")
                .append(": " + GunNBTUtil.getLoadedBullets(stack.orCreateTag) + " / " + settings.loadableBulletsAmount)
        )
        /*if (!(InputMappings.isKeyDown(Minecraft.getInstance().getMainWindow().getHandle(), 340)
                || InputMappings.isKeyDown(Minecraft.getInstance().getMainWindow().getHandle(), 344))) {
            tooltip.add(new TranslatableText("tooltip.endlessrain.generic.shift_for_detail"));
            return;
        }*/tooltip.add(
            TranslatableText("tooltip.endlessrain.gun_item.damage")
                .append(": " + settings.damage)
        )
        if (0f != settings.headShotBonus) {
            tooltip.add(
                TranslatableText("tooltip.endlessrain.gun_item.head_shot")
                    .append(": " + (settings.damage + settings.headShotBonus))
            )
        }
        if (0 < settings.piercingLevel) {
            tooltip.add(
                TranslatableText("tooltip.endlessrain.gun_item.piercing")
                    .append(": " + settings.piercingLevel)
            )
        }
        if (1 < settings.shotAmount) {
            tooltip.add(
                TranslatableText("tooltip.endlessrain.gun_item.shot_amount")
                    .append(": " + settings.shotAmount)
            )
        }
        tooltip.add(
            TranslatableText("tooltip.endlessrain.gun_item.rate")
                .append(": " + (settings.rate * if (0 < settings.burstAmount) settings.burstAmount + 1 else 1).toInt())
        )
        if (0 < settings.burstAmount) {
            tooltip.add(
                TranslatableText("tooltip.endlessrain.gun_item.burst_amount")
                    .append(": " + (settings.burstAmount + 1))
            )
            tooltip.add(
                TranslatableText("tooltip.endlessrain.gun_item.burst_interval")
                    .append(": " + settings.burstInterval)
            )
        }
        tooltip.add(
            TranslatableText("tooltip.endlessrain.gun_item.velocity")
                .append(": " + settings.velocity)
        )
        tooltip.add(
            TranslatableText("tooltip.endlessrain.gun_item.inaccuracy")
                .append(": " + settings.inaccuracy)
        )
        tooltip.add(
            TranslatableText("tooltip.endlessrain.gun_item.range")
                .append(": " + (settings.velocity * settings.decayDuration).toInt())
        )
        if (0 < settings.reloadDuration) {
            tooltip.add(
                TranslatableText("tooltip.endlessrain.gun_item.reload")
                    .append(": " + if (settings.isShouldReloadMagazine) settings.reloadDuration else settings.reloadDuration * settings.loadableBulletsAmount)
            )
        }
        if (0 < settings.bulletCost) {
            tooltip.add(
                TranslatableText("tooltip.endlessrain.gun_item.cost")
                    .append(": " + settings.bulletCost * settings.loadableBulletsAmount)
            )
        }
        if (0 < settings.move) {
            tooltip.add(
                TranslatableText("tooltip.endlessrain.gun_item.move")
                    .append(": " + settings.move)
            )
        }
        if (0 < settings.zoomFov) {
            tooltip.add(
                TranslatableText("tooltip.endlessrain.gun_item.zoom_inaccuracy")
                    .append(": " + settings.zoomInaccuracy)
            )
            tooltip.add(
                TranslatableText("tooltip.endlessrain.gun_item.zoom_move")
                    .append(": " + settings.zoomMove)
            )
        }
        if (settings.isNeedCocking) {
            tooltip.add(
                TranslatableText("tooltip.endlessrain.gun_item.cocking_duration")
                    .append(": " + (settings.openDuration + settings.closeDuration))
            )
        }
        if (settings.inaccuracy * 2f != settings.akimboInaccuracy) {
            tooltip.add(
                TranslatableText("tooltip.endlessrain.gun_item.akimbo_inaccuracy")
                    .append(": " + settings.akimboInaccuracy)
            )
        }
        if (settings.zoomInaccuracy * 2f != settings.akimboZoomInaccuracy) {
            tooltip.add(
                TranslatableText("tooltip.endlessrain.gun_item.akimbo_zoom_inaccuracy")
                    .append(": " + settings.akimboZoomInaccuracy)
            )
        }
        if (settings.reloadDuration * 2 != settings.akimboReloadDuration) {
            tooltip.add(
                TranslatableText("tooltip.endlessrain.gun_item.akimbo_reload")
                    .append(": " + settings.akimboReloadDuration)
            )
        }
    }

    /*public Multimap<String, AttributeModifier> getAttributeModifiers(EquipmentSlotType slot, ItemStack stack) {
        Multimap<String, AttributeModifier> map = super.getAttributeModifiers(slot, stack);
        if (slot == EquipmentSlotType.MAINHAND || slot == EquipmentSlotType.OFFHAND) {
            if (0 < settings.getZoomFov() && isZoom(stack.getOrCreateTag())) {
                map.put(SharedMonsterAttributes.MOVEMENT_SPEED.getName(),
                        new AttributeModifier(ZOOM_SPEED_ID, "Gun Zoom speed",
                                settings.getZoomMove(), AttributeModifier.Operation.ADDITION));
            } else if (settings.getMove() != 0) {
                map.put(SharedMonsterAttributes.MOVEMENT_SPEED.getName(),
                        new AttributeModifier(MOVE_SPEED_ID, "Gun Move speed",
                                settings.getMove(), AttributeModifier.Operation.ADDITION));
            }
        }
        return map;
    }*/

    fun onRightClick(world: World, player: PlayerEntity, hand: Hand): Boolean {
        val gun = player.getStackInHand(hand)
        if (world.isClient) {
            pressUseItemKey(true)
        } else {
            setShooting(gun.orCreateTag, true)
        }
        return true
    }

    fun tick(world: World, shooter: LivingEntity, stack: ItemStack) {
        if (world.isClient) {
            return
        }
        val gun = stack.orCreateTag

        //ディレイ中なら常にtick
        if (isShootDelay(gun)) {
            tickShootDelay(gun)
        }
        if (isBurstDelay(gun)) {
            tickBurstDelay(gun)
        }
        val holding = isHolding(shooter, stack)
        val prevHolding = GunNBTUtil.isHolding(gun)
        //手に持っていない場合は射撃しない
        if (!holding) {
            if (prevHolding) stopTicking(shooter, gun)
            return
        } else if (!prevHolding) {
            GunNBTUtil.setHolding(gun, true)
        }

        //弾がある時に射撃しようとしたら
        if (isShooting(gun)) {
            if (isAmmoLoaded(gun)) {
                //装填されてなかったらコッキング
                if (settings.isNeedCocking && !isLoadedChamber(gun) && !isCocking(gun)) {
                    startCocking(world, shooter, gun)
                }
                //リロードを解除
                if (isReloading(gun)) {
                    stopReload(gun)
                    return
                }
            } else {
                //閉じているとリロードできず、閉じているならコッキング
                if (settings.isNeedCocking && settings.isCockingPerReload && isCloseChamber(gun) && !isCocking(gun)) {
                    startCocking(world, shooter, gun)
                }
                //弾が無いときに射撃しようとしたらリロード
                if (!isReloading(gun)) {
                    if (hasAmmo(shooter)) {
                        startReload(world, shooter, gun)
                    }
                    return
                }
            }
        }
        if (isCocking(gun)) {
            cocking(world, shooter, gun)
        } else if (isReloading(gun)) {
            reloading(world, shooter, gun)
        } else {
            if (isShooting(gun) && canShooting(gun)) {
                shooting(world, shooter, stack, gun)
            }
        }
        if (canBurst(gun)) {
            burst(world, shooter, gun)
        }
    }

    fun isShootDelay(gun: NbtCompound): Boolean {
        return 0 < GunNBTUtil.getShootDelay(gun)
    }

    fun tickShootDelay(gun: NbtCompound) {
        GunNBTUtil.setShootDelay(gun, GunNBTUtil.getShootDelay(gun) - 1f)
    }

    fun isBurstDelay(gun: NbtCompound): Boolean {
        return 0 < GunNBTUtil.getBurstDelay(gun)
    }

    fun tickBurstDelay(gun: NbtCompound) {
        GunNBTUtil.setBurstDelay(gun, GunNBTUtil.getBurstDelay(gun) - 1)
    }

    fun stopTicking(shooter: LivingEntity, gun: NbtCompound) {
        GunNBTUtil.setHolding(gun, false)
        setShooting(gun, false)
        if (0 < settings.reloadDuration) stopReload(gun)
        if (settings.isNeedCocking && isCocking(gun)) {
            stopCocking(gun)
        }
        if (settings.zoomFov != 0f && isZoom(gun)) zoom(shooter, gun, false)
        if (0 < settings.burstAmount) GunNBTUtil.setBurstCount(gun, 0)
    }

    fun isAmmoLoaded(gun: NbtCompound): Boolean {
        return 0 < GunNBTUtil.getLoadedBullets(gun)
    }

    fun isReloading(gun: NbtCompound): Boolean {
        return 0 < GunNBTUtil.getReloadTime(gun)
    }

    fun startReload(world: World, shooter: LivingEntity, gun: NbtCompound) {
        if (isZoom(gun)) {
            zoom(shooter, gun, false)
        }
        if (0 < GunNBTUtil.getReloadTime(gun)) {
            return
        }
        //弾がフルでなく弾を持っているなら
        if (GunNBTUtil.getLoadedBullets(gun) < settings.loadableBulletsAmount && hasAmmo(shooter)) {
            //両手にアイテムを持っている場合はリロードに時間が掛かる
            if (!shooter.offHandStack.isEmpty && !shooter.mainHandStack.isEmpty) {
                GunNBTUtil.setReloadTime(gun, settings.akimboReloadDuration)
            } else {
                GunNBTUtil.setReloadTime(gun, settings.reloadDuration)
            }
            //開放状態でないとリロードできず、閉鎖状態ならコッキング
            if (settings.isCockingPerReload && isCloseChamber(gun)) {
                startCocking(world, shooter, gun)
            }
        } //フルかつコッキングされておらず装填済みなら
        else if (settings.isNeedCocking && !isLoadedChamber(gun) && isAmmoLoaded(gun)) {
            startCocking(world, shooter, gun)
        }
    }

    fun stopReload(gun: NbtCompound) {
        stopCocking(gun)
        GunNBTUtil.setReloadTime(gun, 0)
    }

    fun reloading(world: World, shooter: LivingEntity, gun: NbtCompound) {
        //リロード処理
        var reload = GunNBTUtil.getReloadTime(gun)
        playReloadSound(world, shooter, gun, reload)
        GunNBTUtil.setReloadTime(gun, --reload)
        if (reload <= 0) {
            reload(world, shooter, gun)
        }
    }

    fun playReloadSound(world: World, shooter: LivingEntity, gun: NbtCompound, remaining: Int) {
        val akimbo = !shooter.offHandStack.isEmpty && !shooter.mainHandStack.isEmpty
        if (akimbo && remaining % 2 == 0) {
            return
        }
        val time: Int
        time = if (akimbo) {
            ((settings.akimboReloadDuration - remaining).toFloat() / settings.akimboReloadDuration * settings.reloadDuration).toInt()
        } else {
            settings.reloadDuration - remaining
        }
        val soundPos = getSoundPos(shooter)
        settings.reloadSound.getSounds(time).forEach(Consumer { data: ISoundData ->
            data.play(
                world, soundPos.getX(), soundPos.getY(), soundPos.getZ()
            )
        })
    }

    //目の位置で音鳴らすとパンが偏ったりするしなによりうるさい
    fun getSoundPos(shooter: LivingEntity): Vec3d {
        val look = getVec(shooter.headYaw, shooter.pitch)
        return shooter.getCameraPosVec(1f).add(look.multiply(shooter.width.toDouble()))
    }

    fun reload(world: World, shooter: LivingEntity, gun: NbtCompound) {
        //プレイヤーではないかクリエイティブなら無消費リロード
        //if (!(shooter instanceof PlayerEntity) || ((PlayerEntity) shooter).isCreative()) {
        //マガジン式ならそのままフル
        if (settings.isShouldReloadMagazine) {
            GunNBTUtil.setLoadedBullets(gun, settings.loadableBulletsAmount)
        } else { //そうでないなら一発増やす
            var ammo = GunNBTUtil.getLoadedBullets(gun)
            GunNBTUtil.setLoadedBullets(gun, ++ammo)
            //フルでないならリロード継続
            if (ammo < settings.loadableBulletsAmount) {
                startReload(world, shooter, gun)
            } //フルかつコッキングされていないなら
            else if (settings.isNeedCocking && !isLoadedChamber(gun) && isAmmoLoaded(gun)) {
                startCocking(world, shooter, gun)
            }
        }
        /*} else {
            //サバイバルプレイヤーの場合
            PlayerEntity util = (PlayerEntity) shooter;
            for (int i = 0; i < util.inventory.size(); ++i) {
                ItemStack stack = util.inventory.getStack(i);
                //弾薬箱でないなら次のスロットへ
                if (stack.isEmpty() || !(stack.getItem() instanceof IAmmoBox)) {
                    continue;
                }
                IAmmoBox ammo = (IAmmoBox) stack.getItem();
                if (settings.isShouldReloadMagazine()) {
                    //マガジン式の場合
                    //弾があれば消費してフルまでリロード
                    if (ammo.consumeAmmo(stack, settings.getBulletCost() *
                            (settings.getLoadableBulletsAmount() - GunNBTUtil.getLoadedBullets(gun)))) {
                        GunNBTUtil.setLoadedBullets(gun, settings.getLoadableBulletsAmount());
                        break;
                    } else {//弾が足りなければあるだけリロード
                        float ammoAmount = ammo.getAmmoAmount(stack);
                        int reloadAmount = MathHelper.floor(ammoAmount / settings.getBulletCost());
                        if (0 < reloadAmount && ammo.consumeAmmo(stack, reloadAmount * settings.getBulletCost())) {
                            GunNBTUtil.setLoadedBullets(gun, GunNBTUtil.getLoadedBullets(gun) + reloadAmount);
                        }
                    }
                    //次の弾薬箱を探す
                } else {//単発リロードの場合
                    //弾があれば消費して一発リロード
                    if (ammo.consumeAmmo(stack, settings.getBulletCost())) {
                        int gunAmmo = GunNBTUtil.getLoadedBullets(gun);
                        GunNBTUtil.setLoadedBullets(gun, ++gunAmmo);
                        //フルでないならリロード継続
                        if (gunAmmo < settings.getLoadableBulletsAmount() && hasAmmo(shooter)) {
                            startReload(world, shooter, gun);
                        }
                        break;
                    }
                    //次の弾薬箱を探す
                }
            }
        }*/
        //コッキング
        if (settings.isNeedCocking && !isLoadedChamber(gun) && isAmmoLoaded(gun) && !isReloading(gun)) {
            startCocking(world, shooter, gun)
        }
    }

    fun hasAmmo(shooter: LivingEntity): Boolean {
        return true
        //非プレイヤーまたはクリエなら無条件true
        /*if (!(shooter instanceof PlayerEntity) || ((PlayerEntity) shooter).isCreative()) {
            return true;
        }
        //サバイバルプレイヤーの場合
        PlayerEntity util = (PlayerEntity) shooter;
        for (int i = 0; i < util.inventory.size(); ++i) {
            ItemStack stack = util.inventory.getStack(i);
            //弾薬箱でないなら次のスロットへ
            if (stack.isEmpty() || !(stack.getItem() instanceof IAmmoBox)) {
                continue;
            }
            IAmmoBox ammo = (IAmmoBox) stack.getItem();
            //1発でもリロードできればtrue
            if (settings.getBulletCost() < ammo.getAmmoAmount(stack)) {
                return true;
            }
        }
        return false;*/
    }

    fun isCocking(gun: NbtCompound): Boolean {
        return 0 < GunNBTUtil.getChamberTime(gun)
    }

    fun startCocking(world: World, shooter: LivingEntity, gun: NbtCompound) {
        val time: Int = if (isCloseChamber(gun)) {
            settings.openDuration
        } else {
            settings.closeDuration
        }
        //空なら即座にコッキング
        if (time <= 0) {
            GunNBTUtil.setChamberTime(gun, 1)
            cocking(world, shooter, gun)
        } else {
            GunNBTUtil.setChamberTime(gun, time)
        }
    }

    fun stopCocking(gun: NbtCompound) {
        GunNBTUtil.setChamberTime(gun, 0)
    }

    fun setCloseChamber(gun: NbtCompound, close: Boolean) {
        GunNBTUtil.setChamberState(gun, 0, close)
    }

    fun isCloseChamber(gun: NbtCompound): Boolean {
        return GunNBTUtil.getChamberState(gun, 0)
    }

    fun setLoadedChamber(gun: NbtCompound, loaded: Boolean) {
        GunNBTUtil.setChamberState(gun, 1, loaded)
    }

    fun isLoadedChamber(gun: NbtCompound): Boolean {
        return GunNBTUtil.getChamberState(gun, 1)
    }

    fun openChamber(gun: NbtCompound) {
        setLoadedChamber(gun, false)
        setCloseChamber(gun, false)
    }

    fun closeChamber(gun: NbtCompound) {
        setLoadedChamber(gun, true)
        setCloseChamber(gun, true)
    }

    fun cocking(world: World, shooter: LivingEntity, gun: NbtCompound) {
        var time = GunNBTUtil.getChamberTime(gun)
        GunNBTUtil.setChamberTime(gun, --time)
        val soundData: List<ISoundData> = if (isCloseChamber(gun)) {
            settings.cockingOpenSound.getSounds(settings.openDuration - time)
        } else {
            settings.cockingCloseSound.getSounds(settings.closeDuration - time)
        }
        val soundPos = getSoundPos(shooter)
        soundData.forEach(Consumer { data: ISoundData ->
            data.play(
                world,
                soundPos.getX(),
                soundPos.getY(),
                soundPos.getZ()
            )
        })
        if (time <= 0) {
            val isClose = !isCloseChamber(gun)
            //開く->閉じるなら装填、逆なら抜く
            if (isClose) {
                closeChamber(gun)
            } else {
                openChamber(gun)
            }
            //今現在開いており、リロード中ではなく、弾があるなら再びコッキング
            if (!isClose && !isReloading(gun) && isAmmoLoaded(gun)) startCocking(world, shooter, gun)
        }
    }

    fun setShooting(gun: NbtCompound, shooting: Boolean) {
        GunNBTUtil.setShooting(gun, shooting)
    }

    fun isShooting(gun: NbtCompound): Boolean {
        return GunNBTUtil.isShooting(gun)
    }

    fun canShooting(gun: NbtCompound): Boolean {
        return !isShootDelay(gun)
    }

    fun shooting(world: World, shooter: LivingEntity, stack: ItemStack, gun: NbtCompound) {
        val akimbo = false
        //銃を両手持ちしている場合、銃の発射タイミングをズラす
        /*if (!shooter.getMainHandStack().isEmpty() && !shooter.getOffHandStack().isEmpty()) {
            akimbo = true;
            ItemStack mainStack = shooter.getMainHandStack();
            Item mainItem = mainStack.getItem();
            ItemStack offStack = shooter.getOffHandStack();
            Item offItem = offStack.getItem();
            if (mainItem instanceof GunItem && offItem instanceof GunItem) {
                float delay = Math.min(((GunItem) mainItem).getSettings().getRate(), ((GunItem) offItem).getSettings().getRate());
                delay = Math.max(1200 / delay / 2, 2);
                _root_ide_package_.net.minecraft.nbt.NbtCompound offTag = offStack.getOrCreateTag();
                if (mainStack == stack && ((GunItem) offItem).canShooting(offTag)) {
                    GunNBTUtil.setShootDelay(offTag, GunNBTUtil.getShootDelay(offTag) + delay);
                }
                _root_ide_package_.net.minecraft.nbt.NbtCompound mainTag = mainStack.getOrCreateTag();
                if (offStack == stack && ((GunItem) mainItem).canShooting(mainTag)) {
                    GunNBTUtil.setShootDelay(mainTag, GunNBTUtil.getShootDelay(mainTag) + delay);
                }
            }
        }*/
        //レートによっては2発とか3発撃つ
        while (canShooting(gun)) {
            shoot(world, shooter, gun, akimbo)
            //射撃毎にコッキングが必要な場合は非装填状態にしてコッキング
            if (settings.isNeedCocking && settings.isCockingPerShot) {
                setLoadedChamber(gun, false)
                startCocking(world, shooter, gun)
            }
            var loaded = GunNBTUtil.getLoadedBullets(gun)
            GunNBTUtil.setLoadedBullets(gun, --loaded)
            //弾が切れたらリロード
            if (loaded <= 0) {
                if (settings.isNeedCocking && !settings.isCockingPerShot) {
                    openChamber(gun)
                }
                startReload(world, shooter, gun)
                break
            } else { //弾があるならー
                if (0 < settings.rate) GunNBTUtil.setShootDelay(
                    gun,
                    GunNBTUtil.getShootDelay(gun) + 20f * 60f / settings.rate
                )
                if (0 < settings.burstAmount) GunNBTUtil.setBurstCount(gun, settings.burstAmount)
                if (0 < settings.burstInterval) GunNBTUtil.setBurstDelay(
                    gun,
                    GunNBTUtil.getBurstDelay(gun) + settings.burstInterval
                )
            }
        }
    }

    fun shoot(world: World, shooter: LivingEntity, nbt: NbtCompound, akimbo: Boolean) {
        /*Vec3d soundPos = getSoundPos(shooter);
        settings.getShootSound().getSounds(0).forEach(sound ->
                sound.play(world, soundPos.getX(), soundPos.getY(), soundPos.getZ()));
        shootEffect(world, shooter, nbt, soundPos);
        for (int i = 0; i < settings.getShotAmount(); i++) {
            GunBulletEntity bullet = createBullet(shooter);
            bullet.setDamage(settings.getDamage());
            bullet.setHeadShotBonus(settings.getHeadShotBonus());
            bullet.setGravity(settings.isGravity());
            bullet.setDeleteTime((short) settings.getDecayDuration());
            bullet.setPierceLevel((byte) settings.getPiercingLevel());
            bullet.setBlockBreakLevel(settings.getBlockBreakLevel());
            float inaccuracy = akimbo  settings.getAkimboInaccuracy() : settings.getInaccuracy();
            if (isZoom(nbt)) {
                inaccuracy = akimbo  settings.getAkimboZoomInaccuracy() : settings.getZoomInaccuracy();
            }
            //一応、BowItemの時はrotationYawでやっているため、ifを挟む
            bullet.shoot(shooter, shooter.rotationPitch, shooter instanceof PlayerEntity
                            shooter.rotationYaw :
                            shooter.rotationYawHead, 0.0F,
                    settings.getVelocity(), inaccuracy);
            shooter.world.addEntity(bullet);
        }*/
    }

    /*public GunBulletEntity createBullet(LivingEntity shooter) {
        return new GunBulletEntity(shooter, shooter.world);
    }*/
    fun shootEffect(world: World, shooter: LivingEntity, nbt: NbtCompound, effectPos: Vec3d) {
        (world as ServerWorld).spawnParticles(
            ParticleTypes.SMOKE,
            effectPos.getX() + (world.random.nextFloat() * 2f - 1f) * 0.2f,
            effectPos.getY() + (world.random.nextFloat() * 2f - 1f) * 0.2f,
            effectPos.getZ() + (world.random.nextFloat() * 2f - 1f) * 0.2f,
            1, 0.0, 0.0, 0.0, 0.0
        )
    }

    fun canBurst(gun: NbtCompound): Boolean {
        return !isBurstDelay(gun) && 0 < GunNBTUtil.getBurstCount(gun)
    }

    fun burst(world: World, shooter: LivingEntity, gun: NbtCompound) {
        var akimbo = false
        //銃を両手持ちしている場合
        if (!shooter.mainHandStack.isEmpty && !shooter.offHandStack.isEmpty) {
            akimbo = true
        }
        //レートによっては2発とか3発撃つ
        while (canBurst(gun)) {
            shoot(world, shooter, gun, akimbo)
            //射撃毎にコッキングが必要な場合は非装填状態にしてコッキング
            if (settings.isNeedCocking && settings.isCockingPerShot) {
                setLoadedChamber(gun, false)
                startCocking(world, shooter, gun)
            }
            var loaded = GunNBTUtil.getLoadedBullets(gun)
            GunNBTUtil.setLoadedBullets(gun, --loaded)
            //弾が切れたらリロード
            if (loaded <= 0) {
                //弾切れならカウントリセット
                GunNBTUtil.setBurstCount(gun, 0)
                GunNBTUtil.setBurstDelay(gun, 0f)
                if (settings.isNeedCocking) {
                    openChamber(gun)
                }
                startReload(world, shooter, gun)
                break
            } else {
                //弾があるなら次弾装填
                GunNBTUtil.setBurstCount(gun, GunNBTUtil.getBurstCount(gun) - 1)
                if (0 < settings.burstInterval) GunNBTUtil.setBurstDelay(
                    gun,
                    GunNBTUtil.getBurstDelay(gun) + settings.burstInterval
                )
            }
        }
    }

    fun onLeftClick(stack: ItemStack, entity: LivingEntity): Boolean {
        val gun = stack.orCreateTag
        if (canZoom(gun)) {
            zoom(entity, gun, !isZoom(gun))
        }
        return false
    }

    fun canZoom(gun: NbtCompound): Boolean {
        return settings.zoomFov != 0f && !isReloading(gun)
    }

    fun setZoom(gun: NbtCompound, zoom: Boolean) {
        GunNBTUtil.setZoom(gun, zoom)
    }

    fun isZoom(gun: NbtCompound): Boolean {
        return GunNBTUtil.getZoom(gun)
    }

    fun zoom(shooter: LivingEntity, gun: NbtCompound, zoom: Boolean) {
        if (shooter.world.isClient) {
            return
        }
        /*if (!shooter.getMainHandStack().isEmpty() && !shooter.getOffHandStack().isEmpty()) {
            ItemStack mainStack = shooter.getMainHandStack();
            Item mainItem = mainStack.getItem();
            ItemStack offStack = shooter.getOffHandStack();
            Item offItem = offStack.getItem();
            if (mainItem instanceof GunItem && offItem instanceof GunItem) {
                ((GunItem) mainItem).setZoom(mainStack.getOrCreateTag(), zoom);
                ((GunItem) offItem).setZoom(offStack.getOrCreateTag(), zoom);
            }
        } else {
            setZoom(gun, zoom);
        }*/
        val soundPos = getSoundPos(shooter)
        shooter.world.playSound(
            null, soundPos.getX(), soundPos.getY(), soundPos.getZ(),
            SoundEvents.ENTITY_ENDER_DRAGON_FLAP, SoundCategory.PLAYERS, 0.5f, 2f
        )
    }

    companion object {
        val MOVE_SPEED_ID = UUID.fromString("d6fac9e2-64a8-4b6e-a093-c6a57099c5a4")
        val ZOOM_SPEED_ID = UUID.fromString("662ABCDE-DCBA-BCBC-8813-96EA6097278D")

        @Environment(EnvType.CLIENT)
        protected var press = false

        @Environment(EnvType.CLIENT)
        fun pressUseItemKey(nowPress: Boolean) {
            if (nowPress != press) {
                if (!nowPress) //Networking.INSTANCE.sendToServer(new PacketReleaseRightClick());
                    press = nowPress
            }
        }

        fun isHolding(entity: LivingEntity, stack: ItemStack): Boolean {
            return entity.mainHandStack == stack || entity.offHandStack == stack
        }

        fun getVec(yaw: Float, pitch: Float): Vec3d {
            val pitchRad = pitch * (Math.PI.toFloat() / 180f)
            val yawRad = -yaw * (Math.PI.toFloat() / 180f)
            val yawCos = MathHelper.cos(yawRad)
            val yawSin = MathHelper.sin(yawRad)
            val pitchCos = MathHelper.cos(pitchRad)
            val pitchSin = MathHelper.sin(pitchRad)
            return Vec3d((yawSin * pitchCos).toDouble(), (-pitchSin).toDouble(), (yawCos * pitchCos).toDouble())
        }
    }
}