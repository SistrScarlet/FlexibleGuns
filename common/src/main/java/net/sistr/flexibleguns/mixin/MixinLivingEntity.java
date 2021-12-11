package net.sistr.flexibleguns.mixin;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.sistr.flexibleguns.entity.util.SpeedChangeable;
import net.sistr.flexibleguns.util.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity implements ItemInstanceHolder, Inputable, SpeedChangeable, ZoomableEntity {
    private static final TrackedData<Boolean> ZOOM_STATE =
            DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Float> MOVEMENT_SPEED_AMPLIFIER =
            DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private boolean prevZoomInputFG;
    private ZoomableItem prevZoomItem;

    @Shadow
    public abstract ItemStack getMainHandStack();

    @Shadow
    public abstract ItemStack getOffHandStack();

    public MixinLivingEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "initDataTracker", at = @At("RETURN"))
    private void onInitDataTracker(CallbackInfo ci) {
        this.dataTracker.startTracking(ZOOM_STATE, false);
        this.dataTracker.startTracking(MOVEMENT_SPEED_AMPLIFIER, 1.0F);
    }

    //ZoomableEntity

    @Override
    public void setZoom_FG(boolean zoom) {
        this.dataTracker.set(ZOOM_STATE, zoom);
    }

    @Override
    public boolean isZoom_FG() {
        return this.dataTracker.get(ZOOM_STATE);
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
        ItemStack stack = this.getMainHandStack();
        ItemInstance itemIns = ((CustomItemStack) (Object) stack).getItemInstanceFG();
        if (itemIns != null) {
            if (itemIns instanceof ZoomableItem) {
                boolean zoomInput = ((Inputable) this).getInputKeyFG(Input.ZOOM);
                if (!prevZoomInputFG && zoomInput) {
                    boolean zoom = !isZoom_FG();
                    setZoom_FG(zoom);
                    if (zoom) {
                        ((ZoomableItem) itemIns).zoom((LivingEntity) (Object) this);
                        prevZoomItem = (ZoomableItem) itemIns;
                    } else {
                        ((ZoomableItem) itemIns).unZoom((LivingEntity) (Object) this);
                    }
                }
                prevZoomInputFG = zoomInput;
            } else {
                if (isZoom_FG()) {
                    setZoom_FG(false);
                    if (prevZoomItem != null) {
                        prevZoomItem.unZoom((LivingEntity) (Object) this);
                    }
                }
            }

            if (itemIns instanceof SpeedChangeableItem) {
                float speedAmp = ((SpeedChangeableItem) itemIns).getSpeedAmp((LivingEntity) (Object) this);
                setSpeedAmp_FG(speedAmp);
            } else {
                setSpeedAmp_FG(1f);
            }
        } else {
            if (isZoom_FG()) {
                setZoom_FG(false);
                if (prevZoomItem != null) {
                    prevZoomItem.unZoom((LivingEntity) (Object) this);
                }
            }
            setSpeedAmp_FG(1f);
        }

        tickItemInstanceFG();
    }

    @Override
    public void tickItemInstanceFG() {
        updateTickableItemFGL(getMainHandStack(), Hand.MAIN_HAND);
        updateTickableItemFGL(getOffHandStack(), Hand.OFF_HAND);

        Iterator<ItemDate> iterator = tickableItemMapFGL.values().iterator();
        iterator.forEachRemaining(date -> {
            if (date.isAlive()) {
                ItemInstance instance = ((CustomItemStack) (Object) date.getStack()).getItemInstanceFG();
                if (instance != null) {
                    instance.tick(date.getStack(), (LivingEntity) (Object) this, date.getHeldHand());
                }
                date.setAlive(false);
            } else {
                ItemInstance instance = ((CustomItemStack) (Object) date.getStack()).getItemInstanceFG();
                if (instance != null) {
                    instance.endTick(date.getStack(), (LivingEntity) (Object) this, date.getHeldHand());
                }
                iterator.remove();
            }
        });
    }

    protected void updateTickableItemFGL(ItemStack stack, @Nullable Hand heldHand) {
        if (stack.getItem() instanceof CustomItem) {
            //MapからItemDateを取得
            HasIdentifyNumber has = (HasIdentifyNumber) (Object) stack;
            long identifyNumber = has.getIdentifyNumber();
            ItemDate date = tickableItemMapFGL.get(identifyNumber);
            //Mapに無い場合、初期処理カマしてput
            if (date == null) {
                date = new ItemDate(stack, heldHand, true);
                tickableItemMapFGL.put(identifyNumber, date);
                ItemInstance instance = ((CustomItemStack) (Object) date.getStack()).getItemInstanceFG();
                if (instance != null) {
                    instance.startTick(stack, (LivingEntity) (Object) this, heldHand);
                }
            }
            date.setHeldHand(heldHand);
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
