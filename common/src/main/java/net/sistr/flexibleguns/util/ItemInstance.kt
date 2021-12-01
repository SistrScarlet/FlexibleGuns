package net.sistr.flexibleguns.util

interface ItemInstance {
    companion object {
        val EMPTY = object : ItemInstance {
            override fun tick() {}
            override fun remove() {}
        }
    }

    fun tick()

    //ItemStackが破棄されている場合があるため、NBTへの保存はここでするべきでない
    fun remove()

}