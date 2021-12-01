package net.sistr.flexibleguns.client.renderer

import net.minecraft.client.render.entity.BipedEntityRenderer
import net.minecraft.client.render.entity.EntityRenderDispatcher
import net.minecraft.client.render.entity.model.BipedEntityModel
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import net.sistr.flexibleguns.FlexibleGunsMod
import net.sistr.flexibleguns.client.model.BotModel
import net.sistr.flexibleguns.entity.FGBotEntity

open class BotRenderer(dispatcher: EntityRenderDispatcher) :
    BipedEntityRenderer<FGBotEntity, BipedEntityModel<FGBotEntity>>(
        dispatcher,
        BotModel(),
        0.5f
    ) {
    private val TEXTURE = Identifier(FlexibleGunsMod.MODID, "textures/entity/bot.png")

    override fun scale(entity: FGBotEntity, matrixStack: MatrixStack, f: Float) {
        val scale = 0.9375f
        matrixStack.scale(scale, scale, scale)
    }

    override fun getTexture(entity: FGBotEntity?): Identifier {
        return TEXTURE
    }
}