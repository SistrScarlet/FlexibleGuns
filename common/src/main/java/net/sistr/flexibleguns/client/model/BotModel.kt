package net.sistr.flexibleguns.client.model

import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.model.CrossbowPosing
import net.minecraft.client.render.entity.model.EntityModelLayers
import net.minecraft.client.render.entity.model.PlayerEntityModel
import net.sistr.flexibleguns.entity.FGBotEntity

class BotModel(context: EntityRendererFactory.Context) :
    PlayerEntityModel<FGBotEntity>(context.getPart(EntityModelLayers.PLAYER), false) {

    override fun setAngles(hostileEntity: FGBotEntity, f: Float, g: Float, h: Float, i: Float, j: Float) {
        super.setAngles(hostileEntity, f, g, h, i, j)
        CrossbowPosing.hold(rightArm, leftArm, head, false)
    }

}