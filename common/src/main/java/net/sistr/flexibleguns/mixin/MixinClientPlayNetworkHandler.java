package net.sistr.flexibleguns.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.sistr.flexibleguns.entity.util.ShouldAccurateVelocitySync;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {

    @Shadow
    private MinecraftClient client;

    @Shadow
    private ClientWorld world;

    @Inject(method = "onEntityVelocityUpdate", at = @At("HEAD"), cancellable = true)
    public void onOnVelocityUpdate(EntityVelocityUpdateS2CPacket packet, CallbackInfo ci) {
        NetworkThreadUtils.forceMainThread(packet, (ClientPlayNetworkHandler) (Object) this, this.client);
        Entity entity = this.world.getEntityById(packet.getId());
        if (entity instanceof ShouldAccurateVelocitySync) {
            ci.cancel();
        }
    }

}
