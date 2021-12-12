package net.sistr.flexibleguns.resource

import com.google.common.collect.ImmutableList
import com.google.common.collect.Lists
import com.google.gson.JsonElement
import net.minecraft.network.PacketByteBuf
import net.minecraft.tag.ServerTagManagerHolder
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import net.sistr.flexibleguns.resource.util.JsonUtil

class GunRecipeManager {
    private val recipes = HashMap<Identifier, GunRecipe>()
    private val gunIdToRecipe = HashMap<Identifier, ArrayList<GunRecipe>>()

    companion object {
        val INSTANCE = GunRecipeManager()
    }

    fun registerRecipe(recipeId: Identifier, gunRecipe: GunRecipe) {
        recipes[recipeId] = gunRecipe
        gunIdToRecipe.computeIfAbsent(gunRecipe.resultId) { Lists.newArrayList() }
            .add(gunRecipe)
    }

    fun getRecipe(recipeId: Identifier): GunRecipe? {
        return recipes[recipeId]
    }

    fun getRecipeFromGunId(gunId: Identifier): List<GunRecipe> {
        return ImmutableList.copyOf(gunIdToRecipe[gunId] ?: ImmutableList.of())
    }

    fun isContainGunRecipe(gunId: Identifier): Boolean {
        return gunIdToRecipe.containsKey(gunId)
    }

    fun clear() {
        recipes.clear()
        gunIdToRecipe.clear()
    }

    fun read(id: Identifier, jsonElement: JsonElement): GunRecipe.Builder? {
        if (!jsonElement.isJsonObject) {
            return null
        }
        val builder = GunRecipe.Builder(id)
        val jsonObject = jsonElement.asJsonObject
        JsonUtil.readString(jsonObject["result"]).ifPresent { builder.setResult(Identifier(it)) }
        jsonObject.entrySet().stream()
            .filter { it.value.isJsonObject && it.key.contains("material") }
            .map { it.value.asJsonObject }
            .forEach { material ->
                JsonUtil.readString(material["type"]).ifPresent { type ->
                    JsonUtil.readString(material["id"]).map { Identifier(it) }.ifPresent { id ->
                        val count = JsonUtil.readInt(material["count"]).orElse(1)
                        when (type) {
                            "item" -> {
                                Registry.ITEM.getOrEmpty(id).ifPresent {
                                    builder.addPredicate(count, it)
                                }
                            }
                            "tag" -> {
                                val tag = ServerTagManagerHolder.getTagManager()
                                    .getTag(Registry.ITEM_KEY, id) { IllegalStateException("Unknown item tag") }
                                if (tag != null) {
                                    builder.addPredicate(count, tag)
                                }
                            }
                            "gun" -> {
                                builder.addPredicate(count, id)
                            }
                        }
                    }
                }
            }
        return builder
    }

    fun write(buf: PacketByteBuf) {
        recipes.forEach {
            buf.writeBoolean(true)
            buf.writeIdentifier(it.key)
            it.value.write(buf)
        }
        buf.writeBoolean(false)
    }

    fun read(buf: PacketByteBuf) {
        while (buf.readBoolean()) {
            val id = buf.readIdentifier()
            val recipe = GunRecipe.Builder(buf)
            registerRecipe(id, recipe.build())
        }
    }

}