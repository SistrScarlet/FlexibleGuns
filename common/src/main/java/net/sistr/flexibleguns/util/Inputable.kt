package net.sistr.flexibleguns.util

interface Inputable {
    fun inputKeyFG(input: Input, on: Boolean)
    fun getInputKeyFG(input: Input): Boolean
}