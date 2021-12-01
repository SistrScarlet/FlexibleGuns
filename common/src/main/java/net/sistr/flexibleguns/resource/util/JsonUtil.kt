package net.sistr.flexibleguns.resource.util

import com.google.gson.JsonElement
import java.util.*

object JsonUtil {

    fun readFloat(jsonElement: JsonElement?): Optional<Float> {
        return readNumber(jsonElement).map { it.toFloat() }
    }

    fun readInt(jsonElement: JsonElement?): Optional<Int> {
        return readNumber(jsonElement).map { it.toInt() }
    }

    fun readNumber(jsonElement: JsonElement?): Optional<Number> {
        if (jsonElement == null || !jsonElement.isJsonPrimitive) {
            return Optional.empty()
        }
        val primitive = jsonElement.asJsonPrimitive
        return if (primitive.isNumber) {
            Optional.of(primitive.asNumber)
        } else {
            Optional.empty()
        }
    }

    fun readString(jsonElement: JsonElement?): Optional<String> {
        if (jsonElement == null || !jsonElement.isJsonPrimitive) {
            return Optional.empty()
        }
        val primitive = jsonElement.asJsonPrimitive
        return if (primitive.isString) {
            Optional.of(primitive.asString)
        } else {
            Optional.empty()
        }
    }

}