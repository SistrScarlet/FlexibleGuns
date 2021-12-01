package net.sistr.flexibleguns.entrypoint;

import net.fabricmc.api.ClientModInitializer;
import net.sistr.flexibleguns.setup.ClientSetup;

public class ClientEntryPoint implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientSetup.INSTANCE.init();
    }
}
