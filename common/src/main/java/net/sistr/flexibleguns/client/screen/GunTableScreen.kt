package net.sistr.flexibleguns.client.screen

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.gui.screen.ingame.BeaconScreen
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerListener
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Identifier
import net.minecraft.util.math.MathHelper
import net.sistr.flexibleguns.FlexibleGunsMod
import net.sistr.flexibleguns.block.GunTableScreenHandler
import net.sistr.flexibleguns.network.GunCraftPacket
import net.sistr.flexibleguns.resource.GunManager
import net.sistr.flexibleguns.resource.GunRecipe
import net.sistr.flexibleguns.resource.GunRecipeManager
import net.sistr.flexibleguns.resource.GunSetting
import net.sistr.flexibleguns.setup.Registration
import kotlin.math.roundToInt

open class GunTableScreen<T : GunTableScreenHandler>(handler: T, val inventory: PlayerInventory, title: Text) :
    HandledScreen<T>(
        handler,
        inventory, title
    ), ScreenHandlerListener {
    private val guns: ImmutableList<Data> = kotlin.run {
        val builder = ImmutableList.builder<Data>()
        GunManager.INSTANCE.getGunSettings().entries.stream()
            .sorted(Comparator.comparing { it.key.toString() })
            .forEach {
                builder.add(Data(it.key.toString().replace("/", "."), it.value))
            }
        builder.build()
    }
    private val recipes: ImmutableMap<Identifier, ImmutableList<GunRecipe>> = kotlin.run {
        val builder = ImmutableMap.builder<Identifier, ImmutableList<GunRecipe>>()
        val recipeManager = GunRecipeManager.INSTANCE
        guns.stream()
            .filter { recipeManager.isContainGunRecipe(it.setting.gunId) }
            .map { it.setting.gunId }
            .forEach {
                builder.put(
                    it,
                    ImmutableList.copyOf(recipeManager.getRecipeFromGunId(it))
                )
            }
        builder.build()
    }
    private var point = -1//要らん？
    private var gun: Data? = null
    private var recipe: ImmutableList<GunRecipe> = ImmutableList.of()
    private var craftableRecipeMap: ImmutableMap<Identifier, Boolean> = ImmutableMap.of()
    private var recipeRollCounter = 0
    private var recipeDrawnAtPrevFrame = false

    companion object {
        val TEXTURE = Identifier(FlexibleGunsMod.MODID, "textures/gui/container/gun_table.png")
    }

    init {
        titleX = 87
        backgroundWidth = 256
        backgroundHeight = 224
        playerInventoryTitleX = 88
        playerInventoryTitleY = 132
    }

    protected fun setup() {

    }

    override fun init() {
        super.init()
        setup()
        (handler as GunTableScreenHandler).addListener(this)
    }

    override fun removed() {
        super.removed()
        (handler as GunTableScreenHandler).removeListener(this)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        val xBase = (width - backgroundWidth) / 2 + 5
        val yBase = (height - backgroundHeight) / 2 + 5
        val x = (mouseX - xBase).toInt() / 16
        val y = (mouseY - yBase).toInt() / 16
        val gunIndex = x + y * 5
        if (x in 0..4 && 0 <= y && gunIndex < guns.size) {
            point = gunIndex
            gun = guns[gunIndex]

            recipe = recipes.getOrElse(gun!!.setting.gunId) { ImmutableList.of() }
            //クラフトできるかチェック
            //銃クリック時に判定するため、クリック時とクラフト時で異なる場合がある
            val builder = ImmutableMap.builder<Identifier, Boolean>()
            recipe.forEach {
                builder.put(
                    it.getRecipeId(),
                    it.match(this.inventory) || this.inventory.player.isCreative
                )
            }
            craftableRecipeMap = builder.build()
        }
        //クラフトボタン押したときの処理
        else if (gun != null) {
            if (xBase + 87 <= mouseX && mouseX < xBase + 87 + 9 * 18
                && yBase + 73 <= mouseY && mouseY < yBase + 73 + 18 * 3
            ) {
                val recipeIndex: Int = MathHelper.floor((mouseY - (yBase + 73)) / 18F)
                if (0 <= recipeIndex && recipeIndex < this.recipe.size) {
                    val recipe = this.recipe[recipeIndex]
                    GunCraftPacket.sendC2S(recipe.getRecipeId())
                    //鯖でやる
                    /*if (recipe.match(this.playerInventory)) {
                        val result = recipe.craft(this.playerInventory)
                        if (!this.playerInventory.insertStack(result)) {
                            val dropItemEntity = this.playerInventory.player.dropItem(result, true)
                            if (dropItemEntity != null) {
                                this.playerInventory.player.world.spawnEntity(dropItemEntity)
                            }
                        }
                    }*/
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        this.renderBackground(matrices)
        super.render(matrices, mouseX, mouseY, delta)
        RenderSystem.disableBlend()
        renderForeground(matrices, mouseX, mouseY, delta)
        drawMouseoverTooltip(matrices, mouseX, mouseY)
    }

    private fun renderForeground(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        val showGuns = ItemStack(Registration.GUN_ITEM.get())
        val nbt = showGuns.orCreateNbt
        var num = 0
        val xBase = (width - backgroundWidth) / 2
        val yBase = (height - backgroundHeight) / 2
        guns.stream()
            .forEach {
                nbt.putString("CustomTextureID", it.setting.textureId.toString() + "#inventory")
                this.client!!.itemRenderer.renderInGui(showGuns, xBase + 5 + (num % 5) * 16, yBase + 5 + (num / 5) * 16)
                RenderSystem.setShader { GameRenderer.getPositionTexShader() }
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
                RenderSystem.setShaderTexture(0, TEXTURE)
                if (it === gun) {
                    zOffset = 300
                    this.drawTexture(
                        matrices,
                        xBase + 4 + (num % 5) * 16, yBase + 4 + (num / 5) * 16,
                        80, 224,
                        18, 18
                    )
                    zOffset = 0
                }
                num++
            }

        if (gun != null) {
            //160x57
            val setting = gun!!.setting
            var layer = 0
            val fontHeight = textRenderer.fontHeight
            val hHalf = 160 / 2
            val textXBase = xBase.toFloat() + 89
            val textYBase = yBase.toFloat() + 16
            textRenderer.draw(
                matrices,
                TranslatableText(
                    "gun." + setting.gunId.namespace + "." +
                            setting.gunId.path.replace("/", ".")
                ),
                textXBase, textYBase + fontHeight * layer,
                0x3F3F3F
            )

            ++layer
            textRenderer.draw(
                matrices,
                TranslatableText(
                    "gun." + setting.gunId.namespace + "." +
                            setting.gunId.path.replace("/", ".") + ".lore"
                ),
                textXBase, textYBase + fontHeight * layer,
                0x3F3F3F
            )
            ++layer
            ++layer
            textRenderer.draw(
                matrices,
                TranslatableText("gun_table.damage")
                    .append(LiteralText(" : " + ((setting.damage * 100).roundToInt() / 100F).toString())),
                textXBase, textYBase + fontHeight * layer,
                0x3F3F3F
            )
            textRenderer.draw(
                matrices,
                TranslatableText("gun_table.headshot")
                    .append(
                        LiteralText(
                            " : " + (((setting.damage * setting.headshotAmplifier) * 100).roundToInt() / 100F).toString()
                                    + " (" + (setting.headshotAmplifier) + "x)"
                        )
                    ),
                textXBase + hHalf, textYBase + fontHeight * layer,
                0x3F3F3F
            )

            ++layer
            textRenderer.draw(
                matrices,
                TranslatableText("gun_table.rateOfFire")
                    .append(
                        LiteralText(" : " + (1200 / setting.fireInterval).roundToInt().toString() + " RPM")
                    ),
                textXBase, textYBase + fontHeight * layer,
                0x3F3F3F
            )

            ++layer

            var recipeNum = 0
            recipe.forEach { gunRecipe ->
                var materialNum = 0
                gunRecipe.getMaterialExample().forEach { materials ->
                    RenderSystem.setShader { GameRenderer.getPositionTexShader() }
                    RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
                    RenderSystem.setShaderTexture(0, TEXTURE)
                    this.drawTexture(
                        matrices,
                        xBase + 87 + materialNum * 18, yBase + 73 + recipeNum * 18,
                        40, 224,
                        18, 18
                    )
                    val index = (recipeRollCounter / 30) % materials.size
                    val stack = materials[index]
                    this.client!!.itemRenderer.renderInGuiWithOverrides(
                        stack,
                        xBase + 88 + materialNum * 18,
                        yBase + 74 + recipeNum * 18
                    )
                    this.client!!.itemRenderer.renderGuiItemOverlay(
                        this.textRenderer,
                        stack,
                        xBase + 88 + materialNum * 18,
                        yBase + 74 + recipeNum * 18, null
                    )
                    materialNum++
                }
                recipeNum++
            }
            //クラフトボタンの表示
            if (xBase + 87 <= mouseX && mouseX < xBase + 87 + 9 * 18
                && yBase + 73 <= mouseY && mouseY < yBase + 73 + 18 * 3
            ) {
                RenderSystem.setShader { GameRenderer.getPositionTexShader() }
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
                RenderSystem.setShaderTexture(0, TEXTURE)
                val index: Int = MathHelper.floor((mouseY - (yBase + 73)) / 18F)

                //前のフレームで表示してなかったら
                if (!recipeDrawnAtPrevFrame) {
                    recipeDrawnAtPrevFrame = true
                    //クラフトできるかチェック
                    val builder = ImmutableMap.builder<Identifier, Boolean>()
                    recipe.forEach {
                        builder.put(
                            it.getRecipeId(),
                            it.match(this.inventory) || this.inventory.player.isCreative
                        )
                    }
                    craftableRecipeMap = builder.build()
                }

                val craftable = kotlin.run {
                    if (index < 0 || this.recipe.size <= index) return@run false
                    val recipe = this.recipe[index]
                    craftableRecipeMap.getOrElse(recipe.getRecipeId()) { false }
                }
                this.zOffset = 300
                this.drawTexture(
                    matrices,
                    xBase + 87, yBase + 74 + index * 18,
                    0, 224 + if (craftable) 0 else 16,
                    35, 16
                )
                this.zOffset = 0
            } else {
                recipeDrawnAtPrevFrame = false
            }
        }
    }

    override fun drawBackground(matrices: MatrixStack, delta: Float, mouseX: Int, mouseY: Int) {
        RenderSystem.setShader { GameRenderer.getPositionTexShader() }
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
        RenderSystem.setShaderTexture(0, TEXTURE)
        val i = (width - backgroundWidth) / 2
        val j = (height - backgroundHeight) / 2
        this.drawTexture(matrices, i, j, 0, 0, backgroundWidth, backgroundHeight)
    }

    /*override fun onHandlerRegistered(handler: ScreenHandler, stacks: DefaultedList<ItemStack>) {
        onSlotUpdate(handler, 0, handler.getSlot(0).stack)
    }*/

    override fun onSlotUpdate(handler: ScreenHandler, slotId: Int, stack: ItemStack?) {

    }

    override fun onPropertyUpdate(handler: ScreenHandler, property: Int, value: Int) {

    }

    override fun handledScreenTick() {
        super.handledScreenTick()
        this.recipeRollCounter++
    }

    data class Data(val name: String, val setting: GunSetting)

}