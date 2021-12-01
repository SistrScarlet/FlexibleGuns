package net.sistr.flexibleguns.client

import it.unimi.dsi.fastutil.objects.AbstractObject2BooleanMap
import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import net.sistr.flexibleguns.network.InputPacket
import net.sistr.flexibleguns.util.Input
import net.sistr.flexibleguns.util.Inputable

@Environment(EnvType.CLIENT)
object Inputs {
    private val toggleInputMap = Object2BooleanArrayMap<Input>()
    private val holdInputMap = Object2BooleanArrayMap<Input>()
    private val sendHoldInputMap = Object2BooleanArrayMap<Input>()

    fun tick() {
        checkToggle(Input.FIRE, MinecraftClient.getInstance().options.keyUse)
        checkToggle(Input.ZOOM, MinecraftClient.getInstance().options.keyAttack)
        //checkToggle(Input.FIRE, FGLKeys.FIRE)
        //checkToggle(Input.ZOOM, FGLKeys.ZOOM_TOGGLE)
        //checkHold(Input.ZOOM, FGLKeys.ZOOM_HOLD)
        checkToggle(Input.RELOAD, FGKeys.RELOAD)
    }

    private fun checkToggle(input: Input, key: KeyBinding) {
        val isPressed =
            if (
                MinecraftClient.getInstance().currentScreen != null
                || (MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().player!!.isSpectator)
            ) {
                false
            } else {
                key.isPressed
            }
        if (isPressed != getInput(input, toggleInputMap)) {
            toggleInputMap[input] = isPressed
            val player = MinecraftClient.getInstance().player
            if (player != null) {
                (player as Inputable).inputKeyFG(input, isPressed)
            }
            InputPacket.sendC2S(input, isPressed)
        }
    }

    private fun checkHold(input: Input, key: KeyBinding) {
        //現在と過去が違った時にtrueにして、次のtickでfalseにする
        val isPressed = if (MinecraftClient.getInstance().currentScreen == null) key.isPressed else false
        if (getInput(input, sendHoldInputMap)) {
            sendHoldInputMap[input] = false
            val player = MinecraftClient.getInstance().player
            if (player != null) {
                (player as Inputable).inputKeyFG(input, false)
            }
            InputPacket.sendC2S(input, false)
        }
        if (isPressed != getInput(input, holdInputMap)) {
            holdInputMap[input] = isPressed
            sendHoldInputMap[input] = true
            val player = MinecraftClient.getInstance().player
            if (player != null) {
                (player as Inputable).inputKeyFG(input, true)
            }
            InputPacket.sendC2S(input, true)
        }
    }

    private fun getInput(input: Input, map: AbstractObject2BooleanMap<Input>): Boolean {
        if (map.containsKey(input)) {
            return map.getBoolean(input)
        }
        return false
    }

}