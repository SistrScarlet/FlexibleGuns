package net.sistr.flexibleguns.resource

import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier
import net.sistr.flexibleguns.item.util.SoundDataHolder
import java.util.function.BiConsumer
import java.util.function.Function

data class GunSetting(
    val gunId: Identifier,
    val textureId: Identifier?,
    val fireInterval: Float,
    val shotsAmount: Int,
    val holdSpeed: Float,
    val inAccuracy: Float,
    val velocity: Float,
    val damage: Float,
    val headshotAmplifier: Float,
    val gravity: Float,
    val decay: Int,
    val inertia: Float,
    val reload: ReloadParam?,
    val zoom: ZoomParam?,
    val burst: BurstParam?,
    val shootSounds: SoundDataHolder
) {

    fun write(buf: PacketByteBuf) {
        buf.writeIdentifier(gunId)
        buf.writeIdentifier(textureId ?: Identifier(""))
        buf.writeFloat(fireInterval)
        buf.writeVarInt(shotsAmount)
        buf.writeFloat(holdSpeed)
        buf.writeFloat(inAccuracy)
        buf.writeFloat(velocity)
        buf.writeFloat(damage)
        buf.writeFloat(headshotAmplifier)
        buf.writeFloat(gravity)
        buf.writeVarInt(decay)
        buf.writeFloat(inertia)
        writeIfPresent(buf, reload) { buf, reload ->
            buf.writeVarInt(reload.maxAmmo)
            buf.writeVarInt(reload.reloadLength)
            buf.writeVarInt(reload.reloadAmount)
            buf.writeBoolean(reload.ejectAmmo)
            writeIfPresent(buf, reload.action) { buf, action ->
                buf.writeEnumConstant(action.type)
                buf.writeVarInt(action.openLength)
                buf.writeVarInt(action.closeLength)
            }
        }
        writeIfPresent(buf, zoom) { buf, zoom ->
            buf.writeFloat(zoom.zoomInAccuracy)
            buf.writeFloat(zoom.zoomSpeed)
            buf.writeFloat(zoom.zoomAmount)
        }
        writeIfPresent(buf, burst) { buf, burst ->
            buf.writeVarInt(burst.maxBurstCount)
            buf.writeFloat(burst.maxBurstDelay)
        }
        writeIfPresent(buf, shootSounds) { buf, shootSounds ->
            buf.writeNbt(shootSounds.write())
        }
    }

    private fun <T> writeIfPresent(buf: PacketByteBuf, nullable: T?, consumer: BiConsumer<PacketByteBuf, T>) {
        if (nullable != null) {
            buf.writeBoolean(true)
            consumer.accept(buf, nullable)
        } else {
            buf.writeBoolean(false)
        }
    }

    data class ReloadParam(
        val maxAmmo: Int, val reloadLength: Int, val reloadAmount: Int, val ejectAmmo: Boolean,
        val action: ActionParam?
    )

    data class ActionParam(val type: ActionType, val openLength: Int, val closeLength: Int)
    enum class ActionType(val id: String) {
        //??????
        //???????????????????????????
        //????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????

        //????????????????????????????????????????????????????????????
        SLIDE("slide"),

        //?????????????????????????????????????????????
        //?????????????????????????????????
        BOLT("bolt"),

        //?????????????????????????????????????????????
        PUMP("pump"),

        //???????????????????????????????????????????????????
        //?????????????????????????????????
        BREAK("break");

        companion object {
            fun actionFromId(id: String): ActionType {
                values().forEach { if (it.id == id.lowercase()) return it }
                throw RuntimeException("???????????????ActionType?????????")
            }
        }

    }

    data class ZoomParam(val zoomInAccuracy: Float, val zoomSpeed: Float, val zoomAmount: Float)
    data class BurstParam(val maxBurstCount: Int, val maxBurstDelay: Float)

    class Builder(val gunId: Identifier) {
        //???????????????
        var textureId: Identifier? = null

        //??????
        var fireInterval = 0.0f
        var shotsAmount = 1
        var holdSpeed = 1.0f

        //??????
        var inAccuracy = 0.0f
        var velocity = 0.0f
        var damage = 0.0f
        var headshotAmplifier = 1.0f
        var gravity = 0.0f
        var decay = 100
        var inertia = 1.0f

        //????????????
        var reload: ReloadParam? = null

        //?????????
        var zoom: ZoomParam? = null

        //????????????
        var burst: BurstParam? = null

        //???
        val shootSounds = SoundDataHolder.Builder()

        constructor(buf: PacketByteBuf) : this(buf.readIdentifier()) {
            textureId = buf.readIdentifier()
            fireInterval = buf.readFloat()
            shotsAmount = buf.readVarInt()
            holdSpeed = buf.readFloat()
            inAccuracy = buf.readFloat()
            velocity = buf.readFloat()
            damage = buf.readFloat()
            headshotAmplifier = buf.readFloat()
            gravity = buf.readFloat()
            decay = buf.readVarInt()
            inertia = buf.readFloat()
            reload = readIfPresent(buf) { buf ->
                ReloadParam(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readBoolean(),
                    readIfPresent(buf) { buf ->
                        ActionParam(buf.readEnumConstant(ActionType::class.java), buf.readVarInt(), buf.readVarInt())
                    }
                )
            }
            zoom = readIfPresent(buf) { buf ->
                ZoomParam(buf.readFloat(), buf.readFloat(), buf.readFloat())
            }
            burst = readIfPresent(buf) { buf ->
                BurstParam(buf.readVarInt(), buf.readFloat())
            }
            readIfPresent(buf) { buf ->
                val nbt = buf.readNbt()
                if (nbt != null) {
                    shootSounds.read(nbt)
                }
            }
        }

        private fun <T> readIfPresent(buf: PacketByteBuf, function: Function<PacketByteBuf, T>): T? {
            return if (buf.readBoolean()) {
                function.apply(buf)
            } else {
                null
            }
        }

        fun build(): GunSetting {
            return GunSetting(
                gunId,
                textureId,
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
                reload,
                zoom,
                burst,
                shootSounds.build()
            )
        }

    }

}