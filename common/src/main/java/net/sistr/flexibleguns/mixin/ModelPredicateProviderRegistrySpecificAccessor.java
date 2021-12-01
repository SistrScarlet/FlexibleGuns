package net.sistr.flexibleguns.mixin;

import net.minecraft.client.item.ModelPredicateProvider;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

// mixin doesn't care about descriptor, must put two "register" accessors in different places
@Mixin(ModelPredicateProviderRegistry.class)
public interface ModelPredicateProviderRegistrySpecificAccessor {
    @Invoker
    static void callRegister(Item item, Identifier id, ModelPredicateProvider provider) {
        throw new AssertionError("mixin dummy");
    }
}