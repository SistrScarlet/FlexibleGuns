package net.sistr.flexibleguns.client.model

import net.minecraft.client.render.entity.model.CrossbowPosing
import net.minecraft.client.render.entity.model.PlayerEntityModel
import net.sistr.flexibleguns.entity.FGBotEntity

class BotModel : PlayerEntityModel<FGBotEntity>(0.5f, false) {

    override fun setAngles(hostileEntity: FGBotEntity, f: Float, g: Float, h: Float, i: Float, j: Float) {
        super.setAngles(hostileEntity, f, g, h, i, j)
        CrossbowPosing.method_29352(
            leftArm,
            rightArm, false/*this.isAttacking(hostileEntity)*/, handSwingProgress, h
        )
    }

}