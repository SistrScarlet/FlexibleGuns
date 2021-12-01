package net.sistr.flexibleguns.util

import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d

data class PrevEntity(val pos: Vec3d, val velocity: Vec3d, val box: Box, val eyeHeight: Float)