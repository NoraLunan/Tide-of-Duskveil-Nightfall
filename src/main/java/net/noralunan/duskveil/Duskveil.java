package net.noralunan.duskveil;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.noralunan.duskveil.config.DuskveilConfig;
import net.noralunan.library.common.NoraLogger;

@Mod(Duskveil.MOD_ID)
public class Duskveil
{
    public static final String MOD_ID = "duskveil";
    public static final String MOD_NAME = "Tide of Duskveil Nightfall";
    public static final String VERSION = "2.3.4";

    private static Duskveil duskveil = null;

    public Duskveil()
    {
        duskveil = this;
        ModContainer container = ModLoadingContext.get().getActiveContainer();
        container.addConfig(new ModConfig(Type.SERVER, DuskveilConfig.SERVER_SPEC, container, DuskveilConfig.SERVER_PATH));

        IEventBus mod = FMLJavaModLoadingContext.get().getModEventBus();

        IEventBus forge = MinecraftForge.EVENT_BUS;

        NoraLogger.get().info(MOD_NAME + " Mod Loaded");
    }

    public static NoraLogger logger()
    {
        return NoraLogger.get(MOD_ID, NoraLogger.STACK_WALKER.getCallerClass());
    }
}
