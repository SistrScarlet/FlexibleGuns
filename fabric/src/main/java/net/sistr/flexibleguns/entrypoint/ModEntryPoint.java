package net.sistr.flexibleguns.entrypoint;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.sistr.flexibleguns.FlexibleGunsMod;
import net.sistr.flexibleguns.resource.JsonLoader;
import net.sistr.flexibleguns.setup.ModSetup;

public class ModEntryPoint implements ModInitializer {

    @Override
    public void onInitialize() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA)
                .registerReloadListener(new SimpleSynchronousResourceReloadListener() {
                    @Override
                    public Identifier getFabricId() {
                        return new Identifier("flexibleguns", "json_loader");
                    }

                    @Override
                    public void reload(ResourceManager manager) {
                        JsonLoader.Companion.getINSTANCE().load(manager);
                    }
                });
        FlexibleGunsMod.INSTANCE.init();
        ModSetup.INSTANCE.init();
    }

}
