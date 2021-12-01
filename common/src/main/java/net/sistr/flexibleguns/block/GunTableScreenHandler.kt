package net.sistr.flexibleguns.block

import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.CraftingResultInventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.screen.slot.Slot
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.sistr.flexibleguns.setup.Registration

open class GunTableScreenHandler(
    type: ScreenHandlerType<*>?,
    syncId: Int,
    inventory: PlayerInventory,
    protected val context: ScreenHandlerContext
) :
    ScreenHandler(type, syncId) {
    protected val output = CraftingResultInventory()
    val player: PlayerEntity = inventory.player

    constructor(
        syncId: Int,
        inventory: PlayerInventory,
        context: ScreenHandlerContext
    ) : this(Registration.GUN_TABLE_SCREEN_HANDLER.get(), syncId, inventory, context)

    init {
        for (k in 0 until 3) {
            for (j in 0 until 9) {
                this.addSlot(Slot(inventory, j + k * 9 + 9, 88 + j * 18, 142 + k * 18))
            }
        }

        for (k in 0 until 9) {
            this.addSlot(Slot(inventory, k, 88 + k * 18, 200))
        }
    }

    override fun canUse(player: PlayerEntity): Boolean {
        return (this.context.get({ world: World, blockPos: BlockPos ->
            if (!this.canUse(world.getBlockState(blockPos))) false
            else player.squaredDistanceTo(
                blockPos.x.toDouble() + 0.5, blockPos.y.toDouble() + 0.5, blockPos.z.toDouble() + 0.5
            ) <= 64.0
        }, true) as Boolean)
    }

    protected fun canUse(state: BlockState): Boolean {
        return state.block == Registration.GUN_TABLE_BLOCK.get()
    }

    override fun transferSlot(player: PlayerEntity?, index: Int): ItemStack? {
        return ItemStack.EMPTY
       /* val slot = slots[index]
        return if (slot != null) slot.stack else ItemStack.EMPTY*/
    }
}