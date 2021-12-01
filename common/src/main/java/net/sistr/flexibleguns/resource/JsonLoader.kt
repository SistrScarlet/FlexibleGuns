package net.sistr.flexibleguns.resource

import com.google.gson.Gson
import com.google.gson.internal.Streams
import net.minecraft.resource.ResourceManager
import net.minecraft.util.Identifier
import net.sistr.flexibleguns.FlexibleGunsMod.LOGGER
import java.io.InputStreamReader
import java.io.Reader
import java.nio.charset.StandardCharsets

//todo ワンチャン並列化(つってもこの負荷でそれする必要があるかは疑問だが)
class JsonLoader {
    companion object {
        val INSTANCE = JsonLoader()
    }

    fun load(manager: ResourceManager) {
        LOGGER.info("now loading JsonLoader..")

        LOGGER.info("now loading Gun..")
        GunManager.INSTANCE.clear()
        for (id in manager.findResources(
            "flexibleguns/gun"
        ) { path: String -> path.endsWith(".json") }) {
            try {
                manager.getResource(id).inputStream.use { stream ->
                    val reader: Reader = InputStreamReader(stream, StandardCharsets.UTF_8)
                    val gson = Gson()
                    try {
                        gson.newJsonReader(reader).use { jsonReader ->
                            val jsonElement = Streams.parse(jsonReader)
                            val loadId = Identifier(
                                id.namespace,
                                id.path.substring("flexibleguns/gun/".length, id.path.lastIndexOf("."))
                            )
                            val builder = GunManager.INSTANCE.read(loadId, jsonElement)
                            if (builder != null) {
                                GunManager.INSTANCE.addGunSetting(loadId, builder.build())
                                LOGGER.info("$id : $loadId")
                            }
                        }
                    } catch (e: Exception) {
                        LOGGER.error("Error occurred while loading resource json $id", e)
                    }
                }
            } catch (e: Exception) {
                LOGGER.error("Error occurred while loading resource json $id", e)
            }
        }

        LOGGER.info("now loading GunTable..")
        GunRecipeManager.INSTANCE.clear()
        for (id in manager.findResources(
            "flexibleguns/gun_table"
        ) { path: String -> path.endsWith(".json") }) {
            try {
                manager.getResource(id).inputStream.use { stream ->
                    val reader: Reader = InputStreamReader(stream, StandardCharsets.UTF_8)
                    val gson = Gson()
                    try {
                        gson.newJsonReader(reader).use { jsonReader ->
                            val jsonElement = Streams.parse(jsonReader)
                            val loadId = Identifier(
                                id.namespace,
                                id.path.substring("flexibleguns/gun_table/".length, id.path.lastIndexOf("."))
                            )
                            val builder = GunRecipeManager.INSTANCE.read(loadId, jsonElement)
                            if (builder != null) {
                                GunRecipeManager.INSTANCE.registerRecipe(loadId, builder.build())
                                LOGGER.info("$id : $loadId")
                            }
                        }
                    } catch (e: Exception) {
                        LOGGER.error("Error occurred while loading resource json $id", e)
                    }
                }
            } catch (e: Exception) {
                LOGGER.error("Error occurred while loading resource json $id", e)
            }
        }


    }

}