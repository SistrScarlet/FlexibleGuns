package net.sistr.flexibleguns.client

import dev.architectury.registry.client.keymappings.KeyMappingRegistry
import net.minecraft.client.option.KeyBinding
import net.sistr.flexibleguns.FlexibleGunsMod
import org.lwjgl.glfw.GLFW

object FGKeys {
    /*val FIRE = KeyBinding(
        FlexibleGunsLib.MODID + ".key.fire",
        InputUtil.Type.MOUSE, 1, "key.categories." + FlexibleGunsLib.MODID
    )
    val ZOOM_TOGGLE = KeyBinding(
        FlexibleGunsLib.MODID + ".key.zoom_toggle",
        InputUtil.Type.MOUSE, 0, "key.categories." + FlexibleGunsLib.MODID
    )
    val ZOOM_HOLD = KeyBinding(
        FlexibleGunsLib.MODID + ".key.zoom_hold",
        InputUtil.UNKNOWN_KEY.code, "key.categories." + FlexibleGunsLib.MODID
    )*/
    val RELOAD = KeyBinding(
        FlexibleGunsMod.MODID + ".key.reload",
        GLFW.GLFW_KEY_R, "key.categories." + FlexibleGunsMod.MODID
    )

    fun init() {
        //register(FIRE)
        //register(ZOOM_TOGGLE)
        //register(ZOOM_HOLD)
        register(RELOAD)
    }

    fun register(keyBinding: KeyBinding) {
        KeyMappingRegistry.register(keyBinding)
    }

}