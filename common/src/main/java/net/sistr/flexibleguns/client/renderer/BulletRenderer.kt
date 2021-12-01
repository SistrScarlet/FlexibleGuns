package net.sistr.flexibleguns.client.renderer

import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRenderDispatcher
import net.minecraft.client.render.entity.EntityRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Matrix3f
import net.minecraft.util.math.Matrix4f
import net.minecraft.util.math.Vec3f
import net.sistr.flexibleguns.FlexibleGunsMod
import net.sistr.flexibleguns.client.model.BulletModel
import net.sistr.flexibleguns.entity.FGBulletEntity

class BulletRenderer<T : FGBulletEntity>(dispatcher: EntityRenderDispatcher?) : EntityRenderer<T>(dispatcher) {
    companion object {
        val BULLET_TEXTURE = Identifier(FlexibleGunsMod.MODID, "textures/entity/projectiles/bullet.png")
        val TRAIT_TEXTURE = Identifier(FlexibleGunsMod.MODID, "textures/entity/projectiles/trait.png")
        val BULLET_MODEL = BulletModel()
    }

    override fun render(
        entity: T,
        yaw: Float,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int
    ) {
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light)
        matrices.push()
        matrices.multiply(
            Vec3f.POSITIVE_Y.getDegreesQuaternion(
                MathHelper.lerp(tickDelta, entity.prevYaw, entity.yaw) - 180f
            )
        )
        matrices.multiply(
            Vec3f.POSITIVE_X.getDegreesQuaternion(
                MathHelper.lerp(
                    tickDelta,
                    entity.prevPitch,
                    entity.pitch
                )
            )
        )


        /*val traitLength = if (entity.age <= 1) {
            val pos = entity.pos
            val prevPos = Vec3d(entity.prevX, entity.prevY, entity.prevZ)
            val length = pos.subtract(prevPos).length().toFloat()
            MathHelper.lerp(tickDelta, 0f, length)
        } else {
            val pV = entity.prevVelocity
            val v = entity.velocity
            val dV = Vec3d(
                MathHelper.lerp(tickDelta.toDouble(), pV.x, v.x),
                MathHelper.lerp(tickDelta.toDouble(), pV.y, v.y),
                MathHelper.lerp(tickDelta.toDouble(), pV.z, v.z)
            )
            dV.length().toFloat()
        }

        val traitVC = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(TRAIT_TEXTURE))
        val model = matrices.peek().model
        val normal = matrices.peek().normal
        val r = 0.95f
        val g = 0.95f
        val b = 0.95f
        vertex(
            traitVC, model, normal,
            0f, -traitLength / 2f, 0f,
            1f, 0f, 0f,
            r, g, b, 0.9f,
            0f, 0f, 0xF000F0
        )
        vertex(
            traitVC, model, normal,
            0f, -traitLength / 2f, traitLength,
            1f, 0f, 0f,
            r, g, b, 0.9f,
            1f, 0f, 0xF000F0
        )
        vertex(
            traitVC, model, normal,
            0f, traitLength / 2f, traitLength,
            1f, 0f, 0f,
            r, g, b, 0.9f,
            1f, 1f, 0xF000F0
        )
        vertex(
            traitVC, model, normal,
            0f, traitLength / 2f, 0f,
            1f, 0f, 0f,
            r, g, b, 0.9f,
            0f, 1f, 0xF000F0
        )*/

        matrices.scale(1f, -1f, 1f)
        matrices.scale(0.25f, 0.25f, 0.25f)
        val bulletVC = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(getTexture(entity)))
        BULLET_MODEL.render(
            matrices, bulletVC, 0xF000F0, OverlayTexture.DEFAULT_UV,
            0.4f, 0.3f, 0.1f, 1f
        )
        matrices.pop()
    }

    private fun vertex(
        vertexConsumer: VertexConsumer,
        matrix4f: Matrix4f,
        matrix3f: Matrix3f,
        x: Float, y: Float, z: Float,
        nX: Float, nZ: Float, nY: Float,
        r: Float, g: Float, b: Float, a: Float,
        u: Float, v: Float,
        light: Int
    ) {
        vertexConsumer
            .vertex(matrix4f, x, y, z)
            .color(r, g, b, a)
            .texture(u, v)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(light)
            .normal(matrix3f, nX, nY, nZ)
            .next()
    }

    override fun getTexture(entity: T): Identifier {
        return BULLET_TEXTURE
    }
}