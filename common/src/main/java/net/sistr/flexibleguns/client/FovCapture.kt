package net.sistr.flexibleguns.client

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment

@Environment(EnvType.CLIENT)
object FovCapture {
    var fov: Double = 70.0
}