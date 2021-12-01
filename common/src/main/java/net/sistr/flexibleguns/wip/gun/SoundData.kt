package net.sistr.flexibleguns.wip.gun

import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.world.World

class SoundData(
    private val sound: SoundEvent,
    private val category: SoundCategory,
    private val volume: Float,
    private val pitch: Float
) : ISoundData {
    override fun play(world: World, x: Double, y: Double, z: Double) {
        world.playSound(null, x, y, z, sound, this.category, volume, pitch)
    }
}