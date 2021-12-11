package net.sistr.flexibleguns.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.sistr.flexibleguns.client.overlay.HudOverlayRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ForgeIngameGui.class)
public abstract class MixinForgeIngameGui extends InGameHud {

    public MixinForgeIngameGui(MinecraftClient arg) {
        super(arg);
    }

    @Inject(method = "render", at = @At("HEAD"))
    public void onRender(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        HudOverlayRenderer.Companion.getINSTANCE().render(this.client, matrices, tickDelta);
    }

}
