package net.sistr.flexibleguns.mixin;

import me.shedaniel.architectury.platform.Platform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.sistr.flexibleguns.item.util.CustomTextureItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(ItemModels.class)
public class MixinItemModels {

    @Shadow
    @Final
    private BakedModelManager modelManager;

    @Inject(method = "getModel(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/client/render/model/BakedModel;",
            at = @At("HEAD"), cancellable = true)
    public void onGetModel(ItemStack stack, CallbackInfoReturnable<BakedModel> cir) {
        Item item = stack.getItem();
        if (item instanceof CustomTextureItem) {
            ModelIdentifier id = ((CustomTextureItem) item).getTextureId(stack);
            if (id != ModelLoader.MISSING_ID) {
                /*if (Platform.isForge()) {
                    id = new ModelIdentifier(new Identifier(id.getNamespace(),"item/" + id.getPath()), id.getVariant());
                }*/
                cir.setReturnValue(this.modelManager.getModel(id));
            }
        }
    }

}
