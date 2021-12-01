package net.sistr.flexibleguns.client

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.util.Identifier

@Environment(EnvType.CLIENT)
class SoundCapManager {
    private val map = HashMap<Identifier, SoundCap>()

    companion object {
        val INSTANCE = SoundCapManager()
    }

    fun addSoundCap(id: Identifier) {
        val cap = map[id] ?: return
        cap.addCount()
    }

    fun isSoundCapped(id: Identifier): Boolean {
        val cap = map[id] ?: return false
        return cap.isCapped()
    }

    fun clearSoundCap() {
        map.values.forEach { it.resetCap() }
    }

    fun register(id: Identifier, cap: Int) {
        map[id] = SoundCap(cap)
    }

    class SoundCap(val cap: Int) {
        var count = 0

        fun addCount() {
            count++
        }

        fun isCapped(): Boolean {
            return cap <= count
        }

        fun resetCap() {
            count = 0
        }
    }

}