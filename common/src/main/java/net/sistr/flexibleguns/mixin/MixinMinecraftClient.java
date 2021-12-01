package net.sistr.flexibleguns.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.sistr.flexibleguns.client.FovMultiplier;
import net.sistr.flexibleguns.client.Inputs;
import net.sistr.flexibleguns.client.SoundCapManager;
import net.sistr.flexibleguns.wip.ecs.system.SystemManagers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {

    @Inject(method = "tick", at = @At("HEAD"))
    private void preTick(CallbackInfo ci) {
        SoundCapManager.Companion.getINSTANCE().clearSoundCap();
        Inputs.INSTANCE.tick();
        SystemManagers.INSTANCE.getTICK().run();
        FovMultiplier.INSTANCE.tick();
    }

}
