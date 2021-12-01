package net.sistr.flexibleguns.entity.util

import com.google.common.collect.ImmutableList
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import java.util.concurrent.ConcurrentHashMap

//senseが起きると非同期で視界を取り記憶に収める
//記憶には対象とその位置が記録される
//記憶は100tickしか記憶できない
//クライアント側で駆動するとクラッシュする(はず)
class SenseMemory(private val owner: LivingEntity, private val senses: ISense) {
    private val memoriesMap = ConcurrentHashMap<Int, ImmutableList<Memory>>()
    private var sensed = false

    fun tick() {
        sensed = false
        ImmutableList.copyOf(memoriesMap.keys).stream()
            .filter { it + 100 < owner.age }
            .forEach { memoriesMap.remove(it) }
    }

    fun sense() {
        if (sensed) {
            return
        }
        sensed = true
        val memories = ImmutableList.copyOf(senses.getMemories())
        if (!memories.isEmpty()) {
            val time = memories[0].time
            memoriesMap[time] = memories
        }
    }

    data class Memory(val target: Entity, val pos: Pos, val time: Int)

    data class Pos(val x: Float, val y: Float, val z: Float)

    interface ISense {
        fun getMemories(): Collection<Memory>
    }

    class EyeSense(val owner: LivingEntity, val range: Float, val fov: Float) : ISense {

        override fun getMemories(): Collection<Memory> {
            val sightBox = owner.boundingBox.stretch(owner.rotationVector.multiply(range.toDouble())).expand(1.0)
            return owner.world.getOtherEntities(owner, sightBox)
                .map { Memory(it, Pos(it.x.toFloat(), it.y.toFloat(), it.z.toFloat()), owner.age) }
        }

    }

}