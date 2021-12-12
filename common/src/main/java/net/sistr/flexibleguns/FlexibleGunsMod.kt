package net.sistr.flexibleguns

import dev.architectury.registry.level.entity.EntityAttributeRegistry
import net.sistr.flexibleguns.entity.FGBotEntity
import net.sistr.flexibleguns.setup.Registration
import org.apache.logging.log4j.LogManager

object FlexibleGunsMod {
    const val MODID = "flexibleguns"
    val LOGGER = LogManager.getLogger()!!

    fun init() {
        Registration.init()
        EntityAttributeRegistry.register({ Registration.BOT_ENTITY.get() }, { FGBotEntity.createBotAttributes() })
    }

}