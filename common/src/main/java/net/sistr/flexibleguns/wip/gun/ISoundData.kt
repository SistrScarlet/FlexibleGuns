package net.sistr.flexibleguns.wip.gun

import net.minecraft.world.World

interface ISoundData {
    fun play(world: World, x: Double, y: Double, z: Double)
}