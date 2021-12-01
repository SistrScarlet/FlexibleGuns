package net.sistr.flexibleguns.client.renderer.util

import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.util.math.MatrixStack

object NumberRenderer {

    fun renderNumber(matrices: MatrixStack, number: Int, x: Int, y: Int, z: Int, least: Int, toRight: Boolean) {
        var mutNum = number
        val queue = IntArrayFIFOQueue()
        if (mutNum == 0) {
            queue.enqueue(0)
        } else {
            while (true) {
                val mostRightValue = mutNum % 10
                queue.enqueue(mostRightValue)
                mutNum /= 10
                if (mutNum == 0) {
                    break
                }
            }
        }
        while (queue.size() < least) {
            queue.enqueue(0)
        }
        val xOffset = if (toRight) 0 else queue.size() * 16
        var count = 0
        while (!queue.isEmpty) {
            val num = queue.dequeueLastInt()
            DrawableHelper.drawTexture(
                matrices,
                x + 16 * count - xOffset, y, z,
                (getU(num) * 16).toFloat(), (getV(num) * 16).toFloat(),
                16, 16,
                64, 64
            )
            count++
        }
    }

    private fun getU(number: Int): Int {
        return number % 4
    }

    private fun getV(number: Int): Int {
        return number / 4
    }

}