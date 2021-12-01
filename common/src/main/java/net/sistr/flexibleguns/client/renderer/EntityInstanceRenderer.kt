package net.sistr.flexibleguns.client.renderer

import net.minecraft.client.font.TextRenderer
import net.minecraft.client.render.Frustum
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRenderDispatcher
import net.minecraft.client.render.entity.EntityRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.sistr.flexibleguns.wip.ecs.component.entity.util.EntityInstance

class EntityInstanceRenderer<T : EntityInstance>(dispatcher: EntityRenderDispatcher?) : EntityRenderer<T>(dispatcher) {

    override fun method_27950(entity: T, blockPos: BlockPos?): Int {
        return super.method_27950(entity, blockPos)
    }

    override fun getBlockLight(entity: T, blockPos: BlockPos?): Int {
        return super.getBlockLight(entity, blockPos)
    }

    override fun shouldRender(entity: T, frustum: Frustum?, x: Double, y: Double, z: Double): Boolean {
        return super.shouldRender(entity, frustum, x, y, z)
    }

    override fun getPositionOffset(entity: T, tickDelta: Float): Vec3d {
        return super.getPositionOffset(entity, tickDelta)
    }

    override fun render(
        entity: T,
        yaw: Float,
        tickDelta: Float,
        matrices: MatrixStack?,
        vertexConsumers: VertexConsumerProvider?,
        light: Int
    ) {
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light)
    }

    override fun hasLabel(entity: T): Boolean {
        return super.hasLabel(entity)
    }

    override fun renderLabelIfPresent(
        entity: T,
        text: Text?,
        matrices: MatrixStack?,
        vertexConsumers: VertexConsumerProvider?,
        light: Int
    ) {
        super.renderLabelIfPresent(entity, text, matrices, vertexConsumers, light)
    }

    override fun getTexture(entity: T): Identifier {
        return Identifier("")
    }
}