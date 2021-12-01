package net.sistr.flexibleguns.mixin;

import net.minecraft.item.ItemStack;
import net.sistr.flexibleguns.util.HasIdentifyNumber;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.concurrent.atomic.AtomicLong;

@Mixin(ItemStack.class)
public abstract class MixinItemStack implements HasIdentifyNumber {
    private static final AtomicLong SERIAL_NUM = new AtomicLong();
    private long identifyValue = -1;

    @Shadow
    public abstract boolean isEmpty();

    @Override
    public long getIdentifyNumber() {
        if (this.isEmpty()) return 0L;
        if (this.identifyValue == -1)
            identifyValue = SERIAL_NUM.incrementAndGet();
        return this.identifyValue;
    }
}
