package net.sistr.flexibleguns.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.profiler.Profiler;
import net.sistr.flexibleguns.FlexibleGunsMod;
import net.sistr.flexibleguns.resource.DataDrivenModelLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ModelLoader.class)
public abstract class MixinModelLoader {

    @Shadow
    protected abstract void addModel(ModelIdentifier modelId);

    @Inject(method = "<init>", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V",
            ordinal = 4, shift = At.Shift.BEFORE))
    private void onInit(ResourceManager resourceManager, BlockColors blockColors, Profiler profiler, int i, CallbackInfo ci) {
        profiler.swap("flexibleguns");
        DataDrivenModelLoader.Companion.getINSTANCE().load(resourceManager);
        FlexibleGunsMod.INSTANCE.getLOGGER().info("now loding MixinModelLoader..");
        DataDrivenModelLoader.Companion.getINSTANCE().getIds().forEach(id -> {
            FlexibleGunsMod.INSTANCE.getLOGGER().info(id);
            this.addModel(new ModelIdentifier(id, "inventory"));
        });
    }

}
