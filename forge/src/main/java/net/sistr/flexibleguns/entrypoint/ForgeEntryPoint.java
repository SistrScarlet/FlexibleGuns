package net.sistr.flexibleguns.entrypoint;

import dev.architectury.platform.forge.EventBuses;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Unit;
import net.minecraft.util.profiler.Profiler;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.sistr.flexibleguns.FlexibleGunsMod;
import net.sistr.flexibleguns.client.model.BulletModel;
import net.sistr.flexibleguns.resource.JsonLoader;
import net.sistr.flexibleguns.setup.ClientSetup;
import net.sistr.flexibleguns.setup.ModSetup;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mod(FlexibleGunsMod.MODID)
public class ForgeEntryPoint {

    public ForgeEntryPoint() {
        EventBuses.registerModEventBus(FlexibleGunsMod.MODID, FMLJavaModLoadingContext.get().getModEventBus());
        FlexibleGunsMod.INSTANCE.init();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::modInit);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientInit);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::modelInit);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onRegisterLayers);
        MinecraftForge.EVENT_BUS.addListener(this::reloadListenerInit);
    }

    public void modInit(FMLCommonSetupEvent event) {
        ModSetup.INSTANCE.init();
    }

    public void clientInit(FMLClientSetupEvent event) {
        ClientSetup.INSTANCE.init();
    }

    public void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(BulletModel.MODEL_LAYER, BulletModel::createBodyLayer);
    }

    public void modelInit(ModelRegistryEvent event) {
        /*DataDrivenModelLoader.Companion.getINSTANCE().load(MinecraftClient.getInstance().getResourceManager());
        FlexibleGunsMod.INSTANCE.getLOGGER().info("now loding MixinModelLoader..");
        DataDrivenModelLoader.Companion.getINSTANCE().getIds().forEach(id -> {
            FlexibleGunsMod.INSTANCE.getLOGGER().info(id);
            ModelLoader.addSpecialModel(new ModelIdentifier(id, "inventory"));
        });*/
    }

    public void reloadListenerInit(AddReloadListenerEvent event) {
        event.addListener(new ResourceReloader() {
            @Override
            public CompletableFuture<Void> reload(ResourceReloader.Synchronizer synchronizer,
                                                  ResourceManager manager, Profiler prepareProfiler,
                                                  Profiler applyProfiler, Executor prepareExecutor,
                                                  Executor applyExecutor) {
                return synchronizer.whenPrepared(Unit.INSTANCE).thenRunAsync(() -> {
                    applyProfiler.startTick();
                    applyProfiler.push("listener");
                    this.apply(manager);
                    applyProfiler.pop();
                    applyProfiler.endTick();
                }, applyExecutor);
            }

            private void apply(ResourceManager manager) {
                JsonLoader.Companion.getINSTANCE().load(manager);
            }

            @Override
            public String getName() {
                return "FlexibleGuns";
            }
        });
    }

}
