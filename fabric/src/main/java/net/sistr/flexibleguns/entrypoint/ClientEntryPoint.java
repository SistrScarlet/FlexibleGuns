package net.sistr.flexibleguns.entrypoint;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.mixin.object.builder.ModelPredicateProviderRegistrySpecificAccessor;
import net.minecraft.util.Identifier;
import net.sistr.flexibleguns.FlexibleGunsMod;
import net.sistr.flexibleguns.client.model.BulletModel;
import net.sistr.flexibleguns.item.GunInstance;
import net.sistr.flexibleguns.setup.ClientSetup;
import net.sistr.flexibleguns.setup.Registration;
import net.sistr.flexibleguns.util.CustomItemStack;
import net.sistr.flexibleguns.util.ItemInstance;

public class ClientEntryPoint implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientSetup.INSTANCE.init();
        ClientSetup.INSTANCE.registerEntityRenderer();
        EntityModelLayerRegistry.registerModelLayer(BulletModel.MODEL_LAYER, BulletModel::createBodyLayer);

        ModelPredicateProviderRegistrySpecificAccessor.callRegister(
                Registration.INSTANCE.getGUN_ITEM_BEFORE(),
                new Identifier(FlexibleGunsMod.MODID, "ammo"),
                (stack, a, entity, b) -> {
                    if (entity != null) {
                        ItemInstance itemIns = ((CustomItemStack) (Object) stack).getItemInstanceFG();
                        return ((GunInstance) itemIns).getAmmoAmount();
                    } else {
                        return 0f;
                    }
                }
        );
    }
}
