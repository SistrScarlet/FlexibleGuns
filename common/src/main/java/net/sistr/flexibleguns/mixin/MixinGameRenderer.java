package net.sistr.flexibleguns.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.sistr.flexibleguns.client.FovMultiplier;
import net.sistr.flexibleguns.client.FovCapture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Environment(EnvType.CLIENT)
@Mixin(GameRenderer.class)
public class MixinGameRenderer {

    @ModifyVariable(method = "getFov", at = @At(value = "JUMP", ordinal = 2, shift = At.Shift.AFTER))
    private double modifyGetFov(double fov, Camera camera, float tickDelta, boolean changingFov) {
        fov /= FovMultiplier.INSTANCE.getFov(tickDelta);
        return fov;
    }

    @ModifyVariable(method = "getFov", at = @At(value = "RETURN", ordinal = 1))
    private double onGetFov(double fov, Camera camera, float tickDelta, boolean changingFov) {
        if (changingFov) FovCapture.INSTANCE.setFov(fov);
        return fov;
    }

}
