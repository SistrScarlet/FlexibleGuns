package net.sistr.flexibleguns.entrypoint;

import dev.architectury.platform.forge.EventBuses;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.profiler.Profiler;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.model.ForgeModelBakery;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.sistr.flexibleguns.FlexibleGunsMod;
import net.sistr.flexibleguns.client.model.BulletModel;
import net.sistr.flexibleguns.client.overlay.HudOverlayRenderer;
import net.sistr.flexibleguns.item.GunInstance;
import net.sistr.flexibleguns.resource.DataDrivenModelLoader;
import net.sistr.flexibleguns.resource.JsonLoader;
import net.sistr.flexibleguns.setup.ClientSetup;
import net.sistr.flexibleguns.setup.ModSetup;
import net.sistr.flexibleguns.setup.Registration;
import net.sistr.flexibleguns.util.CustomItemStack;
import net.sistr.flexibleguns.util.ItemInstance;
import thedarkcolour.kotlinforforge.forge.ForgeKt;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mod(FlexibleGunsMod.MODID)
public class ForgeEntryPoint {

    public ForgeEntryPoint() {
        EventBuses.registerModEventBus(FlexibleGunsMod.MODID, ForgeKt.getMOD_BUS());
        FlexibleGunsMod.INSTANCE.init();
        ForgeKt.getMOD_BUS().addListener(this::modInit);
        ForgeKt.getMOD_BUS().addListener(this::clientInit);
        ForgeKt.getMOD_BUS().addListener(this::registerEntityRenderer);
        ForgeKt.getMOD_BUS().addListener(this::modelInit);
        ForgeKt.getMOD_BUS().addListener(this::onRegisterLayers);
        MinecraftForge.EVENT_BUS.addListener(this::reloadListenerInit);
        MinecraftForge.EVENT_BUS.addListener(this::onHudRender);
    }

    public void modInit(FMLCommonSetupEvent event) {
        ModSetup.INSTANCE.init();
    }

    public void clientInit(FMLClientSetupEvent event) {
        ClientSetup.INSTANCE.init();
        ModelPredicateProviderRegistry.register(Registration.INSTANCE.getGUN_ITEM_BEFORE(),
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

    public void registerEntityRenderer(EntityRenderersEvent.RegisterRenderers event) {
        ClientSetup.INSTANCE.registerEntityRenderer();
    }

    public void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(BulletModel.MODEL_LAYER, BulletModel::createBodyLayer);
    }

    public void modelInit(ModelRegistryEvent event) {
        DataDrivenModelLoader.Companion.getINSTANCE().load(MinecraftClient.getInstance().getResourceManager());
        FlexibleGunsMod.INSTANCE.getLOGGER().info("now loding MixinModelLoader..");
        DataDrivenModelLoader.Companion.getINSTANCE().getIds().forEach(id -> {
            FlexibleGunsMod.INSTANCE.getLOGGER().info(id);
            ForgeModelBakery.addSpecialModel(new ModelIdentifier(id, "inventory"));
        });
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

    public void onHudRender(RenderGameOverlayEvent.Pre event) {
        HudOverlayRenderer.Companion.getINSTANCE().render(MinecraftClient.getInstance(), event.getMatrixStack(), event.getPartialTicks());
    }

}
