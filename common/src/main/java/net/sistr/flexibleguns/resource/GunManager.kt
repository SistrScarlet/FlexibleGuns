package net.sistr.flexibleguns.resource

import com.google.common.collect.Maps
import com.google.gson.JsonElement
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier
import net.minecraft.util.math.MathHelper
import net.sistr.flexibleguns.item.util.SoundData
import net.sistr.flexibleguns.item.util.SoundDataHolder
import net.sistr.flexibleguns.resource.util.JsonUtil
import java.util.*
import java.util.concurrent.ThreadLocalRandom

class GunManager {
    private val gunSettings = HashMap<Identifier, GunSetting>()

    companion object {
        val INSTANCE = GunManager()
    }

    fun getGunSettings(): Map<Identifier, GunSetting> {
        return Maps.newHashMap(gunSettings)
    }

    fun getGunSetting(id: Identifier): GunSetting? {
        return gunSettings[id]
    }

    fun getRandomGunSetting(): Optional<GunSetting> {
        val rand = ThreadLocalRandom.current()
        var min = Int.MAX_VALUE
        var result: GunSetting? = null
        for (setting in gunSettings.values) {
            val num = rand.nextInt()
            if (num < min) {
                result = setting
                min = num
            }
        }
        return Optional.ofNullable(result)
    }

    fun addGunSetting(id: Identifier, setting: GunSetting) {
        if (gunSettings.containsKey(id)) {
            throw RuntimeException("登録済みのIDです。 : $id")
        }
        gunSettings[id] = setting
    }

    fun clear() {
        gunSettings.clear()
    }

    //todo 一か所でも例外吐くとコワイ
    //todo Optional使いたい…
    fun read(id: Identifier, jsonElement: JsonElement): GunSetting.Builder? {
        if (!jsonElement.isJsonObject) {
            return null
        }
        val builder = GunSetting.Builder(id)
        val jsonObject = jsonElement.asJsonObject
        JsonUtil.readString(jsonObject["textureId"]).ifPresent { builder.textureId = Identifier(it) }
        JsonUtil.readFloat(jsonObject["fireInterval"]).ifPresent { builder.fireInterval = it }
        JsonUtil.readInt(jsonObject["shotsAmount"]).ifPresent { builder.shotsAmount = it }
        JsonUtil.readFloat(jsonObject["holdSpeed"]).ifPresent { builder.holdSpeed = it }

        JsonUtil.readFloat(jsonObject["inAccuracy"]).ifPresent { builder.inAccuracy = it }
        JsonUtil.readFloat(jsonObject["velocity"]).ifPresent { builder.velocity = it }
        JsonUtil.readFloat(jsonObject["damage"]).ifPresent { builder.damage = it }
        JsonUtil.readFloat(jsonObject["headshotAmplifier"]).ifPresent { builder.headshotAmplifier = it }
        JsonUtil.readFloat(jsonObject["gravity"]).ifPresent { builder.gravity = it }
        JsonUtil.readInt(jsonObject["range"]).ifPresent { builder.decay = MathHelper.ceil(it / builder.velocity) }
        JsonUtil.readFloat(jsonObject["inertia"]).ifPresent { builder.inertia = it }

        readReloadParam(jsonObject["reload"]).ifPresent { builder.reload = it }

        readZoomParam(jsonObject["zoom"]).ifPresent { builder.zoom = it }

        readBurstParam(jsonObject["burst"]).ifPresent { builder.burst = it }

        readSounds(jsonObject["shootSounds"], builder.shootSounds)

        //fireIntervalのエイリアス
        JsonUtil.readFloat(jsonObject["rateOfFire"]).ifPresent { builder.fireInterval = 1200f / it }

        //damageのエイリアス
        //TTK = 1200 / DPM
        //TTK = 20 / DPS
        //DPM = 1200 / TTK
        //DPS = 20 / TTK
        //Interval = 1200 / RPM
        //RPM = 1200 / Interval
        JsonUtil.readFloat(jsonObject["dps"]).ifPresent {
            //Dmg = DPS / RPS
            builder.damage = it / ((20 / builder.fireInterval) * (builder.burst?.maxBurstCount ?: 1))
        }
        JsonUtil.readFloat(jsonObject["dpm"]).ifPresent {
            //Dmg = DPM / RPM
            builder.damage = it / ((1200 / builder.fireInterval) * (builder.burst?.maxBurstCount ?: 1))
        }
        JsonUtil.readFloat(jsonObject["timeToKill"]).ifPresent {
            //Dmg = 20(Health) / (20(tick) * interval(s)) / TTK(20health/s)
            builder.damage = (builder.fireInterval / (builder.burst?.maxBurstCount ?: 1)) / it
        }

        return builder
    }

