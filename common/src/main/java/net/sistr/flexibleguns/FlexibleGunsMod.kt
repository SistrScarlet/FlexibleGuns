package net.sistr.flexibleguns

import me.shedaniel.architectury.registry.entity.EntityAttributes
import net.sistr.flexibleguns.entity.FGBotEntity
import net.sistr.flexibleguns.setup.Registration
import org.apache.logging.log4j.LogManager

object FlexibleGunsMod {
    const val MODID = "flexibleguns"
    val LOGGER = LogManager.getLogger()!!

    fun init() {
        Registration.init()
        EntityAttributes.register({ Registration.BOT_ENTITY.get() }, { FGBotEntity.createBotAttributes() })
    }

}