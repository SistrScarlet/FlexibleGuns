package net.sistr.flexibleguns.setup

import dev.architectury.registry.menu.MenuRegistry
import dev.architectury.registry.registries.DeferredRegister
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.SpawnEggItem
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.util.registry.Registry
import net.minecraft.world.World
import net.sistr.flexibleguns.FlexibleGunsMod
import net.sistr.flexibleguns.block.GunTableBlock
import net.sistr.flexibleguns.block.GunTableScreenHandler
import net.sistr.flexibleguns.entity.FGBotEntity
import net.sistr.flexibleguns.entity.FGBulletEntity
import net.sistr.flexibleguns.item.GunItem

object Registration {
    private val BLOCKS = DeferredRegister.create(FlexibleGunsMod.MODID, Registry.BLOCK_KEY)
    private val ITEMS = DeferredRegister.create(FlexibleGunsMod.MODID, Registry.ITEM_KEY)
    private val ENTITY_TYPE = DeferredRegister.create(FlexibleGunsMod.MODID, Registry.ENTITY_TYPE_KEY)
    private val SCREEN_HANDLER = DeferredRegister.create(FlexibleGunsMod.MODID, Registry.MENU_KEY)

    fun init() {
        BLOCKS.register()
        ITEMS.register()
        ENTITY_TYPE.register()
        SCREEN_HANDLER.register()
    }

    val GUN_TABLE_BLOCK = BLOCKS.register("gun_table") {
        GunTableBlock() }
    val GUN_TABLE_ITEM = ITEMS.register("gun_table") {
        BlockItem(GUN_TABLE_BLOCK.get(), Item.Settings().group(ModSetup.ITEM_GROUP))
    }

    val GUN_ITEM_BEFORE = GunItem(Item.Settings().maxCount(1))
    val GUN_ITEM = ITEMS.register("gun") { GUN_ITEM_BEFORE }
    val BOT_SPAWN_EGG = ITEMS.register("bot_spawn_egg") {
        SpawnEggItem(BOT_ENTITY_BEFORE, 0xFFFFFF, 0x0, Item.Settings().group(ModSetup.ITEM_GROUP))
    }

    val BULLET_ENTITY_BEFORE = EntityType.Builder.create(
        { type: EntityType<FGBulletEntity>, world: World ->
            FGBulletEntity(type, world)
        }, SpawnGroup.MISC
    ).setDimensions(0.05f, 0.05f)
        .maxTrackingRange(4)
        .trackingTickInterval(20)
        .build("bullet")
    val BULLET_ENTITY = ENTITY_TYPE.register("bullet") {
        BULLET_ENTITY_BEFORE
    }
    val BOT_ENTITY_BEFORE = EntityType.Builder.create(
        { type: EntityType<FGBotEntity>, world: World ->
            FGBotEntity(type, world)
        }, SpawnGroup.MONSTER
    ).setDimensions(0.6f, 1.8f)
        .maxTrackingRange(32)
        .trackingTickInterval(2)
        .build("bullet")
    val BOT_ENTITY = ENTITY_TYPE.register("bot") { BOT_ENTITY_BEFORE }

    val GUN_TABLE_SCREEN_HANDLER_BEFORE = MenuRegistry.of { syncId: Int, inventory: PlayerInventory ->
        GunTableScreenHandler(syncId, inventory, ScreenHandlerContext.EMPTY)
    }
    val GUN_TABLE_SCREEN_HANDLER = SCREEN_HANDLER.register("gun_table") {
        GUN_TABLE_SCREEN_HANDLER_BEFORE
    }

}