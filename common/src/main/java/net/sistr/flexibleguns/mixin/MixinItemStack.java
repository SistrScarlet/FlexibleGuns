package net.sistr.flexibleguns.mixin;

import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.sistr.flexibleguns.util.CustomItem;
import net.sistr.flexibleguns.util.CustomItemStack;
import net.sistr.flexibleguns.util.HasIdentifyNumber;
import net.sistr.flexibleguns.util.ItemInstance;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.concurrent.atomic.AtomicLong;

@Mixin(ItemStack.class)
public abstract class MixinItemStack implements CustomItemStack, HasIdentifyNumber {
    private static final AtomicLong SERIAL_NUM = new AtomicLong();
    private long identifyValue = -1;
    private boolean hasItemInstanceFG;

    @Shadow
    public abstract Item getItem();

    @Shadow
    public abstract boolean isEmpty();

    @Nullable
    private ItemInstance itemInstanceFG;

    @Inject(method = "<init>(Lnet/minecraft/item/ItemConvertible;I)V", at = @At("RETURN"))
    public void onInit1(ItemConvertible itemConvertible, int i, CallbackInfo ci) {
        hasItemInstanceFG = this.getItem() instanceof CustomItem;
    }

    @Inject(method = "<init>(Lnet/minecraft/nbt/NbtCompound;)V", at = @At("RETURN"))
    public void onInit2(NbtCompound nbtCompound, CallbackInfo ci) {
        hasItemInstanceFG = this.getItem() instanceof CustomItem;
    }

    public long getIdentifyNumber() {
        if (this.isEmpty()) return 0L;
        if (this.identifyValue == -1)
            identifyValue = SERIAL_NUM.incrementAndGet();
        return this.identifyValue;
    }

    @Override
    public boolean hasItemInstanceFG() {
        return hasItemInstanceFG;
    }

    @Nullable
    @Override
    public ItemInstance getItemInstanceFG() {
        if (itemInstanceFG == null) {
            Item item = this.getItem();
            if (item instanceof CustomItem) {
                itemInstanceFG = ((CustomItem) item).createItemInstanceFG((ItemStack) (Object) this);
            }
        }
        return itemInstanceFG;
    }

    @Override
    public void setItemInstanceFG(@Nullable ItemInstance itemInstance) {
        this.itemInstanceFG = itemInstance;
    }

    @Inject(method = "copy", at = @At(value = "TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    public void copyInstance(CallbackInfoReturnable<ItemStack> cir, ItemStack itemStack) {
        if (!this.isEmpty() && this.itemInstanceFG != null) {
            ((CustomItemStack) (Object) itemStack).setItemInstanceFG(this.itemInstanceFG.copy((ItemStack) (Object) this));
        }
    }

    @Inject(method = "writeNbt", at = @At("HEAD"))
    public void onWriteNbt(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> cir) {
        if (this.itemInstanceFG != null) {
            this.itemInstanceFG.save((ItemStack) (Object) this);
        }
    }

}
