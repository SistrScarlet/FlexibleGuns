package net.sistr.flexibleguns.util

interface CustomItemStack {

    fun hasItemInstanceFG(): Boolean

    fun getItemInstanceFG(): ItemInstance?

    fun setItemInstanceFG(itemInstance: ItemInstance?)

}