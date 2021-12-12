package net.sistr.flexibleguns.item.util

import com.google.common.collect.ImmutableList
import com.google.common.collect.Lists
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.util.Identifier

class SoundDataHolder private constructor(private val length: Int, soundMap: Map<Int, List<SoundData>>) {
    private val soundMap: Int2ObjectArrayMap<ImmutableList<SoundData>> = run {
        val map = Int2ObjectArrayMap<ImmutableList<SoundData>>()
        soundMap.forEach { (time, list) -> map[time] = ImmutableList.copyOf(list) }
        map
    }

    companion object {
        val EMPTY = getBuilder().build()
        fun getBuilder(): Builder {
            return Builder()
        }
    }

    fun getSound(time: Int): ImmutableList<SoundData> {
        if (length < time) {
            return ImmutableList.of()
        }
        val sounds = soundMap[time]
        return sounds ?: ImmutableList.of()
    }

    fun write(): NbtCompound {
        val tag = NbtCompound()
        tag.putInt("length", length)

        val entriesTag = NbtList()
        soundMap.forEach { entry ->
            val entryTag = NbtCompound()
            entryTag.putInt("time", entry.key)

            val soundsTag = NbtList()
            entry.value.forEach { soundData ->
                val soundDataTag = NbtCompound()
                soundDataTag.putString("id", soundData.sound.toString())
                soundDataTag.putFloat("pitch", soundData.pitch)
                soundDataTag.putFloat("volume", soundData.volume)
                soundsTag.add(soundDataTag)
            }
            entryTag.put("sounds", soundsTag)

            entriesTag.add(entryTag)
        }
        tag.put("entries", entriesTag)

        return tag
    }

    class Builder {
        private var length: Int = 0
        private val soundMap: Int2ObjectArrayMap<MutableList<SoundData>> = Int2ObjectArrayMap()

        fun setLength(length: Int): Builder {
            this.length = length
            return this
        }

        fun addSound(time: Int, sound: SoundData): Builder {
            val sounds: MutableList<SoundData> =
                if (soundMap.containsKey(time)) soundMap.get(time) else {
                    val list: MutableList<SoundData> = Lists.newArrayList()
                    soundMap[time] = list
                    list
                }
            //動かなくて腹立つ
            //soundMap.computeIfAbsent(time) { Lists.newArrayList() }
            sounds.add(sound)
            return this
        }

        fun addSound(sound: SoundData): Builder {
            return addSound(0, sound)
        }

        fun read(tag: NbtCompound): Builder {
            this.setLength(tag.getInt("length"))
            val entriesTag = tag.get("entries")
            if (entriesTag is NbtList) {
                entriesTag.forEach { entryTag ->
                    if (entryTag is NbtCompound) {
                        val time = entryTag.getInt("time")
                        val soundsTag = entryTag.get("sounds")
                        if (soundsTag is NbtList) {
                            soundsTag.forEach {
                                if (it is NbtCompound) {
                                    val id = Identifier(it.getString("id"))
                                    val pitch = it.getFloat("pitch")
                                    val volume = it.getFloat("volume")
                                    this.addSound(time, SoundData(id, pitch, volume))
                                }
                            }
                        }
                    }
                }
            }
            return this
        }

        fun build(): SoundDataHolder {
            return SoundDataHolder(length, soundMap)
        }

    }

}