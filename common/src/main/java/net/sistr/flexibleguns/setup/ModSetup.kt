package net.sistr.flexibleguns.setup

import me.shedaniel.architectury.event.events.PlayerEvent
import me.shedaniel.architectury.registry.CreativeTabs
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import net.sistr.flexibleguns.FlexibleGunsMod
import net.sistr.flexibleguns.client.overlay.AmmoOverlay
import net.sistr.flexibleguns.client.overlay.CrossHairOverlay
import net.sistr.flexibleguns.client.overlay.HudOverlayRenderer
import net.sistr.flexibleguns.network.GunSyncPacket
import net.sistr.flexibleguns.network.Networking
import net.sistr.flexibleguns.wip.ecs.component.ComponentHolders
import net.sistr.flexibleguns.wip.ecs.component.ComponentManager
import net.sistr.flexibleguns.wip.ecs.system.Systems

object ModSetup {
    val ITEM_GROUP = CreativeTabs.create(Identifier(FlexibleGunsMod.MODID, "flexibleguns")) {
        ItemStack(Registration.GUN_ITEM_BEFORE)
    }

    fun init() {
        Networking.init()

        //ComponentHolders.init(ComponentManager.INSTANCE)
        //Systems.init()

        HudOverlayRenderer.INSTANCE.register(AmmoOverlay())
        HudOverlayRenderer.INSTANCE.register(CrossHairOverlay())

        PlayerEvent.PLAYER_JOIN.register { player ->
            GunSyncPacket.sendS2C(player)
        }
    }

}