package net.sistr.flexibleguns.entrypoint;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.sistr.flexibleguns.client.model.BulletModel;
import net.sistr.flexibleguns.setup.ClientSetup;

public class ClientEntryPoint implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientSetup.INSTANCE.init();
        EntityModelLayerRegistry.registerModelLayer(BulletModel.MODEL_LAYER, BulletModel::createBodyLayer);
    }
}
