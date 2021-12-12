// Made with Blockbench 3.8.4
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports

package net.sistr.flexibleguns.client.model;

import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.sistr.flexibleguns.FlexibleGunsMod;

public class BulletModel extends EntityModel<Entity> {
    public static final EntityModelLayer MODEL_LAYER = new EntityModelLayer(new Identifier(FlexibleGunsMod.MODID, "bulletmodel"), "main");
    private final ModelPart bb_main;

    public BulletModel(ModelPart root) {
        this.bb_main = root.getChild("bb_main");
    }

    public static TexturedModelData createBodyLayer() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();

        ModelPartData bb_main = modelPartData.addChild("bb_main",
                ModelPartBuilder.create()
                        .uv(0, 0)
                        .cuboid(-1.0F, -2.0F, -1.0F,
                                2.0F, 2.0F, 2.0F,
                                new Dilation(0.0F)
                        ),
                ModelTransform.pivot(0.0F, 0.0F, 0.0F)
        );

        return TexturedModelData.of(modelData, 8, 4);
    }

    @Override
    public void setAngles(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

    }

    @Override
    public void render(MatrixStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        bb_main.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }

}