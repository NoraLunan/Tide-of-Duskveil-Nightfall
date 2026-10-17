package net.noralunan.duskveil;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.noralunan.duskveil.client.DuskveilClient;
import net.noralunan.duskveil.client.DuskveilOverlay;
import net.noralunan.duskveil.client.OverlayInfo;
import net.noralunan.duskveil.client.VersionChecker;
import net.noralunan.duskveil.command.DuskveilCommand;
import net.noralunan.duskveil.config.DuskveilConfig;
import net.noralunan.duskveil.config.data.MoblistConfig;
import net.noralunan.duskveil.config.data.MoblistHandler;
import net.noralunan.duskveil.core.DuskveilData;
import net.noralunan.duskveil.entity.EntityDriver;
import net.noralunan.duskveil.entity.StatusEffect;
import net.noralunan.duskveil.entity.gaze.GazeMechanic;
import net.noralunan.duskveil.event.EventDetector;
import net.noralunan.duskveil.event.EventManager;
import net.noralunan.duskveil.event.EventProcessor;
import net.noralunan.duskveil.event.WeatherController;
import net.noralunan.duskveil.spawner.MobCountStats;
import net.noralunan.duskveil.spawner.SpawnExecutor;
import net.noralunan.duskveil.spawner.SpawnScheduler;
import net.noralunan.library.common.NoraLogger;
import net.noralunan.library.common.event.NoraEvent;

@Mod(Duskveil.MOD_ID)
public class Duskveil
{
    public static final String MOD_ID = "duskveil";
    public static final String MOD_NAME = "Tide of Duskveil Nightfall";
    public static final String VERSION = "2.3.4";

    private static Duskveil duskveil = null;
    private final ModConfig server;
    private final ModConfig client;
    private final ModConfig common;

    public Duskveil()
    {
        duskveil = this;
        ModContainer container = ModLoadingContext.get().getActiveContainer();

        this.server = new ModConfig(Type.SERVER, DuskveilConfig.SERVER_SPEC, container, DuskveilConfig.SERVER_PATH);
        container.addConfig(this.server);

        this.client = new ModConfig(Type.CLIENT, DuskveilConfig.CLIENT_SPEC, container, DuskveilConfig.CLIENT_PATH);
        container.addConfig(this.client);

        this.common = new ModConfig(Type.COMMON, DuskveilConfig.COMMON_SPEC, container, DuskveilConfig.COMMON_PATH);
        container.addConfig(this.common);

        NoraEvent.register(EventDetector.class);
        NoraEvent.register(EventManager.class);
        NoraEvent.register(WeatherController.class);

        NoraEvent.register(SpawnScheduler.class);

        NoraEvent.register(EntityDriver.class);

        NoraEvent.register(DuskveilClient.class);

        IEventBus mod = FMLJavaModLoadingContext.get().getModEventBus();
        mod.addListener(DuskveilOverlay::setup);
        mod.register(OverlayInfo.EntityEntry.class);
        mod.register(DuskveilClient.GammaOverride.class);

        mod.register(DuskveilData.NetWork.class);

        IEventBus forge = MinecraftForge.EVENT_BUS;
        forge.register(DuskveilData.class);
        forge.register(MoblistConfig.class);
        forge.register(MoblistHandler.class);

        forge.register(EventProcessor.class);
        forge.register(WeatherController.class);

        forge.register(MobCountStats.class);
        forge.register(SpawnScheduler.class);
        forge.register(SpawnExecutor.class);

        forge.register(EntityDriver.class);
        forge.register(StatusEffect.class);
        forge.register(GazeMechanic.class);

        forge.register(DuskveilCommand.class);

        forge.register(DuskveilClient.class);
        forge.register(VersionChecker.class);
        forge.register(OverlayInfo.Pos.class);

        NoraLogger.get().info(MOD_NAME + " Mod Loaded");
    }

    public static ModConfig getConfig(Type type)
    {
        switch (type)
        {
            case SERVER -> { return duskveil.server; }
            case CLIENT -> { return duskveil.client; }
            default -> { return duskveil.common; }
        }
    }

    public static NoraLogger logger()
    {
        return NoraLogger.get(MOD_ID, NoraLogger.STACK_WALKER.getCallerClass());
    }
}
