package net.sistr.flexibleguns.resource

import com.google.common.collect.Lists
import net.minecraft.resource.ResourceManager
import net.minecraft.util.Identifier
import net.sistr.flexibleguns.FlexibleGunsMod

class DataDrivenModelLoader {
    companion object {
        val INSTANCE = DataDrivenModelLoader()
    }
    val ids = Lists.newArrayList<Identifier>()

    fun load(manager: ResourceManager) {
        FlexibleGunsMod.LOGGER.info("now loading DataDrivenModelLoader..")
        ids.clear()
        for (id in manager.findResources(
            "models/item/non_item"
        ) { path: String -> path.endsWith(".json") }) {
            val length = "models/item".length
            val path = id.path.substring(length + 1)
            val loadId = Identifier(id.namespace, path.substring(0, path.lastIndexOf(".")))
            ids.add(loadId)
            FlexibleGunsMod.LOGGER.info("$id : $loadId")
        }
    }
}