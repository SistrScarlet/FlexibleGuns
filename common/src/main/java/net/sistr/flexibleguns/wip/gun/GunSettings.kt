package net.sistr.flexibleguns.wip.gun

import com.google.common.collect.Lists
import com.google.common.collect.Maps
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent

//銃の性能を決定する変数を保持するクラス
//直接コンストラクタ触らずBuilder使ってね
//todo 脆いブロックを壊せる設定
data class GunSettings private constructor(//弾丸の設定
    val damage: Float,  //ダメージ
    val headShotBonus: Float, //ヘッドショット時に加算される量
    val decayDuration: Int, //消えるまでの時間、ゼロだと1tick目に消滅かつダメージ判定なし
    val isGravity: Boolean,//重力に従うか
    val piercingLevel: Int, //貫通力、ゼロだと不使用
    val blockBreakLevel: Int,//ブロック破壊力
    //射撃の設定
    val rate: Float,  //分間発射速度 ゼロなら1tick連射
    val velocity: Float, //初速
    val inaccuracy: Float, //射撃の不正確度 ゼロなら完璧な精度
    val shotAmount: Int,//同時発射数 ゼロなら何も出ない
    //バースト射撃の設定
    val burstAmount: Int, //バースト射撃する弾数 3で三連射 ゼロなら不使用
    val burstInterval: Float,//バースト射撃の間隔 rateと別なので気を付けること ゼロなら1tick連射
    //リロードの設定
    val loadableBulletsAmount: Int, //装弾数 ゼロならリロード不要
    val reloadDuration: Int, //リロードに掛かる時間 ゼロなら瞬間リロード
    val isShouldReloadMagazine: Boolean, //リロードをマガジンで行うか
    val bulletCost: Float,//弾丸一発当たりの弾コスト消費量
    //アクションの設定
    val isNeedCocking: Boolean,//コッキングが必要か
    val isCockingPerShot: Boolean, //射撃ごとにコッキングが必要か
    val isCockingPerReload: Boolean, //リロードごとにコッキングが必要か
    val openDuration: Int, //開放までの長さ
    val closeDuration: Int,//閉鎖までの長さ
    //ズームの設定
    val zoomFov: Float, //ズーム時にFovに掛けられる倍率 ゼロなら不使用
    val zoomInaccuracy: Float, //ズーム時不精度 ゼロなら完璧な精度
    val zoomMove: Float,//ズーム時に移動速度に加算される量
    //アキンボ(二丁持ち)の設定
    val akimboInaccuracy: Float, //アキンボ時の射撃の不正確度 ゼロなら完璧な精度
    val akimboZoomInaccuracy: Float, //アキンボ時のズーム時不精度 ゼロなら完璧な精度
    val akimboReloadDuration: Int,//アキンボ時にリロードに掛かる時間 ゼロなら瞬間リロード
    //その他の設定
    val move: Float,//所持時に移動速度に加算される量
    //音の設定
    val shootSound: SequentialSoundData,//射撃音
    val reloadSound: SequentialSoundData,//リロード音
    val cockingOpenSound: SequentialSoundData,//コッキング(開く)音
    val cockingCloseSound: SequentialSoundData//コッキング(閉じる)音
) {

    //上記のビルダー
    //必要な物だけ設定すれば良い
    class Builder {
        //弾丸の設定
        private var damage = 0f //ダメージ
        private var headShotBonus = 0f //ヘッドショット時に加算される量
        private var range = 64 //消えるまでの距離
        private var isGravity = true //重力に従うか
        private var piercingLevel = 0 //貫通力、ゼロだと不使用
        private var blockBreakLevel = 0 //ブロック破壊力

        //射撃の設定
        private var rate = 0f //分間発射速度 ゼロなら1tick連射
        private var velocity = 0f //初速
        private var inaccuracy = 0f //射撃の不正確度 ゼロなら完璧な精度
        private var shotAmount = 1 //同時発射数 ゼロなら何も出ない

        //バースト射撃の設定
        private var burstAmount = 0 //バースト射撃する弾数 3で三連射 ゼロなら不使用
        private var burstDelay = 0f //バースト射撃の間隔 rateと別なので気を付けること ゼロなら一瞬でバースト

        //リロードの設定
        private var loadableBulletsAmount = 0 //装弾数 ゼロならリロード不要
        private var reloadDuration = 0 //リロードに掛かる時間 ゼロなら瞬間リロード
        private var shouldReloadMagazine = true //リロードをマガジンで行うか
        private var bulletCost = 0f //弾丸一発当たりの弾コスト消費量

        //アクションの設定
        private var needCocking = false //コッキングが必要か
        private var cockingPerShot = false //射撃ごとにコッキングが必要か
        private var cockingPerReload = false //リロードごとにコッキングが必要か
        private var openDuration = 0
        private var closeDuration = 0

        //ズームの設定
        private var zoomFov = 0f //ズーム時にFovに掛けられる倍率 ゼロなら不使用
        private var zoomInaccuracy = 0f //ズーム時不精度 ゼロなら完璧な精度
        private var zoomMove = 0f //ズーム時に移動速度に加算される量

        //アキンボ(二丁持ち)の設定
        private var akimboInaccuracy = -1f //アキンボ時の射撃の不正確度 ゼロなら完璧な精度
        private var akimboZoomInaccuracy = -1f //アキンボ時のズーム時不精度 ゼロなら完璧な精度
        private var akimboReloadDuration = -1 //アキンボ時にリロードに掛かる時間 ゼロなら瞬間リロード

        //その他の設定
        private var move = 0f //所持時に移動速度に加算される量

        //音の設定
        private val shootSound: MutableMap<Int, MutableList<ISoundData>?> = Maps.newHashMap() //射撃音
        private val reloadSound: MutableMap<Int, MutableList<ISoundData>?> = Maps.newHashMap() //リロード音
        private val cockingOpenSound: MutableMap<Int, MutableList<ISoundData>?> = Maps.newHashMap() //コッキング(開く)音
        private val cockingCloseSound: MutableMap<Int, MutableList<ISoundData>?> = Maps.newHashMap() //コッキング(開く)音

        fun damage(body: Float, head: Float): Builder {
            damage = body
            headShotBonus = head - body
            return this
        }

        fun range(distance: Int): Builder {
            range = distance
            return this
        }

        fun gravity(gravity: Boolean): Builder {
            isGravity = gravity
            return this
        }

        fun piercing(piercing: Int): Builder {
            piercingLevel = piercing
            return this
        }

        fun blockBreak(level: Int): Builder {
            blockBreakLevel = level
            return this
        }

        fun rate(rate: Float): Builder {
            this.rate = rate
            return this
        }

        fun velocity(velocity: Float): Builder {
            this.velocity = velocity
            return this
        }

        fun inaccuracy(inaccuracy: Float): Builder {
            this.inaccuracy = inaccuracy
            return this
        }

        fun shotAmount(shotAmount: Int): Builder {
            this.shotAmount = shotAmount
            return this
        }

        fun burst(burstAmount: Int, burstDelay: Float): Builder {
            this.burstAmount = burstAmount
            this.burstDelay = burstDelay
            return this
        }

        fun reload(loadableBulletsAmount: Int, reloadDuration: Int): Builder {
            this.loadableBulletsAmount = loadableBulletsAmount
            this.reloadDuration = reloadDuration
            return this
        }

        fun shouldReloadMagazine(shouldReloadMagazine: Boolean): Builder {
            this.shouldReloadMagazine = shouldReloadMagazine
            return this
        }

        fun cost(bulletCost: Float): Builder {
            this.bulletCost = bulletCost
            return this
        }

        fun cocking(
            needCocking: Boolean, cockingPerShot: Boolean, cockingPerReload: Boolean,
            openDuration: Int, closeDuration: Int
        ): Builder {
            this.needCocking = needCocking
            this.cockingPerShot = cockingPerShot
            this.cockingPerReload = cockingPerReload
            this.openDuration = openDuration
            this.closeDuration = closeDuration
            return this
        }

        fun zoom(zoomFov: Float, zoomInaccuracy: Float, zoomMove: Float): Builder {
            this.zoomFov = zoomFov
            this.zoomInaccuracy = zoomInaccuracy
            this.zoomMove = zoomMove
            return this
        }

        fun akimbo(akimboInaccuracy: Float, akimboZoomInaccuracy: Float, akimboReloadDuration: Int): Builder {
            this.akimboInaccuracy = akimboInaccuracy
            this.akimboZoomInaccuracy = akimboZoomInaccuracy
            this.akimboReloadDuration = akimboReloadDuration
            return this
        }

        fun move(move: Float): Builder {
            this.move = move
            return this
        }

        fun addShootSound(
            time: Int,
            sound: SoundEvent?,
            category: SoundCategory?,
            volume: Float,
            pitch: Float
        ): Builder {
            addSound(shootSound, time, SoundData(sound!!, category!!, volume, pitch))
            return this
        }

        fun addShootSound(time: Int, sound: ISoundData): Builder {
            addSound(shootSound, time, sound)
            return this
        }

        fun addReloadSound(time: Int, sound: ISoundData): Builder {
            addSound(reloadSound, time, sound)
            return this
        }

        fun addCockingOpenSound(time: Int, sound: ISoundData): Builder {
            addSound(cockingOpenSound, time, sound)
            return this
        }

        fun addCockingCloseSound(time: Int, sound: ISoundData): Builder {
            addSound(cockingCloseSound, time, sound)
            return this
        }

        private fun addSound(sounds: MutableMap<Int, MutableList<ISoundData>?>, time: Int, sound: ISoundData) {
            var data = sounds[time]
            if (data == null) data = Lists.newArrayList()
            data!!.add(sound)
            sounds[time] = data
        }

        fun build(): GunSettings {
            if (0 < burstAmount) {
                rate /= burstAmount.toFloat()
                burstAmount--
            }
            if (akimboInaccuracy == -1f) {
                akimboInaccuracy = inaccuracy * 2f
            }
            if (akimboZoomInaccuracy == -1f) {
                akimboZoomInaccuracy = zoomInaccuracy * 2f
            }
            if (akimboReloadDuration == -1) {
                akimboReloadDuration = reloadDuration * 2
            }
            return GunSettings(
                damage, headShotBonus, (range / velocity).toInt(), isGravity,
                piercingLevel, blockBreakLevel, rate, velocity, inaccuracy, shotAmount,
                burstAmount, burstDelay,
                loadableBulletsAmount, reloadDuration, shouldReloadMagazine, bulletCost,
                needCocking, cockingPerShot, cockingPerReload, openDuration, closeDuration,
                zoomFov, zoomInaccuracy, zoomMove,
                akimboInaccuracy, akimboZoomInaccuracy, akimboReloadDuration,
                move,
                SequentialSoundData(shootSound), SequentialSoundData(reloadSound),
                SequentialSoundData(cockingOpenSound), SequentialSoundData(cockingCloseSound)
            )
        }
    }

}