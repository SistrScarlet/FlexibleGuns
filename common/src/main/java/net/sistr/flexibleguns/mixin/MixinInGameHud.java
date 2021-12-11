package net.sistr.flexibleguns.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.sistr.flexibleguns.client.overlay.HudOverlayRenderer;
import net.sistr.flexibleguns.util.CustomItemStack;
import net.sistr.flexibleguns.util.ItemInstance;
import net.sistr.flexibleguns.util.ShootableItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(InGameHud.class)
public class MixinInGameHud {

    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "render", at = @At("HEAD"))
    public void onRender(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        HudOverlayRenderer.Companion.getINSTANCE().render(this.client, matrices, tickDelta);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void onTick(CallbackInfo ci) {
        HudOverlayRenderer.Companion.getINSTANCE().tick(this.client);
    }

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void onRenderCrosshair(MatrixStack matrices, CallbackInfo ci) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) {
            ItemStack stack = player.getMainHandStack();
            ItemInstance instance = ((CustomItemStack) (Object) stack).getItemInstanceFG();
            if (instance instanceof ShootableItem) {
                ci.cancel();
            }
        }
    }

}
