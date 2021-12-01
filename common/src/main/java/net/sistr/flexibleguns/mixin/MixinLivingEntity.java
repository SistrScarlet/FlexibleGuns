package net.sistr.flexibleguns.mixin;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.sistr.flexibleguns.entity.util.SpeedChangeable;
import net.sistr.flexibleguns.util.*;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.Optional;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity implements ItemInstanceHolder, Inputable, SpeedChangeable {
    private static final TrackedData<Float> MOVEMENT_SPEED_AMPLIFIER =
            DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.FLOAT);

    @Shadow
    public abstract ItemStack getMainHandStack();

    @Shadow
    public abstract ItemStack getOffHandStack();

    public MixinLivingEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "initDataTracker", at = @At("RETURN"))
    private void onInitDataTracker(CallbackInfo ci) {
        this.dataTracker.startTracking(MOVEMENT_SPEED_AMPLIFIER, 1.0F);
    }

    //ISpeedChangeable

    @Override
    public void setSpeedAmp_FG(float amp) {
        this.dataTracker.set(MOVEMENT_SPEED_AMPLIFIER, amp);
    }

    @Override
    public float getSpeedAmp_FG() {
        return this.dataTracker.get(MOVEMENT_SPEED_AMPLIFIER);
    }

    /*@Override
    public void move(MovementType type, Vec3d movement) {
        if (type == MovementType.SELF || type == MovementType.PLAYER) {
            float amp = getSpeedAmp_FG();
            super.move(type, movement.multiply(amp, 1, amp));
        } else {
            super.move(type, movement);
        }
    }*/

    @ModifyArg(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;travel(Lnet/minecraft/util/math/Vec3d;)V"))
    public Vec3d onTravel(Vec3d movementInput) {
        float amp = getSpeedAmp_FG();
        if (amp == 1) return movementInput;
        return movementInput.multiply(amp, 1, amp);
    }

    //IFGTickableEntity

    protected final Long2ObjectOpenHashMap<ItemDate> tickableItemMapFGL = new Long2ObjectOpenHashMap<>(3);

    @Inject(method = "tickMovement", at = @At("HEAD"))
    public void onTickMovement(CallbackInfo ci) {
        tickItemInstanceFG();
    }

    @Override
    public void tickItemInstanceFG() {
        updateTickableItemFGL(getMainHandStack());
        updateTickableItemFGL(getOffHandStack());

        Iterator<ItemDate> iterator = tickableItemMapFGL.values().iterator();
        iterator.forEachRemaining(date -> {
            if (date.isAlive()) {
                date.getInstance().tick();
                date.setAlive(false);
            } else {
                date.getInstance().remove();
                iterator.remove();
            }
        });
    }

    @NotNull
    @Override
    public Optional<ItemInstance> getItemInstanceFG(@NotNull ItemStack stack) {
        updateTickableItemFGL(stack);
        HasIdentifyNumber has = (HasIdentifyNumber) (Object) stack;
        long identifyNumber = has.getIdentifyNumber();
        return Optional.ofNullable(tickableItemMapFGL.get(identifyNumber)).map(ItemDate::getInstance);
    }

    protected void updateTickableItemFGL(ItemStack stack) {
        if (stack.getItem() instanceof ItemInstanceAttachable) {
            HasIdentifyNumber has = (HasIdentifyNumber) (Object) stack;
            long identifyNumber = has.getIdentifyNumber();
            ItemDate date = tickableItemMapFGL.get(identifyNumber);
            if (date == null) {
                ItemInstanceAttachable item = (ItemInstanceAttachable) stack.getItem();
                ItemInstance instance = item.createItemInstanceFG(this.world, (LivingEntity) (Object) this, stack);
                date = new ItemDate(stack, instance, true);
                tickableItemMapFGL.put(identifyNumber, date);
            }
            date.setAlive(true);
        }
    }

    //Inputable

    private final Object2BooleanArrayMap<Input> inputMapFGL = new Object2BooleanArrayMap<>();

    @Override
    public void inputKeyFG(@NotNull Input input, boolean on) {
        inputMapFGL.put(input, on);
    }

    @Override
    public boolean getInputKeyFG(@NotNull Input input) {
        if (inputMapFGL.containsKey(input)) {
            return inputMapFGL.getBoolean(input);
        }
        return false;
    }


}
