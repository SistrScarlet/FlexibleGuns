package net.sistr.flexibleguns.wip.ecs.component

import net.minecraft.util.Identifier
import net.sistr.flexibleguns.FlexibleGunsMod
import net.sistr.flexibleguns.wip.ecs.component.item.ItemComponent
import net.sistr.flexibleguns.wip.ecs.component.item.Hold
import net.sistr.flexibleguns.wip.ecs.component.item.HoldSpeed
import net.sistr.flexibleguns.wip.ecs.component.item.gun.*

object ComponentHolders {
    val ITEM_COMPONENT_ID = Identifier(FlexibleGunsMod.MODID, "item_component")
    val ITEM_HOLD_ID = Identifier(FlexibleGunsMod.MODID, "item_hold")
    val ITEM_HOLD_SPEED_ID = Identifier(FlexibleGunsMod.MODID, "item_hold_speed")
    val ITEM_SHOOT_DELAY_ID = Identifier(FlexibleGunsMod.MODID, "item_shoot_delay")
    val ITEM_SHOOTABLE_ID = Identifier(FlexibleGunsMod.MODID, "item_shootable")
    val ITEM_AMMO_LOADABLE_ID = Identifier(FlexibleGunsMod.MODID, "item_ammo_loadable")
    val ITEM_RELOADABLE_ID = Identifier(FlexibleGunsMod.MODID, "item_reloadable")
    val ITEM_ZOOMABLE_ID = Identifier(FlexibleGunsMod.MODID, "item_zoomable")
    val ITEM_BURSTABLE_ID = Identifier(FlexibleGunsMod.MODID, "item_holds")
    val ITEM_SLIDE_ACTION_ID = Identifier(FlexibleGunsMod.MODID, "item_holds")

    val ITEM_COMPONENT_HOLDER = ComponentHolder<ItemComponent>()
    val ITEM_HOLD_HOLDER = ComponentHolder<Hold>()
    val ITEM_HOLD_SPEED_HOLDER = ComponentHolder<HoldSpeed>()
    val ITEM_SHOOT_DELAY_HOLDER = ComponentHolder<ShootDelay>()
    val ITEM_SHOOTABLE_HOLDER = ComponentHolder<Shootable>()
    val ITEM_AMMO_LOADABLE_HOLDER = ComponentHolder<AmmoLoadable>()
    val ITEM_RELOADABLE_HOLDER = ComponentHolder<Reloadable>()
    val ITEM_ZOOMABLE_HOLDER = ComponentHolder<Zoomable>()
    val ITEM_BURSTABLE_HOLDER = ComponentHolder<Burstable>()
    val ITEM_SLIDE_ACTION_HOLDER = ComponentHolder<SlideAction>()

    fun init(componentManager: ComponentManager) {
        componentManager.register(ITEM_COMPONENT_ID, ITEM_COMPONENT_HOLDER)
        componentManager.register(ITEM_HOLD_ID, ITEM_HOLD_HOLDER)
        componentManager.register(ITEM_HOLD_SPEED_ID, ITEM_HOLD_SPEED_HOLDER)
        componentManager.register(ITEM_SHOOT_DELAY_ID, ITEM_SHOOT_DELAY_HOLDER)
        componentManager.register(ITEM_SHOOTABLE_ID, ITEM_SHOOTABLE_HOLDER)
        componentManager.register(ITEM_AMMO_LOADABLE_ID, ITEM_AMMO_LOADABLE_HOLDER)
        componentManager.register(ITEM_RELOADABLE_ID, ITEM_RELOADABLE_HOLDER)
        componentManager.register(ITEM_ZOOMABLE_ID, ITEM_ZOOMABLE_HOLDER)
        componentManager.register(ITEM_BURSTABLE_ID, ITEM_BURSTABLE_HOLDER)
        componentManager.register(ITEM_SLIDE_ACTION_ID, ITEM_SLIDE_ACTION_HOLDER)

    }

}