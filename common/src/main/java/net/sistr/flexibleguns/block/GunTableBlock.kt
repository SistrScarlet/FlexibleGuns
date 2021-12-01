package net.sistr.flexibleguns.block

import me.shedaniel.architectury.registry.MenuRegistry
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Material
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.screen.SimpleNamedScreenHandlerFactory
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.TranslatableText
import net.minecraft.util.ActionResult
import net.minecraft.util.DyeColor
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class GunTableBlock : Block(Settings.of(Material.METAL, DyeColor.CYAN)) {

    companion object {
        val TITLE = TranslatableText("container.flexibleguns.gun_table")
    }

    override fun onUse(
        state: BlockState,
        world: World,
        pos: BlockPos?,
        player: PlayerEntity,
        hand: Hand?,
        hit: BlockHitResult?
    ): ActionResult {
        return if (world.isClient) {
            ActionResult.SUCCESS
        } else {
            MenuRegistry.openMenu(player as ServerPlayerEntity, state.createScreenHandlerFactory(world, pos))
            //player.incrementStat(Stats.INTERACT_WITH_ANVIL)
            ActionResult.CONSUME
        }
    }

    override fun createScreenHandlerFactory(
        state: BlockState?,
        world: World?,
        pos: BlockPos?
    ): NamedScreenHandlerFactory {
        return SimpleNamedScreenHandlerFactory({ i: Int, playerInventory: PlayerInventory, _: PlayerEntity ->
            GunTableScreenHandler(
                i,
                playerInventory,
                ScreenHandlerContext.create(world, pos)
            )
        }, TITLE)
    }

}