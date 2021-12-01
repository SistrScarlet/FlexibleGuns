package net.sistr.flexibleguns.util

interface PrevEntityGetter {

    fun nextPrevEntity()

    fun getPrevEntity(num: Int): PrevEntity

}