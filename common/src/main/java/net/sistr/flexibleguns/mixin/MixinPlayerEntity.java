package net.sistr.flexibleguns.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.world.World;
import net.sistr.flexibleguns.util.Zoomable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity extends MixinLivingEntity implements Zoomable {
    private static final TrackedData<Boolean> ZOOM_STATE =
            DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    @Shadow
    @Final
    public PlayerInventory inventory;

    public MixinPlayerEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "initDataTracker", at = @At("RETURN"))
    private void onInitDataTracker(CallbackInfo ci) {
        this.dataTracker.startTracking(ZOOM_STATE, false);
    }

    @Override
    public void setZoom_FG(boolean zoom) {
        this.dataTracker.set(ZOOM_STATE, zoom);
    }

    @Override
    public boolean isZoom_FG() {
        return this.dataTracker.get(ZOOM_STATE);
    }

    @Override
    public void tickItemInstanceFG() {
        for (int i = 0; i < inventory.size(); i++) {
            updateTickableItemFGL(inventory.getStack(i));
        }
        super.tickItemInstanceFG();
    }

    /*private static final LagChecker LAG_CHECKER = new LagChecker();
    @Inject(method = "tick", at = @At("HEAD"))
    public void onTick(CallbackInfo ci) {
        LAG_CHECKER.tick((LivingEntity) (Object)this);
    }*/

}