    private fun readReloadParam(jsonElement: JsonElement?): Optional<GunSetting.ReloadParam> {
        if (jsonElement == null || !jsonElement.isJsonObject) {
            return Optional.empty()
        }
        val jsonObject = jsonElement.asJsonObject
        val maxAmmo = jsonObject["maxAmmo"]
        val reloadLength = jsonObject["reloadLength"]
        val reloadAmount = jsonObject["reloadAmount"]
        val ejectAmmo = jsonObject["ejectAmmo"]
        return if (maxAmmo.isJsonPrimitive && maxAmmo.asJsonPrimitive.isNumber
            && reloadLength.isJsonPrimitive && reloadLength.asJsonPrimitive.isNumber
            && reloadAmount.isJsonPrimitive && reloadAmount.asJsonPrimitive.isNumber
            && ejectAmmo.isJsonPrimitive && ejectAmmo.asJsonPrimitive.isBoolean
        ) {
            val actionParam = readActionParam(jsonObject["action"]).orElse(null)
            Optional.of(
                GunSetting.ReloadParam(
                    maxAmmo.asInt, reloadLength.asInt,
                    if (reloadAmount.asInt <= 0) maxAmmo.asInt else reloadAmount.asInt,
                    ejectAmmo.asBoolean,
                    actionParam
                )
            )
        } else {
            Optional.empty()
        }
    }

    private fun readActionParam(jsonElement: JsonElement?): Optional<GunSetting.ActionParam> {
        if (jsonElement == null || !jsonElement.isJsonObject) {
            return Optional.empty()
        }
        val jsonObject = jsonElement.asJsonObject
        val type = jsonObject["type"]
        val openLength = jsonObject["openLength"]
        val closeLength = jsonObject["closeLength"]
        return if (type.isJsonPrimitive && type.asJsonPrimitive.isString
            && openLength.isJsonPrimitive && openLength.asJsonPrimitive.isNumber
            && closeLength.isJsonPrimitive && closeLength.asJsonPrimitive.isNumber
        ) {
            //ここエラー吐く場合アリ、注意
            val actionType = GunSetting.ActionType.actionFromId(type.asString)
            Optional.of(
                GunSetting.ActionParam(actionType, openLength.asInt, closeLength.asInt)
            )
        } else {
            Optional.empty()
        }
    }

    private fun readZoomParam(jsonElement: JsonElement?): Optional<GunSetting.ZoomParam> {
        if (jsonElement == null || !jsonElement.isJsonObject) {
            return Optional.empty()
        }
        val jsonObject = jsonElement.asJsonObject
        val zoomInAccuracy = jsonObject["zoomInAccuracy"]
        val zoomSpeed = jsonObject["zoomSpeed"]
        val zoomAmount = jsonObject["zoomAmount"]
        return if (zoomInAccuracy.isJsonPrimitive && zoomInAccuracy.asJsonPrimitive.isNumber
            && zoomSpeed.isJsonPrimitive && zoomSpeed.asJsonPrimitive.isNumber
            && zoomAmount.isJsonPrimitive && zoomAmount.asJsonPrimitive.isNumber
        ) {
            Optional.of(GunSetting.ZoomParam(zoomInAccuracy.asFloat, zoomSpeed.asFloat, zoomAmount.asFloat))
        } else {
            Optional.empty()
        }
    }

    private fun readBurstParam(jsonElement: JsonElement?): Optional<GunSetting.BurstParam> {
        if (jsonElement == null || !jsonElement.isJsonObject) {
            return Optional.empty()
        }
        val jsonObject = jsonElement.asJsonObject
        val maxBurstCount = jsonObject["maxBurstCount"]
        val burstRate = jsonObject["burstRate"]
        return if (maxBurstCount.isJsonPrimitive && maxBurstCount.asJsonPrimitive.isNumber
            && burstRate.isJsonPrimitive && burstRate.asJsonPrimitive.isNumber
        ) {
            Optional.of(GunSetting.BurstParam(maxBurstCount.asInt, 1200f / burstRate.asFloat))
        } else {
            Optional.empty()
        }
    }

    private fun readSounds(jsonElement: JsonElement?, soundBuilder: SoundDataHolder.Builder) {
        if (jsonElement == null || !jsonElement.isJsonObject) {
            return
        }
        val jsonObject = jsonElement.asJsonObject
        jsonObject.entrySet().forEach {
            if (it.value.isJsonObject) {
                val soundJsonObject = it.value.asJsonObject
                val id = JsonUtil.readString(soundJsonObject["id"])
                val pitch = JsonUtil.readFloat(soundJsonObject["pitch"])
                val volume = JsonUtil.readFloat(soundJsonObject["volume"])
                val time = JsonUtil.readInt(soundJsonObject["time"])
                if (id.isPresent && pitch.isPresent && volume.isPresent && time.isPresent) {
                    soundBuilder.addSound(time.get(), SoundData(Identifier(id.get()), pitch.get(), volume.get()))
                }
            }

        }
    }

    fun write(buf: PacketByteBuf) {
        gunSettings.forEach { (id, setting) ->
            buf.writeBoolean(true)
            buf.writeIdentifier(id)
            setting.write(buf)
        }
        buf.writeBoolean(false)
    }

    fun read(buf: PacketByteBuf) {
        while (buf.readBoolean()) {
            val id = buf.readIdentifier()
            val builder = GunSetting.Builder(buf)
            addGunSetting(id, builder.build())
        }
    }

}