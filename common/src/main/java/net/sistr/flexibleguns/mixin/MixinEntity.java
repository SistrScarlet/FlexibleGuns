package net.sistr.flexibleguns.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.sistr.flexibleguns.util.PrevEntity;
import net.sistr.flexibleguns.util.PrevEntityGetter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Entity.class)
public abstract class MixinEntity implements PrevEntityGetter {
    @Shadow
    public abstract Vec3d getPos();

    @Shadow
    public abstract Vec3d getVelocity();

    @Shadow
    public abstract Box getBoundingBox();

    @Shadow
    public abstract float getStandingEyeHeight();

    //PrevPosGetter

    private PrevEntity prevEntity0;
    private PrevEntity prevEntity1;
    private PrevEntity prevEntity2;
    private PrevEntity prevEntity3;

    @Override
    public void nextPrevEntity() {
        if (prevEntity0 == null) {
            PrevEntity now = new PrevEntity(getPos(), getVelocity(), getBoundingBox(), getStandingEyeHeight());
            prevEntity0 = now;
            prevEntity1 = now;
            prevEntity2 = now;
            prevEntity3 = now;
        }
        prevEntity3 = prevEntity2;
        prevEntity2 = prevEntity1;
        prevEntity1 = prevEntity0;
        prevEntity0 = new PrevEntity(getPos(), getVelocity(), getBoundingBox(), getStandingEyeHeight());
    }

    @NotNull
    @Override
    public PrevEntity getPrevEntity(int num) {
        if (prevEntity0 == null || num == 0) {
            return new PrevEntity(getPos(), getVelocity(), getBoundingBox(), getStandingEyeHeight());
        } else if (num == 1) {
            return prevEntity0;
        } else if (num == 2) {
            return prevEntity1;
        } else if (num == 3) {
            return prevEntity2;
        }
        return num < 0
                ? new PrevEntity(getPos(), getVelocity(), getBoundingBox(), getStandingEyeHeight())
                : prevEntity3;
    }

}
