package net.noralunan.duskveil.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.loading.FMLPaths;
import net.noralunan.duskveil.Duskveil;
import net.noralunan.library.common.NoraLogger;
import net.noralunan.library.config.forge.NoraConfig;

import java.io.File;

public class DuskveilConfig {
    public static final NoraLogger LOGGER;

    public static final String SERVER_PATH;
    public static final String CLIENT_PATH;
    public static final String COMMON_PATH;
    public static final String MOBLIST_PATH;

    public static final ServerConfig SERVER;
    public static final ForgeConfigSpec SERVER_SPEC;

    static
    {
        LOGGER = Duskveil.logger();

        String sep = File.separator;
        String modid = Duskveil.MOD_ID;
        String dir = FMLPaths.CONFIGDIR.get().toString() + sep + modid + sep;
        SERVER_PATH = dir + modid + "-server.toml";
        CLIENT_PATH = dir + modid + "-client.toml";
        COMMON_PATH = dir + modid + "-common.toml";
        MOBLIST_PATH = dir + modid + "-moblist.json";

        var server = NoraConfig.create(ServerConfig::new);
        SERVER = server.getLeft();
        SERVER_SPEC = server.getRight();
    }

    public static class ServerConfig
    {
        private final EventConfig event;

        private ServerConfig(NoraConfig config)
        {
            this.event = new EventConfig(config);
        }
    }
}
