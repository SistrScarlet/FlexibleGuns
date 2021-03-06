package net.sistr.flexibleguns.setup

import me.shedaniel.architectury.registry.MenuRegistry
import me.shedaniel.architectury.registry.entity.EntityRenderers
import net.minecraft.util.Identifier
import net.sistr.flexibleguns.FlexibleGunsMod
import net.sistr.flexibleguns.client.FGKeys
import net.sistr.flexibleguns.client.SoundCapManager
import net.sistr.flexibleguns.client.overlay.AmmoOverlay
import net.sistr.flexibleguns.client.overlay.CrossHairOverlay
import net.sistr.flexibleguns.client.overlay.HudOverlayRenderer
import net.sistr.flexibleguns.client.renderer.BotRenderer
import net.sistr.flexibleguns.client.renderer.BulletRenderer
import net.sistr.flexibleguns.client.screen.GunTableScreen
import net.sistr.flexibleguns.item.GunInstance
import net.sistr.flexibleguns.mixin.ModelPredicateProviderRegistrySpecificAccessor
import net.sistr.flexibleguns.util.CustomItemStack

object ClientSetup {
    val GUN_SHOOT_SOUND_CAP_ID = Identifier(FlexibleGunsMod.MODID, "gun_shoot")
    val GUN_HIT_SOUND_CAP_ID = Identifier(FlexibleGunsMod.MODID, "gun_hit")

    fun init() {
        EntityRenderers.register(Registration.BULLET_ENTITY_BEFORE) { BulletRenderer(it) }
        EntityRenderers.register(Registration.BOT_ENTITY_BEFORE) { BotRenderer(it) }
        FGKeys.init()
        SoundCapManager.INSTANCE.register(GUN_SHOOT_SOUND_CAP_ID, 2)
        SoundCapManager.INSTANCE.register(GUN_HIT_SOUND_CAP_ID, 2)
        MenuRegistry.registerScreenFactory(Registration.GUN_TABLE_SCREEN_HANDLER_BEFORE) { screenHandler, inventory, title ->
            GunTableScreen(screenHandler, inventory, title)
        }
        HudOverlayRenderer.INSTANCE.register(AmmoOverlay())
        HudOverlayRenderer.INSTANCE.register(CrossHairOverlay())

        ModelPredicateProviderRegistrySpecificAccessor.callRegister(
            Registration.GUN_ITEM_BEFORE,
            Identifier(FlexibleGunsMod.MODID, "ammo")
        ) { stack, _, entity ->
            return@callRegister if (entity != null) {
                val itemIns = ((stack as Any) as CustomItemStack).getItemInstanceFG()
                (itemIns as GunInstance).getAmmoAmount().toFloat()
            } else {
                0f
            }
        }
    }

}