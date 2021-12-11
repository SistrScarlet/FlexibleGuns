package net.sistr.flexibleguns.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity extends MixinLivingEntity {

    @Shadow @Final public PlayerInventory inventory;

    public MixinPlayerEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Override
    public void tickItemInstanceFG() {
        for (int i = 0; i < inventory.size(); i++) {
            updateTickableItemFGL(inventory.getStack(i), null);
        }
        super.tickItemInstanceFG();
    }

    /*private static final LagChecker LAG_CHECKER = new LagChecker();
    @Inject(method = "tick", at = @At("HEAD"))
    public void onTick(CallbackInfo ci) {
        LAG_CHECKER.tick((LivingEntity) (Object)this);
    }*/

}
